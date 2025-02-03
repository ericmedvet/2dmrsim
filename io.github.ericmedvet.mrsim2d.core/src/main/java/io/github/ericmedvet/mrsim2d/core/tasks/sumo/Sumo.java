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

import io.github.ericmedvet.jnb.datastructure.DoubleRange;
import io.github.ericmedvet.mrsim2d.core.EmbodiedAgent;
import io.github.ericmedvet.mrsim2d.core.Mirrorable;
import io.github.ericmedvet.mrsim2d.core.Snapshot;
import io.github.ericmedvet.mrsim2d.core.XMirrorer;
import io.github.ericmedvet.mrsim2d.core.actions.AddAgent;
import io.github.ericmedvet.mrsim2d.core.actions.CreateUnmovableBody;
import io.github.ericmedvet.mrsim2d.core.actions.TranslateAgentAt;
import io.github.ericmedvet.mrsim2d.core.bodies.Body;
import io.github.ericmedvet.mrsim2d.core.engine.Engine;
import io.github.ericmedvet.mrsim2d.core.geometry.BoundingBox;
import io.github.ericmedvet.mrsim2d.core.geometry.Path;
import io.github.ericmedvet.mrsim2d.core.geometry.Point;
import io.github.ericmedvet.mrsim2d.core.geometry.Terrain;
import io.github.ericmedvet.mrsim2d.core.tasks.AgentsObservation;
import io.github.ericmedvet.mrsim2d.core.tasks.HomogeneousBiTask;
import io.github.ericmedvet.mrsim2d.core.util.PolyUtils;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class Sumo implements HomogeneousBiTask<Supplier<EmbodiedAgent>, SumoAgentsObservation, SumoAgentsOutcome> {

  private static final double INITIAL_Y_GAP = 0.1;
  private static final double HOLE_W = 10;
  private static final double HOLE_H = 15;
  private static final double FLAT_W = 15;
  private final double duration;
  private final double initialYGap;

  public Sumo(double duration, double initialYGap) {
    this.duration = duration;
    this.initialYGap = initialYGap;
  }

  public Sumo(double duration) {
    this(duration, INITIAL_Y_GAP);
  }

  @Override
  public SumoAgentsOutcome run(
      Supplier<EmbodiedAgent> embodiedAgentSupplier1,
      Supplier<EmbodiedAgent> embodiedAgentSupplier2,
      Engine engine,
      Consumer<Snapshot> snapshotConsumer
  ) {
    Terrain terrain = new Terrain(
        new Path(Point.ORIGIN)
            .moveBy(HOLE_W, 0)
            .moveBy(0, HOLE_H)
            .moveBy(FLAT_W, 0)
            .moveBy(0, -HOLE_H)
            .moveBy(HOLE_W, 0)
            .moveBy(0, -HOLE_H)
            .moveBy(-2 * HOLE_W - FLAT_W, 0)
            .toPoly(),
        new DoubleRange(HOLE_W, HOLE_W + FLAT_W)
    );
    System.out.println(terrain);
    double groundH = HOLE_H;
    EmbodiedAgent agent1 = embodiedAgentSupplier1.get();
    EmbodiedAgent agent2 = embodiedAgentSupplier2.get();
    // TODO: move actions-filter to NumGridVSR
    if (agent2 instanceof Mirrorable mirrorable) {
      mirrorable.mirror();
    }
    engine.registerActionsFilter(agent1, new XMirrorer<>());
    engine.registerActionsFilter(agent2, new XMirrorer<>());
    engine.perform(new CreateUnmovableBody(terrain.poly()));
    engine.perform(new AddAgent(agent1));
    engine.perform(new AddAgent(agent2));
    engine.perform(
        new TranslateAgentAt(
            agent1,
            BoundingBox.Anchor.LL,
            new Point(terrain.withinBordersXRange().min(), groundH + initialYGap)
        )
    );
    engine.perform(
        new TranslateAgentAt(
            agent2,
            BoundingBox.Anchor.UL,
            new Point(terrain.withinBordersXRange().max(), groundH + initialYGap)
        )
    );
    Map<Double, SumoAgentsObservation> observations = new HashMap<>();
    while ((engine.t() < duration) && (agent1.boundingBox().max().y() > groundH) && (agent2.boundingBox()
        .max()
        .y() > groundH)) {
      Snapshot snapshot = engine.tick();
      snapshotConsumer.accept(snapshot);

      observations.put(
          engine.t(),
          new SumoAgentsObservation(
              List.of(
                  new AgentsObservation.Agent(
                      agent1.bodyParts().stream().map(Body::poly).toList(),
                      PolyUtils.maxYAtX(
                          terrain.poly(),
                          agent1.boundingBox().center().x()
                      )
                  ),
                  new AgentsObservation.Agent(
                      agent2.bodyParts().stream().map(Body::poly).toList(),
                      PolyUtils.maxYAtX(
                          terrain.poly(),
                          agent2.boundingBox().center().x()
                      )
                  )
              )
          )
      );
    }

    return new SumoAgentsOutcome(new TreeMap<>(observations));
  }
}
