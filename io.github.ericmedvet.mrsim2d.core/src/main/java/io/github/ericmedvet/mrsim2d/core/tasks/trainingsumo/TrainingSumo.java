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
package io.github.ericmedvet.mrsim2d.core.tasks.trainingsumo;

import io.github.ericmedvet.jnb.datastructure.DoubleRange;
import io.github.ericmedvet.mrsim2d.core.EmbodiedAgent;
import io.github.ericmedvet.mrsim2d.core.Snapshot;
import io.github.ericmedvet.mrsim2d.core.actions.AddAgent;
import io.github.ericmedvet.mrsim2d.core.actions.CreateAndTranslateRigidBody;
import io.github.ericmedvet.mrsim2d.core.actions.CreateUnmovableBody;
import io.github.ericmedvet.mrsim2d.core.actions.TranslateAgent;
import io.github.ericmedvet.mrsim2d.core.bodies.Body;
import io.github.ericmedvet.mrsim2d.core.bodies.RigidBody;
import io.github.ericmedvet.mrsim2d.core.engine.Engine;
import io.github.ericmedvet.mrsim2d.core.geometry.BoundingBox;
import io.github.ericmedvet.mrsim2d.core.geometry.Point;
import io.github.ericmedvet.mrsim2d.core.geometry.Poly;
import io.github.ericmedvet.mrsim2d.core.geometry.Terrain;
import io.github.ericmedvet.mrsim2d.core.tasks.AgentsObservation;
import io.github.ericmedvet.mrsim2d.core.tasks.Task;
import io.github.ericmedvet.mrsim2d.core.util.PolyUtils;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class TrainingSumo implements Task<Supplier<EmbodiedAgent>, TrainingSumoObservation, TrainingSumoAgentOutcome> {

  private static final double INITIAL_X_GAP = 7;
  private static final double INITIAL_Y_GAP = 0.25;
  private final double duration;
  private final Terrain terrain;
  private final double initialXGap;
  private final double initialYGap;

  public TrainingSumo(double duration, Terrain terrain, double initialXGap, double initialYGap) {
    this.duration = duration;
    this.terrain = terrain;
    this.initialXGap = initialXGap;
    this.initialYGap = initialYGap;
  }

  public TrainingSumo(double duration, Terrain terrain) {
    this(duration, terrain, INITIAL_X_GAP, INITIAL_Y_GAP);
  }

  @Override
  public TrainingSumoAgentOutcome run(
      Supplier<EmbodiedAgent> embodiedAgentSupplier, Engine engine, Consumer<Snapshot> snapshotConsumer) {
    // create agent
    EmbodiedAgent agent = embodiedAgentSupplier.get();

    // build world
    engine.perform(new CreateUnmovableBody(terrain.poly()));
    engine.perform(new AddAgent(agent));

    // place first agent
    BoundingBox agent1BB = agent.boundingBox();
    engine.perform(new TranslateAgent(
        agent,
        new Point(
            terrain.withinBordersXRange().min()
                + initialXGap
                - agent1BB.min().x(),
            0)));
    agent1BB = agent.boundingBox();
    double maxY1 = terrain.maxHeightAt(agent1BB.xRange());
    double y1 = maxY1 + initialYGap - agent1BB.min().y();
    engine.perform(new TranslateAgent(agent, new Point(0, y1)));

    // create and place rigid body
    // TODO parameterize w and h of the rigid body
    Poly rigidBodyPoly = Poly.rectangle(3, 3);
    // TODO change rigidBodyMass (instead of Friction, already put again at 1)
    double rigidBodyMass = 2;
    double rigidBodyAnchorsDensity = 0;
    BoundingBox rigidBodyBB = rigidBodyPoly.boundingBox();
    Point rigidBodyTranslation = new Point(terrain.withinBordersXRange().min() + initialXGap * 2, maxY1);
    RigidBody rigidBody = engine.perform(new CreateAndTranslateRigidBody(
            rigidBodyPoly, rigidBodyMass, rigidBodyAnchorsDensity, rigidBodyTranslation))
        .outcome()
        .orElseThrow();

    // run for defined time
    Map<Double, TrainingSumoObservation> observations = new HashMap<>();
    while (engine.t() < duration) {
      Snapshot snapshot = engine.tick();
      snapshotConsumer.accept(snapshot);
      double boxX = rigidBody.poly().boundingBox().center().x();

      if (rigidBody.poly().boundingBox().max().y() < terrain.maxHeightAt(new DoubleRange(boxX, boxX))) {
        System.out.println("Well done agent!");
        break;
      }

      observations.put(
          engine.t(),
          new TrainingSumoObservation(
              List.of(new AgentsObservation.Agent(
                  agent.bodyParts().stream().map(Body::poly).toList(),
                  PolyUtils.maxYAtX(
                      terrain.poly(),
                      agent.boundingBox().center().x()))),
              rigidBody));
    }

    return new TrainingSumoAgentOutcome(new TreeMap<>(observations));
  }
}
