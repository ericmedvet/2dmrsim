/*
 * Copyright 2022 Eric Medvet <eric.medvet@gmail.com> (as eric)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package it.units.erallab.mrsim2d.core.tasks.locomotion;

import it.units.erallab.mrsim2d.builder.BuilderMethod;
import it.units.erallab.mrsim2d.builder.Param;
import it.units.erallab.mrsim2d.core.EmbodiedAgent;
import it.units.erallab.mrsim2d.core.Snapshot;
import it.units.erallab.mrsim2d.core.actions.AddAgent;
import it.units.erallab.mrsim2d.core.actions.CreateUnmovableBody;
import it.units.erallab.mrsim2d.core.actions.TranslateAgent;
import it.units.erallab.mrsim2d.core.bodies.Body;
import it.units.erallab.mrsim2d.core.engine.Engine;
import it.units.erallab.mrsim2d.core.geometry.*;
import it.units.erallab.mrsim2d.core.tasks.Task;
import it.units.erallab.mrsim2d.core.util.PolyUtils;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class Locomotion implements Task<Supplier<EmbodiedAgent>, Outcome> {

  private final static double INITIAL_X_GAP = 1;
  private final static double INITIAL_Y_GAP = 0.25;
  private final double duration;
  private final Terrain terrain;
  private final double initialXGap;
  private final double initialYGap;
  public Locomotion(double duration, Terrain terrain, double initialXGap, double initialYGap) {
    this.duration = duration;
    this.terrain = terrain;
    this.initialXGap = initialXGap;
    this.initialYGap = initialYGap;
  }

  @BuilderMethod
  public Locomotion(
      @Param(value = "duration", dD = 30) double duration,
      @Param(value = "terrain", dNPM = "terrain.flat()") Terrain terrain
  ) {
    this(duration, terrain, INITIAL_X_GAP, INITIAL_Y_GAP);
  }

  @Override
  public Outcome run(
      Supplier<EmbodiedAgent> embodiedAgentSupplier,
      Engine engine,
      Consumer<Snapshot> snapshotConsumer
  ) {
    //create agent
    EmbodiedAgent embodiedAgent = embodiedAgentSupplier.get();
    //build world
    engine.perform(new CreateUnmovableBody(terrain.poly()));
    engine.perform(new AddAgent(embodiedAgent));
    //place agent
    BoundingBox agentBB = embodiedAgent.boundingBox();
    double step = terrain.poly()
        .sides()
        .stream()
        .mapToDouble(Segment::length)
        .min()
        .orElseThrow(() -> new IllegalArgumentException("Cannot find a valid step size"));
    double maxY = Double.NEGATIVE_INFINITY;
    for (double x = terrain.withinBordersXRange().min() + initialXGap;
         x < terrain.withinBordersXRange().min() + initialXGap + agentBB.width();
         x = x + step) {
      maxY = Math.max(maxY, PolyUtils.maxYAtX(terrain.poly(), x));
    }
    engine.perform(new TranslateAgent(embodiedAgent, new Point(
        terrain.withinBordersXRange().min() + initialXGap - agentBB.min().x(),
        maxY + initialYGap - agentBB.min().y()
    )));
    //run for defined time
    Map<Double, Outcome.Observation> observations = new HashMap<>();
    while (engine.t() < duration) {
      Snapshot snapshot = engine.tick();
      snapshotConsumer.accept(snapshot);
      observations.put(
          engine.t(),
          new Outcome.Observation(
              embodiedAgent.bodyParts().stream().map(Body::poly).toList(),
              PolyUtils.maxYAtX(terrain.poly(), embodiedAgent.boundingBox().center().x())
          )
      );
    }
    //return
    return new Outcome(new TreeMap<>(observations));
  }
}
