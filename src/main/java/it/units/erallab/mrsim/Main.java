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

package it.units.erallab.mrsim;

import it.units.erallab.mrsim.agents.gridvsr.AbstractGridVSR;
import it.units.erallab.mrsim.agents.gridvsr.CentralizedNumGridVSR;
import it.units.erallab.mrsim.agents.gridvsr.GridVSRUtils;
import it.units.erallab.mrsim.agents.independentvoxel.NumIndependentVoxel;
import it.units.erallab.mrsim.core.EmbodiedAgent;
import it.units.erallab.mrsim.core.Snapshot;
import it.units.erallab.mrsim.core.actions.*;
import it.units.erallab.mrsim.core.bodies.Body;
import it.units.erallab.mrsim.core.bodies.Voxel;
import it.units.erallab.mrsim.core.geometry.Point;
import it.units.erallab.mrsim.core.geometry.Poly;
import it.units.erallab.mrsim.engine.Engine;
import it.units.erallab.mrsim.engine.dyn4j.Dyn4JEngine;
import it.units.erallab.mrsim.functions.MultiLayerPerceptron;
import it.units.erallab.mrsim.functions.TimedRealFunction;
import it.units.erallab.mrsim.util.Grid;
import it.units.erallab.mrsim.util.PolyUtils;
import it.units.erallab.mrsim.viewer.*;

import java.io.File;
import java.util.List;
import java.util.Objects;
import java.util.Random;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.random.RandomGenerator;
import java.util.stream.IntStream;

/**
 * @author "Eric Medvet" on 2022/07/06 for 2dmrsim
 */
public class Main {
  public static void main(String[] args) {
    Drawer drawer = Drawers.basic().profiled();
    VideoBuilder videoBuilder = new VideoBuilder(
        600,
        400,
        0,
        50,
        30,
        VideoUtils.EncoderFacility.FFMPEG_LARGE,
        new File("/home/eric/experiments/balls.mp4"),
        drawer
    );
    RealtimeViewer viewer = new RealtimeViewer(30, drawer);
    Poly terrain = PolyUtils.createTerrain("downhill-5", 500, 5, 1, 5);
    Engine engine = new Dyn4JEngine();
    //do thing
    vsr(engine, terrain, viewer);
    //iVsrs(engine, terrain, viewer);
    //ball(engine, terrain, viewer);
    //do final stuff
    videoBuilder.get();
  }

  private static void vsr(Engine engine, Poly terrain, Consumer<Snapshot> consumer) {
    Poly ball = Poly.regular(1, 20);
    double ballInterval = 5d;
    double lastBallT = 0d;
    Grid<Boolean> shape = GridVSRUtils.buildShape("biped-4x3");
    Grid<List<Function<Voxel, Sense<? super Voxel>>>> sensors = GridVSRUtils.buildSensors(
        //"uniform-a+ar+sd0+v0+v90+sin1",
        "top-a+ar-bottom-t+v0-front-ld0",
        shape
    );
    int nOfInputs = sensors.values().stream().filter(Objects::nonNull).mapToInt(List::size).sum();
    int nOfOutputs = (int) sensors.values().stream().filter(Objects::nonNull).count();
    MultiLayerPerceptron mlp = new MultiLayerPerceptron(
        MultiLayerPerceptron.ActivationFunction.TANH,
        nOfInputs,
        new int[10],
        nOfOutputs
    );
    RandomGenerator rg = new Random();
    mlp.setParams(IntStream.range(0, mlp.getParams().length).mapToDouble(i -> rg.nextDouble(-10, 10)).toArray());
    AbstractGridVSR vsr = new CentralizedNumGridVSR(
        shape.map(b -> b ? new Voxel.Material() : null),
        sensors,
        mlp
    );
    engine.perform(new CreateUnmovableBody(terrain));
    engine.perform(new AddAndTranslateAgent(vsr, new Point(50, 5)));
    while (engine.t() < 100) {
      Snapshot snapshot = engine.tick();
      consumer.accept(snapshot);
      if (engine.t() > lastBallT + ballInterval) {
        lastBallT = engine.t();
        engine.perform(new CreateAndTranslateRigidBody(
            ball,
            2,
            new Point(rg.nextDouble() * 10 + 2, snapshot.bodies()
                .stream()
                .mapToDouble(b -> b.poly().boundingBox().max().y())
                .max()
                .orElse(0) + Math.max(1d, 1d + rg.nextGaussian() * 1.1d))
        ));
      }
    }
  }

  private static void iVsrs(Engine engine, Poly terrain, Consumer<Snapshot> consumer) {
    double interval = 5d;
    double lastT = Double.NEGATIVE_INFINITY;
    double sideInterval = 2d;
    engine.perform(new CreateUnmovableBody(terrain));
    RandomGenerator rg = new Random();
    List<Function<Voxel, Sense<? super Voxel>>> sensors = List.of(
        v -> new SenseRotatedVelocity(0, v),
        v -> new SenseRotatedVelocity(Math.PI / 2d, v),
        SenseAreaRatio::new,
        SenseAngle::new,
        v -> new SenseSideCompression(Voxel.Side.N, v),
        v -> new SenseSideCompression(Voxel.Side.E, v),
        v -> new SenseSideCompression(Voxel.Side.S, v),
        v -> new SenseSideCompression(Voxel.Side.W, v),
        v -> new SenseDistanceToBody(0, 2, v),
        v -> new SenseDistanceToBody(Math.PI, 2, v)
    );
    Function<Integer, TimedRealFunction> functionProvider = index -> TimedRealFunction.from(
        (oT, in) -> {
          double t = oT + index;
          int sideIndex = (int) Math.round(t / sideInterval) % 4;
          double[] out = new double[8];
          for (int i = 0; i < 4; i++) {
            out[i] = (sideIndex == i) ? Math.sin(2d * Math.PI * t) : 0d;
            out[i + 4] = (sideIndex == i) ? ((Math.round(t / sideInterval / 4d) % 2 == 1) ? 1d : -1d) : 0d;
          }
          return out;
        },
        sensors.size(), 8
    );
    while (engine.t() < 100) {
      Snapshot snapshot = engine.tick();
      consumer.accept(snapshot);
      if (engine.t() > lastT + interval) {
        lastT = engine.t();
        EmbodiedAgent agent = new NumIndependentVoxel(sensors, functionProvider.apply(snapshot.agents().size()));
        engine.perform(new AddAndTranslateAgent(agent, new Point(rg.nextDouble() * 5 + 5, 5)));
      }
    }
  }

  private static void ball(Engine engine, Poly terrain, Consumer<Snapshot> consumer) {
    engine.perform(new CreateUnmovableBody(terrain));
    Body ball = engine.perform(new CreateAndTranslateRigidBody(Poly.regular(1, 32), 1, new Point(2, 2)))
        .outcome()
        .orElseThrow();
    Voxel voxel = engine.perform(new CreateAndTranslateVoxel(1, 1, new Point(10, 1))).outcome().orElseThrow();
    while (engine.t() < 100) {
      Snapshot snapshot = engine.tick();
      consumer.accept(snapshot);
      engine.perform(new SenseDistanceToBody(0, 2, ball));
      engine.perform(new SenseVelocity(0, ball));
      engine.perform(new SenseRotatedVelocity(0, ball));
      engine.perform(new SenseDistanceToBody(0, 2, voxel));
    }
  }

}
