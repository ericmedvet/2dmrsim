/*-
 * ========================LICENSE_START=================================
 * mrsim2d-core
 * %%
 * Copyright (C) 2020 - 2025 Eric Medvet
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

package io.github.ericmedvet.mrsim2d.core.tasks.piling;

import io.github.ericmedvet.jnb.datastructure.DoubleRange;
import io.github.ericmedvet.mrsim2d.core.EmbodiedAgent;
import io.github.ericmedvet.mrsim2d.core.Snapshot;
import io.github.ericmedvet.mrsim2d.core.actions.AddAgent;
import io.github.ericmedvet.mrsim2d.core.actions.CreateUnmovableBody;
import io.github.ericmedvet.mrsim2d.core.actions.TranslateAgent;
import io.github.ericmedvet.mrsim2d.core.bodies.Body;
import io.github.ericmedvet.mrsim2d.core.engine.Engine;
import io.github.ericmedvet.mrsim2d.core.geometry.BoundingBox;
import io.github.ericmedvet.mrsim2d.core.geometry.Point;
import io.github.ericmedvet.mrsim2d.core.geometry.Terrain;
import io.github.ericmedvet.mrsim2d.core.tasks.AgentsObservation;
import io.github.ericmedvet.mrsim2d.core.tasks.AgentsOutcome;
import io.github.ericmedvet.mrsim2d.core.tasks.Task;
import io.github.ericmedvet.mrsim2d.core.util.PolyUtils;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class StandPiling implements Task<Supplier<EmbodiedAgent>, AgentsObservation, AgentsOutcome<AgentsObservation>> {

  private static final double FIRST_X_GAP = 10;
  private static final double INITIAL_Y_GAP = 0.1;
  private final double duration;
  private final int nOfAgents;
  private final double xGapRatio;
  private final Terrain terrain;
  private final double terrainAttachableDistance;
  private final double firstXGap;
  private final double initialYGap;

  public StandPiling(
      double duration,
      int nOfAgents,
      double xGapRatio,
      Terrain terrain,
      double terrainAttachableDistance,
      double firstXGap,
      double initialYGap
  ) {
    this.duration = duration;
    this.nOfAgents = nOfAgents;
    this.xGapRatio = xGapRatio;
    this.terrain = terrain;
    this.terrainAttachableDistance = terrainAttachableDistance;
    this.firstXGap = firstXGap;
    this.initialYGap = initialYGap;
  }

  public StandPiling(double duration, int nOfAgents, double xGapRatio, Terrain terrain) {
    this(duration, nOfAgents, xGapRatio, terrain, Double.POSITIVE_INFINITY, FIRST_X_GAP, INITIAL_Y_GAP);
  }

  private void placeAgent(Engine engine, EmbodiedAgent agent, List<EmbodiedAgent> agents) {
    double baseX = agents.stream()
        .mapToDouble(a -> a.boundingBox().max().x())
        .max()
        .orElse(terrain.withinBordersXRange().min() + firstXGap);
    BoundingBox agentBB = agent.boundingBox();
    DoubleRange xRange = agentBB.xRange().delta(-agentBB.width() / 2d).delta(baseX + agentBB.width() * xGapRatio);
    double y = terrain.maxHeightAt(xRange) + initialYGap;
    engine.perform(
        new TranslateAgent(
            agent,
            new Point(
                xRange.min() + xRange.extent() / 2d - agentBB.min().x(),
                y - agentBB.min().y()
            )
        )
    );
  }

  @Override
  public AgentsOutcome<AgentsObservation> run(
      Supplier<EmbodiedAgent> embodiedAgentSupplier,
      Engine engine,
      Consumer<Snapshot> snapshotConsumer
  ) {
    // build world
    engine.perform(new CreateUnmovableBody(terrain.poly(), terrainAttachableDistance));
    // place agents
    List<EmbodiedAgent> agents = new ArrayList<>(nOfAgents);
    while (agents.size() < nOfAgents) {
      EmbodiedAgent agent = embodiedAgentSupplier.get();
      engine.perform(new AddAgent(agent));
      placeAgent(engine, agent, agents);
      agents.add(agent);
    }
    // run for defined time
    snapshotConsumer.accept(engine.snapshot());
    Map<Double, AgentsObservation> observations = new HashMap<>();
    while (engine.t() < duration) {
      // tick
      Snapshot snapshot = engine.tick();
      snapshotConsumer.accept(snapshot);
      observations.put(
          engine.t(),
          new AgentsObservation(
              agents.stream()
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
    return new AgentsOutcome<>(new TreeMap<>(observations));
  }
}
