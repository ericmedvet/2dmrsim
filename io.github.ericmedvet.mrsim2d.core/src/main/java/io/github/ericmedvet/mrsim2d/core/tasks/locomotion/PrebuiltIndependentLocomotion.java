/*-
 * ========================LICENSE_START=================================
 * mrsim2d-core
 * %%
 * Copyright (C) 2020 - 2024 Eric Medvet
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * =========================LICENSE_END==================================
 */
package io.github.ericmedvet.mrsim2d.core.tasks.locomotion;

import io.github.ericmedvet.jnb.datastructure.Grid;
import io.github.ericmedvet.mrsim2d.core.EmbodiedAgent;
import io.github.ericmedvet.mrsim2d.core.Snapshot;
import io.github.ericmedvet.mrsim2d.core.actions.AddAgent;
import io.github.ericmedvet.mrsim2d.core.actions.AttachClosestAnchors;
import io.github.ericmedvet.mrsim2d.core.actions.CreateUnmovableBody;
import io.github.ericmedvet.mrsim2d.core.actions.TranslateAgent;
import io.github.ericmedvet.mrsim2d.core.agents.gridvsr.GridBody;
import io.github.ericmedvet.mrsim2d.core.agents.gridvsr.GridBody.VoxelType;
import io.github.ericmedvet.mrsim2d.core.agents.independentvoxel.AbstractIndependentVoxel;
import io.github.ericmedvet.mrsim2d.core.bodies.Anchor;
import io.github.ericmedvet.mrsim2d.core.bodies.Body;
import io.github.ericmedvet.mrsim2d.core.bodies.UnmovableBody;
import io.github.ericmedvet.mrsim2d.core.engine.Engine;
import io.github.ericmedvet.mrsim2d.core.geometry.BoundingBox;
import io.github.ericmedvet.mrsim2d.core.geometry.Point;
import io.github.ericmedvet.mrsim2d.core.geometry.Poly;
import io.github.ericmedvet.mrsim2d.core.geometry.Terrain;
import io.github.ericmedvet.mrsim2d.core.tasks.AgentsObservation;
import io.github.ericmedvet.mrsim2d.core.tasks.AgentsOutcome;
import io.github.ericmedvet.mrsim2d.core.tasks.Task;
import io.github.ericmedvet.mrsim2d.core.util.PolyUtils;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class PrebuiltIndependentLocomotion implements Task<Supplier<AbstractIndependentVoxel>, AgentsObservation, AgentsOutcome<AgentsObservation>> {

  private final double duration;
  private final Terrain terrain;
  private final double terrainAttachableDistance;
  private final double initialXGap;
  private final double initialYGap;
  private final double xGapRatio;
  private final double yGapRatio;
  private final Grid<VoxelType> shape;

  public PrebuiltIndependentLocomotion(
      double duration,
      Terrain terrain,
      double terrainAttachableDistance,
      double initialXGap,
      double initialYGap,
      double xGapRatio,
      double yGapRatio,
      Grid<GridBody.VoxelType> shape
  ) {
    this.duration = duration;
    this.terrain = terrain;
    this.terrainAttachableDistance = terrainAttachableDistance;
    this.initialXGap = initialXGap;
    this.initialYGap = initialYGap;
    this.xGapRatio = xGapRatio;
    this.yGapRatio = yGapRatio;
    this.shape = shape;
  }

  @Override
  public AgentsOutcome<AgentsObservation> run(
      Supplier<AbstractIndependentVoxel> abstractIndependentVoxelSupplier,
      Engine engine,
      Consumer<Snapshot> snapshotConsumer
  ) {
    // build world
    engine.perform(new CreateUnmovableBody(terrain.poly(), terrainAttachableDistance));
    // place agents
    Grid<AbstractIndependentVoxel> agents = shape.map(t -> switch (t) {
      case NONE, RIGID -> null;
      case SOFT -> (AbstractIndependentVoxel) engine.perform(new AddAgent(abstractIndependentVoxelSupplier.get()))
          .outcome()
          .orElseThrow();
    });
    final double maxBBX = agents.values()
            .stream()
            .filter(Objects::nonNull)
            .mapToDouble(v -> v.boundingBox().width())
            .max().orElseThrow();
    final double maxBBY = agents.values()
            .stream()
            .filter(Objects::nonNull)
            .mapToDouble(v -> v.boundingBox().height())
            .max().orElseThrow();
    double currX = 0;
    double currY = 0;
    double[] maxYPerLine = new double[agents.h()];
    for (int j = 0; j < agents.h(); ++j) {
      maxYPerLine[j] = 0;
      for (int i = 0; i < agents.w(); i++) {
        if (Objects.nonNull(agents.get(i, j))) {
          maxYPerLine[j] = Math.max(maxYPerLine[j], agents.get(i, j).boundingBox().height());
        }
      }
      if (maxYPerLine[j] == 0) {
        maxYPerLine[j] = maxBBY;
      }
    }
    for (int j = 0; j < agents.h(); ++j) {
      currY += maxYPerLine[j] * yGapRatio / 2;
      for (int i = 0; i < agents.w(); ++i) {
        if (Objects.nonNull(agents.get(i, j))) {
          currX += agents.get(i, j).boundingBox().width() * xGapRatio / 2;
          engine.perform(new TranslateAgent(agents.get(i, j), new Point(currX, currY)));
          currX += agents.get(i, j).boundingBox().width() * xGapRatio / 2;
        } else {
          currX += maxBBX * xGapRatio / 2;
        }
      }
      currY += maxYPerLine[j] * yGapRatio / 2;
      currX = 0;
    }
    BoundingBox allBB = agents.values()
        .stream()
        .filter(Objects::nonNull)
        .map(EmbodiedAgent::boundingBox)
        .reduce(BoundingBox::enclosing)
        .orElseThrow();
    double dX = terrain.withinBordersXRange().min() + initialXGap - allBB.min().x();
    double maxY = terrain.maxHeightAt(allBB.xRange().delta(dX));
    agents.values()
        .stream()
        .filter(Objects::nonNull)
        .forEach(
            a -> engine.perform(
                new TranslateAgent(
                    a,
                    new Point(dX, maxY + initialYGap - allBB.min().y())
                )
            )
        );
    // attach agents
    for (Grid.Key key : agents.keys()) {
      if (agents.get(key) == null) {
        continue;
      }
      Grid.Key[] adjacentKeys = new Grid.Key[]{key.translated(1, 0), key.translated(0, 1)};
      for (Grid.Key adjacentKey : adjacentKeys) {
        if (agents.isValid(adjacentKey) && agents.get(adjacentKey) != null) {
          engine.perform(
              new AttachClosestAnchors(
                  2,
                  agents.get(key).voxel(),
                  agents.get(adjacentKey).voxel(),
                  Anchor.Link.Type.RIGID
              )
          );
        }
      }
    }
    // run for defined time
    Map<Double, AgentsObservation> observations = new HashMap<>();
    while (engine.t() < duration) {
      Snapshot snapshot = engine.tick();
      snapshotConsumer.accept(snapshot);
      observations.put(
          engine.t(),
          new AgentsObservation(
              agents.values()
                  .stream()
                  .filter(Objects::nonNull)
                  .map(
                      a -> new AgentsObservation.Agent(
                          a.bodyParts().stream().map(Body::poly).toList(),
                          PolyUtils.maxYAtX(
                              terrain.poly(),
                              a.boundingBox().center().x()
                          ),
                          snapshot.agentEnergyConsumptions().get(a)
                      )
                  )
                  .toList()
          )
      );
    }
    // return
    return new AgentsOutcome<>(new TreeMap<>(observations));
  }
}
