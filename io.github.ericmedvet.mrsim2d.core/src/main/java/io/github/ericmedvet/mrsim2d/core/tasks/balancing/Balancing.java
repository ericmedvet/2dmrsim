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
package io.github.ericmedvet.mrsim2d.core.tasks.balancing;

import io.github.ericmedvet.jnb.datastructure.DoubleRange;
import io.github.ericmedvet.mrsim2d.core.EmbodiedAgent;
import io.github.ericmedvet.mrsim2d.core.Snapshot;
import io.github.ericmedvet.mrsim2d.core.actions.*;
import io.github.ericmedvet.mrsim2d.core.bodies.*;
import io.github.ericmedvet.mrsim2d.core.engine.Engine;
import io.github.ericmedvet.mrsim2d.core.geometry.*;
import io.github.ericmedvet.mrsim2d.core.tasks.AgentsObservation;
import io.github.ericmedvet.mrsim2d.core.tasks.Task;
import io.github.ericmedvet.mrsim2d.core.util.PolyUtils;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class Balancing implements Task<Supplier<EmbodiedAgent>, BalancingObservation, BalancingAgentsOutcome> {

  public static final double TERRAIN_BORDER_W = 10d;
  public static final double TERRAIN_BORDER_H = 100d;
  public static final double TERRAIN_W = 100d;
  public static final double TERRAIN_H = 25d;
  private static final double INITIAL_Y_GAP = 0.25;
  private static final double SUPPORT_WIDTH = 1;
  private static final double SWING_HEIGHT = 0.5;
  private final double duration;
  private final double initialYGap;
  private final double swingLength;
  private final double swingDensity;
  private final double supportHeight;
  private final double initialXGap;

  public Balancing(
      double duration,
      double swingLength,
      double swingDensity,
      double supportHeight,
      double initialXGap,
      double initialYGap) {
    this.duration = duration;
    this.swingLength = swingLength;
    this.swingDensity = swingDensity;
    this.supportHeight = supportHeight;
    this.initialXGap = initialXGap;
    this.initialYGap = initialYGap;
  }

  public Balancing(
      double duration, double swingLength, double swingDensity, double supportHeight, double initialXGap) {
    this(duration, swingLength, swingDensity, supportHeight, initialXGap, INITIAL_Y_GAP);
  }

  @Override
  public BalancingAgentsOutcome run(
      Supplier<EmbodiedAgent> embodiedAgentSupplier, Engine engine, Consumer<Snapshot> snapshotConsumer) {
    // create agent
    EmbodiedAgent embodiedAgent = embodiedAgentSupplier.get();
    // build world
    Terrain terrain =
        Terrain.fromPath(new Path(new Point(TERRAIN_W, 0)), TERRAIN_H, TERRAIN_BORDER_W, TERRAIN_BORDER_H);
    UnmovableBody ground = engine.perform(new CreateUnmovableBody(terrain.poly()))
        .outcome()
        .orElseThrow();
    // create swing
    double worldCenterX = terrain.withinBordersXRange().min()
        + terrain.withinBordersXRange().extent() / 2d;
    Point worldCenter = new Point(worldCenterX, terrain.maxHeightAt(new DoubleRange(worldCenterX, worldCenterX)));
    RigidBody support = engine.perform(new CreateUnmovableBody(Poly.rectangle(SUPPORT_WIDTH, supportHeight), 1d))
        .outcome()
        .orElseThrow();
    engine.perform(new TranslateBodyAt(support, BoundingBox.Anchor.CL, worldCenter));
    RotationalJoint joint = engine.perform(new CreateRotationalJoint(
            2d * SUPPORT_WIDTH,
            SUPPORT_WIDTH,
            1d,
            new RotationalJoint.Motor(0d, 0d, 0d, 0d, 0d, 2d * Math.PI),
            DoubleRange.UNBOUNDED))
        .outcome()
        .orElseThrow();
    engine.perform(new RotateBody(joint, Math.PI / 2d));
    engine.perform(new TranslateBodyAt(
        joint, BoundingBox.Anchor.CL, support.poly().boundingBox().anchor(BoundingBox.Anchor.CU)));
    RigidBody swing = engine.perform(new CreateRigidBody(
            Poly.rectangle(swingLength, SWING_HEIGHT), swingDensity * swingLength * SWING_HEIGHT, 1))
        .outcome()
        .orElseThrow();
    engine.perform(new TranslateBodyAt(
        swing, BoundingBox.Anchor.CL, joint.poly().boundingBox().anchor(BoundingBox.Anchor.CU)));
    engine.perform(new AttachClosestAnchors(2, joint, support, Anchor.Link.Type.RIGID));
    engine.perform(new AttachClosestAnchors(2, swing, joint, Anchor.Link.Type.RIGID));
    // add agent
    engine.perform(new AddAgent(embodiedAgent));
    engine.perform(new TranslateAgentAt(
        embodiedAgent,
        BoundingBox.Anchor.CL,
        swing.poly().boundingBox().anchor(BoundingBox.Anchor.CU).sum(new Point(initialXGap, initialYGap))));
    // run for defined time
    Map<Double, BalancingObservation> observations = new HashMap<>();
    while (engine.t() < duration) {
      Snapshot snapshot = engine.tick();
      snapshotConsumer.accept(snapshot);
      Collection<Body> swingInContactBodies =
          engine.perform(new FindInContactBodies(swing)).outcome().orElseThrow();
      observations.put(
          engine.t(),
          new BalancingObservation(
              List.of(new AgentsObservation.Agent(
                  embodiedAgent.bodyParts().stream()
                      .map(Body::poly)
                      .toList(),
                  PolyUtils.maxYAtX(
                      terrain.poly(),
                      embodiedAgent.boundingBox().center().x()))),
              swing.angle(),
              swingInContactBodies.contains(ground),
              swing.poly().boundingBox()));
    }
    // return
    return new BalancingAgentsOutcome(new TreeMap<>(observations));
  }
}
