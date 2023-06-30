package io.github.ericmedvet.mrsim2d.core.tasks.locomotion;

import io.github.ericmedvet.jsdynsym.grid.Grid;
import io.github.ericmedvet.mrsim2d.core.EmbodiedAgent;
import io.github.ericmedvet.mrsim2d.core.Snapshot;
import io.github.ericmedvet.mrsim2d.core.actions.AddAgent;
import io.github.ericmedvet.mrsim2d.core.actions.AttachClosestAnchors;
import io.github.ericmedvet.mrsim2d.core.actions.CreateUnmovableBody;
import io.github.ericmedvet.mrsim2d.core.actions.TranslateAgent;
import io.github.ericmedvet.mrsim2d.core.agents.gridvsr.GridBody;
import io.github.ericmedvet.mrsim2d.core.agents.independentvoxel.AbstractIndependentVoxel;
import io.github.ericmedvet.mrsim2d.core.bodies.Anchor;
import io.github.ericmedvet.mrsim2d.core.bodies.Body;
import io.github.ericmedvet.mrsim2d.core.engine.Engine;
import io.github.ericmedvet.mrsim2d.core.geometry.BoundingBox;
import io.github.ericmedvet.mrsim2d.core.geometry.Point;
import io.github.ericmedvet.mrsim2d.core.geometry.Terrain;
import io.github.ericmedvet.mrsim2d.core.tasks.Observation;
import io.github.ericmedvet.mrsim2d.core.tasks.Outcome;
import io.github.ericmedvet.mrsim2d.core.tasks.Task;
import io.github.ericmedvet.mrsim2d.core.util.PolyUtils;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.TreeMap;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * @author "Eric Medvet" on 2023/01/21 for 2dmrsim
 */
public class PrebuiltIndependentLocomotion implements Task<Supplier<AbstractIndependentVoxel>, Outcome> {

  private final double duration;
  private final Terrain terrain;
  private final double initialXGap;
  private final double initialYGap;
  private final Grid<GridBody.VoxelType> shape;

  public PrebuiltIndependentLocomotion(
      double duration,
      Terrain terrain,
      double initialXGap,
      double initialYGap,
      Grid<GridBody.VoxelType> shape
  ) {
    this.duration = duration;
    this.terrain = terrain;
    this.initialXGap = initialXGap;
    this.initialYGap = initialYGap;
    this.shape = shape;
  }

  @Override
  public Outcome run(
      Supplier<AbstractIndependentVoxel> abstractIndependentVoxelSupplier,
      Engine engine,
      Consumer<Snapshot> snapshotConsumer
  ) {
    //build world
    engine.perform(new CreateUnmovableBody(terrain.poly()));
    //place agents
    Grid<AbstractIndependentVoxel> agents = shape.map(t -> switch (t) {
      case NONE, RIGID -> null;
      case SOFT -> (AbstractIndependentVoxel) engine
          .perform(new AddAgent(abstractIndependentVoxelSupplier.get()))
          .outcome()
          .orElseThrow();
    });
    BoundingBox oneBB = agents.values().stream()
        .filter(Objects::nonNull)
        .findFirst()
        .orElseThrow()
        .boundingBox();
    agents.entries().stream()
        .filter(e -> e.value() != null)
        .forEach(e -> engine.perform(new TranslateAgent(e.value(), new Point(
            oneBB.width() * e.key().x(),
            oneBB.height() * e.key().y()
        ))));
    BoundingBox allBB = agents.values().stream()
        .filter(Objects::nonNull)
        .map(EmbodiedAgent::boundingBox)
        .reduce(BoundingBox::enclosing)
        .orElseThrow();
    double dX = terrain.withinBordersXRange().min() + initialXGap - allBB.min().x();
    double maxY = terrain.maxHeightAt(allBB.xRange().delta(dX));
    agents.values().stream()
        .filter(Objects::nonNull)
        .forEach(a -> engine.perform(new TranslateAgent(a, new Point(
            dX,
            maxY + initialYGap - allBB.min().y()
        ))));
    //attach agents
    for (Grid.Key key : agents.keys()) {
      if (agents.get(key) == null) {
        continue;
      }
      Grid.Key[] adjacentKeys = new Grid.Key[]{
          key.translated(1, 0),
          key.translated(0, 1)
      };
      for (Grid.Key adjacentKey : adjacentKeys) {
        if (agents.isValid(adjacentKey) && agents.get(adjacentKey) != null) {
          engine.perform(new AttachClosestAnchors(
              2,
              agents.get(key).voxel(),
              agents.get(adjacentKey).voxel(),
              Anchor.Link.Type.RIGID
          ));
        }
      }
    }
    //run for defined time
    Map<Double, Observation> observations = new HashMap<>();
    while (engine.t() < duration) {
      Snapshot snapshot = engine.tick();
      snapshotConsumer.accept(snapshot);
      observations.put(
          engine.t(),
          new Observation(agents.values().stream()
              .filter(Objects::nonNull)
              .map(a -> new Observation.Agent(
                  a.bodyParts().stream().map(Body::poly).toList(),
                  PolyUtils.maxYAtX(terrain.poly(), a.boundingBox().center().x())
              ))
              .toList()
          )
      );
    }
    //return
    return new Outcome(new TreeMap<>(observations));
  }
}
