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
package io.github.ericmedvet.mrsim2d.core.tasks.trainingfight;

import io.github.ericmedvet.jnb.datastructure.DoubleRange;
import io.github.ericmedvet.mrsim2d.core.EmbodiedAgent;
import io.github.ericmedvet.mrsim2d.core.Snapshot;
import io.github.ericmedvet.mrsim2d.core.XMirrorable;
import io.github.ericmedvet.mrsim2d.core.actions.AddAgent;
import io.github.ericmedvet.mrsim2d.core.actions.CreateUnmovableBody;
import io.github.ericmedvet.mrsim2d.core.actions.TranslateAgent;
import io.github.ericmedvet.mrsim2d.core.bodies.Body;
import io.github.ericmedvet.mrsim2d.core.engine.Engine;
import io.github.ericmedvet.mrsim2d.core.geometry.BoundingBox;
import io.github.ericmedvet.mrsim2d.core.geometry.Point;
import io.github.ericmedvet.mrsim2d.core.geometry.Terrain;
import io.github.ericmedvet.mrsim2d.core.tasks.AgentsObservation;
import io.github.ericmedvet.mrsim2d.core.tasks.Task;
import io.github.ericmedvet.mrsim2d.core.util.PolyUtils;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class TrainingFight implements Task<Supplier<EmbodiedAgent>, TrainingFightObservation, TrainingFightAgentOutcome> {

  private static final double INITIAL_Y_GAP = 0.25;
  private final double duration;
  private final Terrain terrain;
  private final double initialYGap;

  public TrainingFight(double duration, Terrain terrain, double initialYGap) {
    this.duration = duration;
    this.terrain = terrain;
    this.initialYGap = initialYGap;
  }

  public TrainingFight(double duration, Terrain terrain) {
    this(duration, terrain, INITIAL_Y_GAP);
  }

  @Override
  public TrainingFightAgentOutcome run(
      Supplier<EmbodiedAgent> embodiedAgentSupplier,
      Engine engine,
      Consumer<Snapshot> snapshotConsumer
  ) {

    double flatW = (terrain.withinBordersXRange().max() - terrain.withinBordersXRange().min()) / 4;

    double centerX = ((terrain.withinBordersXRange().min() + flatW) + (terrain.withinBordersXRange()
        .max() - flatW)) / 2;
    double centerY = terrain.maxHeightAt(new DoubleRange(centerX, centerX));

    double agent1InitialX = centerX - (flatW - 1);
    double agent2InitialX = centerX + (flatW - 1);

    EmbodiedAgent agent1 = embodiedAgentSupplier.get();
    EmbodiedAgent agent2 = embodiedAgentSupplier.get();
    if (agent2 instanceof XMirrorable xMirrorable) {
      xMirrorable.mirror();
    }

    engine.perform(new CreateUnmovableBody(terrain.poly()));
    engine.perform(new AddAgent(agent1));
    engine.perform(new AddAgent(agent2));

    engine.perform(new TranslateAgent(agent1, new Point(agent1InitialX, 0)));
    BoundingBox agent1BB = agent1.boundingBox();
    double y1 = centerY + initialYGap - agent1BB.min().y();
    engine.perform(new TranslateAgent(agent1, new Point(0, y1)));

    engine.perform(new TranslateAgent(agent2, new Point(agent2InitialX, 0)));
    BoundingBox agent2BB = agent2.boundingBox();
    double y2 = centerY + initialYGap - agent2BB.min().y();
    engine.perform(new TranslateAgent(agent2, new Point(0, y2)));

    Map<Double, TrainingFightObservation> observations = new HashMap<>();
    while ((engine.t() < duration) && (agent1.boundingBox().max().y() > centerY) && (agent2.boundingBox()
        .max()
        .y() > centerY)) {
      Snapshot snapshot = engine.tick();
      snapshotConsumer.accept(snapshot);

      observations.put(
          engine.t(),
          new TrainingFightObservation(
              List.of(
                  new AgentsObservation.Agent(
                      agent1.bodyParts().stream().map(Body::poly).toList(),
                      PolyUtils.maxYAtX(
                          terrain.poly(),
                          agent1.boundingBox().center().x()
                      ),
                      snapshot.agentEnergyConsumptions().get(agent1)
                  ),
                  new AgentsObservation.Agent(
                      agent2.bodyParts().stream().map(Body::poly).toList(),
                      PolyUtils.maxYAtX(
                          terrain.poly(),
                          agent2.boundingBox().center().x()
                      ),
                      snapshot.agentEnergyConsumptions().get(agent2)
                  )
              )
          )
      );
    }
    return new TrainingFightAgentOutcome(new TreeMap<>(observations));
  }
}
