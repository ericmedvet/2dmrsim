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
import io.github.ericmedvet.mrsim2d.core.tasks.Outcome;
import io.github.ericmedvet.mrsim2d.core.tasks.Task;
import io.github.ericmedvet.mrsim2d.core.util.PolyUtils;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.random.RandomGenerator;

public class FallPiling implements Task<Supplier<EmbodiedAgent>, Outcome<AgentsObservation>> {

  private static final double X_GAP = 10;

  private final double duration;
  private final double fallInterval;
  private final int nOfAgents;
  private final double xSigmaRatio;
  private final RandomGenerator randomGenerator;
  private final Terrain terrain;
  private final double yGapRatio;
  private final double xGap;

  public FallPiling(
      double duration,
      double fallInterval,
      int nOfAgents,
      double xSigmaRatio,
      RandomGenerator randomGenerator,
      Terrain terrain,
      double yGapRatio,
      double xGap) {
    this.duration = duration;
    this.fallInterval = fallInterval;
    this.nOfAgents = nOfAgents;
    this.xSigmaRatio = xSigmaRatio;
    this.randomGenerator = randomGenerator;
    this.terrain = terrain;
    this.xGap = xGap;
    this.yGapRatio = yGapRatio;
  }

  public FallPiling(
      double duration,
      double fallInterval,
      int nOfAgents,
      double xSigmaRatio,
      RandomGenerator randomGenerator,
      Terrain terrain,
      double yGapRatio) {
    this(duration, fallInterval, nOfAgents, xSigmaRatio, randomGenerator, terrain, yGapRatio, X_GAP);
  }

  private void placeAgent(Engine engine, EmbodiedAgent agent, List<EmbodiedAgent> agents) {
    BoundingBox agentBB = agent.boundingBox();
    DoubleRange xRange = new DoubleRange(-agentBB.width() / 2d, agentBB.width() / 2d)
        .delta(terrain.withinBordersXRange().min() + xGap);
    double baseY;
    if (agents.isEmpty()) {
      baseY = terrain.maxHeightAt(xRange);
    } else {
      baseY = agents.stream()
          .map(EmbodiedAgent::boundingBox)
          .filter(b ->
              xRange.overlaps(new DoubleRange(b.min().x(), b.max().x())))
          .mapToDouble(b -> b.max().y())
          .max()
          .orElse(0d);
    }
    baseY = baseY + agentBB.height() * yGapRatio;
    engine.perform(new TranslateAgent(
        agent,
        new Point(
            xRange.min()
                + xRange.extent() / 2d
                + randomGenerator.nextGaussian(0d, xSigmaRatio * agentBB.width())
                - agentBB.min().x(),
            baseY - agentBB.min().y())));
  }

  @Override
  public Outcome<AgentsObservation> run(
      Supplier<EmbodiedAgent> embodiedAgentSupplier, Engine engine, Consumer<Snapshot> snapshotConsumer) {
    // build world
    engine.perform(new CreateUnmovableBody(terrain.poly()));
    // run for defined time
    Map<Double, AgentsObservation> observations = new HashMap<>();
    List<EmbodiedAgent> agents = new ArrayList<>(nOfAgents);
    while (engine.t() < duration) {
      // check if new agent needed
      if (agents.size() < Math.ceil(engine.t() / fallInterval) && agents.size() < nOfAgents) {
        EmbodiedAgent agent = embodiedAgentSupplier.get();
        engine.perform(new AddAgent(agent));
        placeAgent(engine, agent, agents);
        agents.add(agent);
      }
      // tick
      Snapshot snapshot = engine.tick();
      snapshotConsumer.accept(snapshot);
      observations.put(
          engine.t(),
          new AgentsObservation(agents.stream()
              .map(a -> new AgentsObservation.Agent(
                  a.bodyParts().stream().map(Body::poly).toList(),
                  PolyUtils.maxYAtX(
                      terrain.poly(),
                      a.boundingBox().center().x())))
              .toList()));
    }
    return new Outcome<>(new TreeMap<>(observations));
  }
}
