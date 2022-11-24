package it.units.erallab.mrsim2d.core.tasks.piling;

import it.units.erallab.mrsim2d.core.EmbodiedAgent;
import it.units.erallab.mrsim2d.core.Snapshot;
import it.units.erallab.mrsim2d.core.actions.AddAgent;
import it.units.erallab.mrsim2d.core.actions.CreateUnmovableBody;
import it.units.erallab.mrsim2d.core.actions.TranslateAgent;
import it.units.erallab.mrsim2d.core.bodies.Body;
import it.units.erallab.mrsim2d.core.engine.Engine;
import it.units.erallab.mrsim2d.core.geometry.BoundingBox;
import it.units.erallab.mrsim2d.core.geometry.Point;
import it.units.erallab.mrsim2d.core.geometry.Terrain;
import it.units.erallab.mrsim2d.core.tasks.Task;
import it.units.erallab.mrsim2d.core.util.DoubleRange;
import it.units.malelab.jnb.core.BuilderMethod;
import it.units.malelab.jnb.core.Param;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class StandPiling implements Task<Supplier<EmbodiedAgent>, Outcome> {

  private final static double FIRST_X_GAP = 10;
  private final static double INITIAL_Y_GAP = 0.1;
  private final double duration;
  private final int nOfAgents;
  private final double xGapRatio;
  private final Terrain terrain;
  private final double firstXGap;
  private final double initialYGap;

  public StandPiling(
      double duration,
      int nOfAgents,
      double xGapRatio,
      Terrain terrain,
      double firstXGap,
      double initialYGap
  ) {
    this.duration = duration;
    this.nOfAgents = nOfAgents;
    this.xGapRatio = xGapRatio;
    this.terrain = terrain;
    this.firstXGap = firstXGap;
    this.initialYGap = initialYGap;
  }

  @BuilderMethod
  public StandPiling(
      @Param(value = "duration", dD = 45) double duration,
      @Param(value = "nOfAgents") int nOfAgents,
      @Param(value = "xGapRatio", dD = 1) double xGapRatio,
      @Param(value = "terrain") Terrain terrain
  ) {
    this(duration, nOfAgents, xGapRatio, terrain, FIRST_X_GAP, INITIAL_Y_GAP);
  }

  private void placeAgent(Engine engine, EmbodiedAgent agent, List<EmbodiedAgent> agents) {
    double baseX = agents.stream()
        .mapToDouble(a -> a.boundingBox().max().x())
        .max()
        .orElse(terrain.withinBordersXRange().min() + firstXGap);
    BoundingBox agentBB = agent.boundingBox();
    DoubleRange xRange = agentBB.xRange()
        .delta(-agentBB.width() / 2d)
        .delta(baseX + agentBB.width() * xGapRatio);
    double y = terrain.maxHeightAt(xRange) + initialYGap;
    engine.perform(new TranslateAgent(agent, new Point(
        xRange.min() + xRange.extent() / 2d - agentBB.min().x(),
        y - agentBB.min().y()
    )));
  }

  @Override
  public Outcome run(
      Supplier<EmbodiedAgent> embodiedAgentSupplier,
      Engine engine,
      Consumer<Snapshot> snapshotConsumer
  ) {
    //build world
    engine.perform(new CreateUnmovableBody(terrain.poly()));
    //place agents
    List<EmbodiedAgent> agents = new ArrayList<>(nOfAgents);
    while (agents.size() < nOfAgents) {
      EmbodiedAgent agent = embodiedAgentSupplier.get();
      engine.perform(new AddAgent(agent));
      placeAgent(engine, agent, agents);
      agents.add(agent);
    }
    //run for defined time
    Map<Double, Outcome.Observation> observations = new HashMap<>();
    while (engine.t() < duration) {
      //tick
      Snapshot snapshot = engine.tick();
      snapshotConsumer.accept(snapshot);
      observations.put(
          engine.t(),
          new Outcome.Observation(agents.stream()
              .map(a -> a.bodyParts().stream().map(Body::poly).toList())
              .toList()
          )
      );
    }
    return new Outcome(new TreeMap<>(observations));
  }

}
