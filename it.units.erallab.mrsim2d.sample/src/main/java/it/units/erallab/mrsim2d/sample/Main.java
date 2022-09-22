/*
 * Copyright 2022 eric
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

package it.units.erallab.mrsim2d.sample;

import it.units.erallab.mrsim2d.builder.NamedBuilder;
import it.units.erallab.mrsim2d.core.EmbodiedAgent;
import it.units.erallab.mrsim2d.core.PreparedNamedBuilder;
import it.units.erallab.mrsim2d.core.Snapshot;
import it.units.erallab.mrsim2d.core.actions.*;
import it.units.erallab.mrsim2d.core.agents.gridvsr.AbstractGridVSR;
import it.units.erallab.mrsim2d.core.agents.gridvsr.CentralizedNumGridVSR;
import it.units.erallab.mrsim2d.core.agents.gridvsr.NumGridVSR;
import it.units.erallab.mrsim2d.core.agents.independentvoxel.NumIndependentVoxel;
import it.units.erallab.mrsim2d.core.bodies.Anchor;
import it.units.erallab.mrsim2d.core.bodies.Body;
import it.units.erallab.mrsim2d.core.bodies.RotationalJoint;
import it.units.erallab.mrsim2d.core.bodies.Voxel;
import it.units.erallab.mrsim2d.core.builders.TerrainBuilder;
import it.units.erallab.mrsim2d.core.engine.Engine;
import it.units.erallab.mrsim2d.core.functions.MultiLayerPerceptron;
import it.units.erallab.mrsim2d.core.functions.TimedRealFunction;
import it.units.erallab.mrsim2d.core.geometry.Point;
import it.units.erallab.mrsim2d.core.geometry.Poly;
import it.units.erallab.mrsim2d.core.geometry.Terrain;
import it.units.erallab.mrsim2d.core.tasks.locomotion.Locomotion;
import it.units.erallab.mrsim2d.core.tasks.locomotion.Outcome;
import it.units.erallab.mrsim2d.viewer.*;

import java.io.File;
import java.util.List;
import java.util.Objects;
import java.util.Random;
import java.util.ServiceLoader;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.random.RandomGenerator;
import java.util.stream.IntStream;

/**
 * @author "Eric Medvet" on 2022/07/06 for 2dmrsim
 */
public class Main {
  private static void ball(Engine engine, Terrain terrain, Consumer<Snapshot> consumer) {
    engine.perform(new CreateUnmovableBody(terrain.poly(), false));
    Body ball = engine.perform(new CreateAndTranslateRigidBody(Poly.regular(1, 8), 1, true, new Point(2, 2)))
        .outcome()
        .orElseThrow();
    Voxel voxel = engine.perform(new CreateAndTranslateVoxel(1, 1, new Point(10, 1))).outcome().orElseThrow();
    while (engine.t() < 10) {
      Snapshot snapshot = engine.tick();
      consumer.accept(snapshot);
      engine.perform(new SenseDistanceToBody(0, 2, ball));
      engine.perform(new SenseVelocity(0, ball));
      engine.perform(new SenseRotatedVelocity(0, ball));
      engine.perform(new SenseDistanceToBody(0, 2, voxel));
    }
  }

  private static void iVsrs(Engine engine, Terrain terrain, Consumer<Snapshot> consumer) {
    double interval = 5d;
    double lastT = Double.NEGATIVE_INFINITY;
    double sideInterval = 2d;
    engine.perform(new CreateUnmovableBody(terrain.poly(), false));
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
        v -> new SenseSideAttachment(Voxel.Side.N, v),
        v -> new SenseSideAttachment(Voxel.Side.E, v),
        v -> new SenseSideAttachment(Voxel.Side.S, v),
        v -> new SenseSideAttachment(Voxel.Side.W, v),
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
        engine.perform(new AddAndTranslateAgent(agent, new Point(rg.nextDouble() * 5 + 15, 5)));
      }
    }
  }

  private static void locomotion(Engine engine, Terrain terrain, Consumer<Snapshot> consumer) {
    NamedBuilder<Object> nb = PreparedNamedBuilder.get()
        .and(NamedBuilder.fromClass(NumGridVSR.Body.class));
    String bodyS = """
        body(
          shape=s.vsr.s.biped(w=4;h=3);
          sensorizingFunction=s.vsr.sf.directional(
            sSensors=[s.s.d(a=-90)];
            headSensors=[s.s.sin();s.s.d(a=-15;r=5)];
            nSensors=[s.s.ar();s.s.rv(a=0);s.s.rv(a=90)]
          )
        )
        """;
    NumGridVSR.Body body = (NumGridVSR.Body) nb.build(bodyS);
    int nOfInputs = body.sensorsGrid().values().stream().filter(Objects::nonNull).mapToInt(List::size).sum();
    int nOfOutputs = (int) body.sensorsGrid().values().stream().filter(Objects::nonNull).count();
    MultiLayerPerceptron mlp = new MultiLayerPerceptron(
        MultiLayerPerceptron.ActivationFunction.TANH,
        nOfInputs,
        new int[]{10},
        nOfOutputs
    );
    RandomGenerator rg = new Random();
    mlp.setParams(IntStream.range(0, mlp.getParams().length).mapToDouble(i -> rg.nextDouble(-1, 1)).toArray());
    AbstractGridVSR vsr = new CentralizedNumGridVSR(
        body,
        mlp
    );
    Locomotion locomotion = new Locomotion(30, terrain);
    Outcome outcome = locomotion.run(() -> vsr, engine, consumer);
    System.out.println(outcome);
  }

  public static void main(String[] args) {
    Drawer drawer = Drawers.basic().profiled();
    VideoBuilder videoBuilder = new VideoBuilder(
        400,
        300,
        0,
        30,
        30,
        VideoUtils.EncoderFacility.FFMPEG_LARGE,
        new File("/home/eric/experiments/2dmrsim/rot-joint-pid.mp4"),
        drawer
    );
    RealtimeViewer viewer = new RealtimeViewer(30, drawer);
    Terrain terrain = TerrainBuilder.downhill(2000d, 10d, 1d, 10d, 5d);
    Engine engine = ServiceLoader.load(Engine.class).findFirst().orElseThrow();
    //do thing
    //rotationalJoint(engine, terrain, viewer);
    locomotion(engine, terrain, viewer);
    //ball(engine, terrain, viewer);
    //videoBuilder.get();
  }

  private static void rotationalJoint(Engine engine, Terrain terrain, Consumer<Snapshot> consumer) {
    engine.perform(new CreateUnmovableBody(terrain.poly(), false));
    //engine.perform(new CreateAndTranslateRotationalJoint(3d,1d,1d, new Point(4,0))).outcome().orElseThrow();
    RotationalJoint rj = engine.perform(new CreateAndTranslateRotationalJoint(
        3d,
        1d,
        1d,
        new RotationalJoint.Motor(),
        new Point(5, 5)
    )).outcome().orElseThrow();
    //engine.perform(new RotateBody(rj, new Point(5, 5), Math.toRadians(90)));
    Voxel v1 = engine.perform(new CreateAndTranslateVoxel(1, 1, new Point(4.5, 5.5))).outcome().orElseThrow();
    Voxel v2 = engine.perform(new CreateAndTranslateVoxel(1, 1, new Point(8.5, 5.5))).outcome().orElseThrow();
    engine.perform(new AttachClosestAnchors(2, v1, rj, Anchor.Link.Type.RIGID));
    engine.perform(new AttachClosestAnchors(2, v2, rj, Anchor.Link.Type.RIGID));
    double f = 0.5d;
    double a = 60d;
    while (engine.t() < 10) {
      engine.perform(new ActuateRotationalJoint(rj, Math.toRadians(a * Math.sin(2 * Math.PI * f * engine.t()))));
      Snapshot snapshot = engine.tick();
      consumer.accept(snapshot);
    }
  }
}
