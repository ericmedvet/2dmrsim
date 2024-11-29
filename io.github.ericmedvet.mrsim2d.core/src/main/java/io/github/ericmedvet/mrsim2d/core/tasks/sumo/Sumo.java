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
package io.github.ericmedvet.mrsim2d.core.tasks.sumo;

import io.github.ericmedvet.mrsim2d.core.EmbodiedAgent;
import io.github.ericmedvet.mrsim2d.core.Snapshot;
import io.github.ericmedvet.mrsim2d.core.actions.AddAgent;
import io.github.ericmedvet.mrsim2d.core.actions.CreateAndTranslateRigidBody;
import io.github.ericmedvet.mrsim2d.core.actions.CreateUnmovableBody;
import io.github.ericmedvet.mrsim2d.core.actions.TranslateAgent;
import io.github.ericmedvet.mrsim2d.core.bodies.Body;
import io.github.ericmedvet.mrsim2d.core.engine.Engine;
import io.github.ericmedvet.mrsim2d.core.geometry.BoundingBox;
import io.github.ericmedvet.mrsim2d.core.geometry.Point;
import io.github.ericmedvet.mrsim2d.core.geometry.Poly;
import io.github.ericmedvet.mrsim2d.core.geometry.Terrain;
import io.github.ericmedvet.mrsim2d.core.tasks.AgentsObservation;
import io.github.ericmedvet.mrsim2d.core.tasks.AgentsOutcome;
import io.github.ericmedvet.mrsim2d.core.tasks.Task;
import io.github.ericmedvet.mrsim2d.core.util.PolyUtils;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class Sumo implements Task<Supplier<EmbodiedAgent>, AgentsObservation, AgentsOutcome<AgentsObservation>> {

  private static final double INITIAL_X_GAP = 1;
  private static final double INITIAL_Y_GAP = 0.25;
  private final double duration;
  private final Terrain terrain;
  private final double initialXGap;
  private final double initialYGap;

  public Sumo(double duration, Terrain terrain, double initialXGap, double initialYGap) {
    this.duration = duration;
    this.terrain = terrain;
    this.initialXGap = initialXGap;
    this.initialYGap = initialYGap;
  }

  public Sumo(double duration, Terrain terrain) {
    this(duration, terrain, INITIAL_X_GAP, INITIAL_Y_GAP);
  }

  @Override
  public AgentsOutcome<AgentsObservation> run(
      Supplier<EmbodiedAgent> embodiedAgentSupplier, Engine engine, Consumer<Snapshot> snapshotConsumer) {
    // create agents
    EmbodiedAgent agent1 = embodiedAgentSupplier.get();
    //    EmbodiedAgent agent2 = embodiedAgentSupplier.get();

    // build world
    engine.perform(new CreateUnmovableBody(terrain.poly()));
    engine.perform(new AddAgent(agent1));
    //    engine.perform(new AddAgent(agent2));

    // place first agent
    BoundingBox agent1BB = agent1.boundingBox();
    engine.perform(new TranslateAgent(
        agent1,
        new Point(
            terrain.withinBordersXRange().min()
                + initialXGap
                + 5
                - agent1BB.min().x(),
            0)));
    agent1BB = agent1.boundingBox();
    double maxY1 = terrain.maxHeightAt(agent1BB.xRange());
    double Y1 = maxY1 + initialYGap - agent1BB.min().y();
    engine.perform(new TranslateAgent(
        agent1, new Point(0, Y1)));

    // place second agent slightly ahead
    //    BoundingBox agent2BB = agent2.boundingBox();
    //    engine.perform(new TranslateAgent(
    //            agent2,
    //            new Point(
    //                    terrain.withinBordersXRange().min()
    //                            + initialXGap
    //                            - agent2BB.min().x() + 5, // Adjust this value to move the agent forward
    //                    0)));
    //    agent2BB = agent2.boundingBox();
    //    double maxY2 = terrain.maxHeightAt(agent2BB.xRange());
    //    engine.perform(new TranslateAgent(
    //            agent2, new Point(0, maxY2 + initialYGap - agent2BB.min().y())));

    // create and place rigid body
    Poly rigidBodyPoly = Poly.rectangle(3, 2);
    double rigidBodyMass = 5;
    double rigidBodyAnchorsDensity = 10;
    BoundingBox rigidBodyBB = rigidBodyPoly.boundingBox();
    Point rigidBodyTranslation = new Point(
        terrain.withinBordersXRange().min()
            + initialXGap
            + 10
            - rigidBodyBB.min().x(),
        Y1);
    engine.perform(new CreateAndTranslateRigidBody(
        rigidBodyPoly, rigidBodyMass, rigidBodyAnchorsDensity, rigidBodyTranslation));

    // run for defined time
    Map<Double, AgentsObservation> observations = new HashMap<>();
    while (engine.t() < duration) {
      Snapshot snapshot = engine.tick();
      snapshotConsumer.accept(snapshot);
      observations.put(
          engine.t(),
          new AgentsObservation(List.of(
              new AgentsObservation.Agent(
                  agent1.bodyParts().stream().map(Body::poly).toList(),
                  PolyUtils.maxYAtX(
                      terrain.poly(),
                      agent1.boundingBox().center().x()))
              //                  ,
              //              new AgentsObservation.Agent(
              //                   agent2.bodyParts().stream().map(Body::poly).toList(),
              //                   PolyUtils.maxYAtX(
              //                       terrain.poly(), agent2.boundingBox().center().x()))
              )));
    }

    // return
    return new AgentsOutcome<>(new TreeMap<>(observations));
  }
}
