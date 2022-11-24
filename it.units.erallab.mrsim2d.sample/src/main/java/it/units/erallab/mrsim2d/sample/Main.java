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

import it.units.erallab.mrsim2d.core.EmbodiedAgent;
import it.units.erallab.mrsim2d.core.PreparedNamedBuilder;
import it.units.erallab.mrsim2d.core.Sensor;
import it.units.erallab.mrsim2d.core.Snapshot;
import it.units.erallab.mrsim2d.core.actions.*;
import it.units.erallab.mrsim2d.core.agents.gridvsr.CentralizedNumGridVSR;
import it.units.erallab.mrsim2d.core.agents.gridvsr.HeteroDistributedNumGridVSR;
import it.units.erallab.mrsim2d.core.agents.independentvoxel.NumIndependentVoxel;
import it.units.erallab.mrsim2d.core.agents.legged.NumLeggedHybridModularRobot;
import it.units.erallab.mrsim2d.core.bodies.Anchor;
import it.units.erallab.mrsim2d.core.bodies.Body;
import it.units.erallab.mrsim2d.core.bodies.RotationalJoint;
import it.units.erallab.mrsim2d.core.bodies.Voxel;
import it.units.erallab.mrsim2d.core.builders.SensorBuilder;
import it.units.erallab.mrsim2d.core.builders.TerrainBuilder;
import it.units.erallab.mrsim2d.core.engine.Engine;
import it.units.erallab.mrsim2d.core.functions.TimedRealFunction;
import it.units.erallab.mrsim2d.core.geometry.Point;
import it.units.erallab.mrsim2d.core.geometry.Poly;
import it.units.erallab.mrsim2d.core.geometry.Terrain;
import it.units.erallab.mrsim2d.core.tasks.locomotion.Locomotion;
import it.units.erallab.mrsim2d.core.tasks.locomotion.Outcome;
import it.units.erallab.mrsim2d.core.util.DoubleRange;
import it.units.erallab.mrsim2d.viewer.*;
import it.units.malelab.jnb.core.NamedBuilder;

import java.io.File;
import java.util.List;
import java.util.Random;
import java.util.ServiceLoader;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.random.RandomGenerator;

/**
 * @author "Eric Medvet" on 2022/07/06 for 2dmrsim
 */
public class Main {
  private static void ball(Engine engine, Terrain terrain, Consumer<Snapshot> consumer) {
    engine.perform(new CreateUnmovableBody(terrain.poly(), 1));
    Body ball = engine.perform(new CreateAndTranslateRigidBody(
            Poly.regular(0.5, 32),
            1,
            Double.POSITIVE_INFINITY,
            new Point(2, 2)
        ))
        .outcome()
        .orElseThrow();
    engine.perform(new CreateAndTranslateRigidBody(Poly.rectangle(2, 5.05), 1, 1, new Point(5, 2)));
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
    engine.perform(new CreateUnmovableBody(terrain.poly()));
    RandomGenerator rg = new Random();
    List<Sensor<? super Voxel>> sensors = List.of(
        SensorBuilder.rv(0d),
        SensorBuilder.rv(Math.PI / 2d),
        SensorBuilder.ar(),
        SensorBuilder.a(),
        SensorBuilder.sc(Voxel.Side.N),
        SensorBuilder.sc(Voxel.Side.E),
        SensorBuilder.sc(Voxel.Side.S),
        SensorBuilder.sc(Voxel.Side.W),
        SensorBuilder.sa(Voxel.Side.N),
        SensorBuilder.sa(Voxel.Side.E),
        SensorBuilder.sa(Voxel.Side.S),
        SensorBuilder.sa(Voxel.Side.W),
        SensorBuilder.d(Math.PI / 2d * 0d, 0.75),
        SensorBuilder.d(Math.PI / 2d * 1d, 0.75),
        SensorBuilder.d(Math.PI / 2d * 2d, 0.75),
        SensorBuilder.d(Math.PI / 2d * 3d, 0.75)
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

  private static void leggedLocomotion(Supplier<Engine> engineSupplier, Terrain terrain, Consumer<Snapshot> consumer) {
    NamedBuilder<Object> nb = PreparedNamedBuilder.get();
    String agentS = """
        s.a.numLeggedHybridModularRobot(
          modules=[
            s.a.l.module(legChunks=[s.a.l.legChunk(upConnector=rigid); s.a.l.legChunk(upConnector=rigid)];downConnector=soft);
            s.a.l.module(legChunks=[s.a.l.legChunk(upConnector=rigid); s.a.l.legChunk(upConnector=soft)];downConnector=soft);
            s.a.l.module(legChunks=[s.a.l.legChunk(upConnector=rigid); s.a.l.legChunk(upConnector=rigid)];downConnector=soft)
          ];
          function=s.f.sinP(a=s.range(min=0.0;max=0.5);f=s.range(min=1.0;max=1.0);p=s.range(min=0.0;max=0.0))
        )
        """;
    NumLeggedHybridModularRobot lhmr = (NumLeggedHybridModularRobot) nb.build(agentS);
    lhmr.randomize(new Random(), DoubleRange.SYMMETRIC_UNIT);
    Locomotion locomotion = new Locomotion(30, terrain);
    Outcome outcome = locomotion.run(() -> lhmr, engineSupplier.get(), consumer);
    System.out.println(outcome);
  }

  public static void main(String[] args) {
    Drawer drawer = Drawers.basicWithAgentMiniature("").profiled();
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
    Terrain terrain = TerrainBuilder.downhill(2000d, 10d, 1d, 10d, 1d);
    Supplier<Engine> engine = () -> ServiceLoader.load(Engine.class).findFirst().orElseThrow();
    //do thing
    //rotationalJoint(engine, terrain, viewer);
    //vsrLocomotion(engine, terrain, viewer);
    vsrDistributedLocomotion(engine, terrain, viewer);
    //leggedLocomotion(engine, terrain, viewer);
    //ball(engine, terrain, viewer);
    //videoBuilder.get();
  }

  private static void rotationalJoint(Engine engine, Terrain terrain, Consumer<Snapshot> consumer) {
    engine.perform(new CreateUnmovableBody(terrain.poly()));
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

  private static void vsrDistributedLocomotion(Supplier<Engine> engine, Terrain terrain, Consumer<Snapshot> consumer) {
    NamedBuilder<Object> nb = PreparedNamedBuilder.get();
    String agentS = """
        s.a.heteroDistributedNumGridVSR(
          body=s.vsr.gridBody(
            shape=s.vsr.s.biped(w=4;h=3);
            sensorizingFunction=s.vsr.sf.directional(
              sSensors=[s.s.d(a=-90)];
              headSensors=[
                s.s.sin();
                s.s.d(a=-30;r=8);
                s.s.d(a=-40;r=8)
              ];
              nSensors=[s.s.ar();s.s.rv(a=0);s.s.rv(a=90)]
          ));
          function=s.f.stepOut(
            stepT=0.2;
            innerFunction=s.f.diffIn(
              windowT=0.2;
              innerFunction=s.f.mlp(nOfInnerLayers=2;activationFunction=tanh);
              types=[avg;current]
            )
          );
          signals=1;
          directional=t
        )
        """;
    Supplier<EmbodiedAgent> agentSupplier = () -> {
      HeteroDistributedNumGridVSR vsr = (HeteroDistributedNumGridVSR) nb.build(agentS);
      vsr.randomize(new Random(33), new DoubleRange(-5, 5));
      return vsr;
    };
    Locomotion locomotion = new Locomotion(30, terrain);
    locomotion.run(agentSupplier, engine.get(), consumer);
  }

  private static void vsrLocomotion(Supplier<Engine> engine, Terrain terrain, Consumer<Snapshot> consumer) {
    NamedBuilder<Object> nb = PreparedNamedBuilder.get();
    String agentS = """
        s.a.centralizedNumGridVSR(body=s.vsr.gridBody(
          shape=s.vsr.s.biped(w=4;h=3);
          sensorizingFunction=s.vsr.sf.directional(
            sSensors=[s.s.d(a=-90)];
            headSensors=[
              s.s.sin();
              s.s.d(a=-30;r=8);
              s.s.d(a=-40;r=8)
            ];
            nSensors=[s.s.ar();s.s.rv(a=0);s.s.rv(a=90)]
          ));
          function=s.f.stepOut(
            stepT=0.2;
            innerFunction=s.f.diffIn(
              windowT=0.2;
              innerFunction=s.f.mlp(nOfInnerLayers=2;activationFunction=tanh);
              types=[avg;current]
            )
          )
        )
        """;
    Supplier<EmbodiedAgent> agentSupplier = () -> {
      CentralizedNumGridVSR vsr = (CentralizedNumGridVSR) nb.build(agentS);
      vsr.randomize(new Random(33), new DoubleRange(-5, 5));
      return vsr;
    };
    Locomotion locomotion = new Locomotion(30, terrain);
    locomotion.run(agentSupplier, engine.get(), consumer);
  }
}
