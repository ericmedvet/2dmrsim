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
import io.github.ericmedvet.mrsim2d.core.Snapshot;
import io.github.ericmedvet.mrsim2d.core.XMirrorable;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class SumoCup implements HomogeneousBiTask<Supplier<EmbodiedAgent>, SumoAgentsObservation, SumoAgentsOutcome> {
  private final Configuration configuration;

  public record Configuration(
      double wCup,
      double hCup,
      int tCup,
      double duration,
      double initialXGap,
      double initialYGap
  ) {
    public static final Configuration DEFAULT = new Configuration(
        30,
        15,
        5,
        60,
        7,
        0.15
    );
  }

  public SumoCup(double duration, double initialXGap, double initialYGap) {
    this.configuration = new Configuration(
        Configuration.DEFAULT.wCup(),
        Configuration.DEFAULT.hCup(),
        Configuration.DEFAULT.tCup(),
        duration,
        initialXGap,
        initialYGap
    );
  }

  public SumoCup(double duration) {
    this(duration, Configuration.DEFAULT.initialXGap, Configuration.DEFAULT.initialYGap);
  }

  public SumoCup(Configuration configuration) {
    this.configuration = configuration;
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
            .moveBy(0, configuration.hCup() + configuration.tCup())
            .moveBy(configuration.tCup(), 0)
            .moveBy(0, -configuration.hCup())
            .moveBy(configuration.wCup(), 0)
            .moveBy(0, configuration.hCup())
            .moveBy(configuration.tCup(), 0)
            .moveBy(0, -configuration.hCup() - configuration.tCup())
            .toPoly(),
        new DoubleRange(0, configuration.wCup())
    );
    double groundH = configuration.tCup();
    engine.perform(new CreateUnmovableBody(terrain.poly()));
    // put agent 1 on left
    EmbodiedAgent agent1 = embodiedAgentSupplier1.get();
    engine.perform(new AddAgent(agent1));
    engine.perform(
        new TranslateAgentAt(
            agent1,
            BoundingBox.Anchor.LL,
            new Point(
                terrain.withinBordersXRange().min() + configuration.tCup() + configuration.initialXGap(),
                groundH + configuration.initialYGap()
            )
        )
    );
    // put agent 2 on right
    EmbodiedAgent agent2 = embodiedAgentSupplier2.get();
    if (agent2 instanceof XMirrorable xMirrorable) {
      xMirrorable.mirror();
    } else {
      throw new IllegalArgumentException("Agent2 must be XMirrorable");
    }
    engine.perform(new AddAgent(agent2));
    engine.perform(
        new TranslateAgentAt(
            agent2,
            BoundingBox.Anchor.UL,
            new Point(
                terrain.withinBordersXRange().min() + configuration.tCup() + configuration.wCup() - configuration
                    .initialXGap(),
                groundH + configuration.initialYGap()
            )
        )
    );
    Map<Double, SumoAgentsObservation> observations = new HashMap<>();
    while (engine.t() < configuration.duration()) {
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
    return new SumoAgentsOutcome(new TreeMap<>(observations));
  }
}
