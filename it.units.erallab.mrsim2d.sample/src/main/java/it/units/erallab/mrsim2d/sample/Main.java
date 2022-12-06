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

import it.units.erallab.mrsim2d.buildable.PreparedNamedBuilder;
import it.units.erallab.mrsim2d.core.EmbodiedAgent;
import it.units.erallab.mrsim2d.core.Snapshot;
import it.units.erallab.mrsim2d.core.agents.gridvsr.CentralizedNumGridVSR;
import it.units.erallab.mrsim2d.core.agents.gridvsr.DistributedNumGridVSR;
import it.units.erallab.mrsim2d.core.agents.legged.NumLeggedHybridModularRobot;
import it.units.erallab.mrsim2d.core.agents.legged.NumLeggedHybridRobot;
import it.units.erallab.mrsim2d.core.engine.Engine;
import it.units.erallab.mrsim2d.core.tasks.Task;
import it.units.erallab.mrsim2d.core.tasks.locomotion.Locomotion;
import it.units.erallab.mrsim2d.core.util.DoubleRange;
import it.units.erallab.mrsim2d.core.util.Parametrized;
import it.units.erallab.mrsim2d.viewer.Drawer;
import it.units.erallab.mrsim2d.viewer.RealtimeViewer;
import it.units.erallab.mrsim2d.viewer.VideoBuilder;
import it.units.erallab.mrsim2d.viewer.VideoUtils;
import it.units.malelab.jnb.core.NamedBuilder;

import java.io.File;
import java.util.Random;
import java.util.ServiceLoader;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * @author "Eric Medvet" on 2022/07/06 for 2dmrsim
 */
public class Main {

  private static void activeLegged(
      Supplier<Engine> engineSupplier,
      Task<Supplier<EmbodiedAgent>, ?> task,
      Consumer<Snapshot> consumer
  ) {
    NamedBuilder<Object> nb = PreparedNamedBuilder.get();
    String agentS = """
        s.a.numLeggedHybridRobot(
          legs=3 * [
            s.a.l.leg(
              legChunks=[s.a.l.legChunk(); s.a.l.legChunk()];
              downConnector=soft;
              downConnectorSensors=[s.s.d(a=-90;r=1)]
            )
          ];
          headSensors=[
            s.s.sin();
            s.s.d(a=-30;r=8);
            s.s.d(a=-40;r=8)
          ];
          function=s.f.noised(innerFunction=s.f.mlp();outputSigma=0.01)
        )
        """;
    NumLeggedHybridRobot lhmr = (NumLeggedHybridRobot) nb.build(agentS);
    ((Parametrized)lhmr.brain()).randomize(new Random(), DoubleRange.SYMMETRIC_UNIT);
    task.run(() -> lhmr, engineSupplier.get(), consumer);
  }

  private static void activeModularLegged(
      Supplier<Engine> engineSupplier,
      Task<Supplier<EmbodiedAgent>, ?> task,
      Consumer<Snapshot> consumer
  ) {
    NamedBuilder<Object> nb = PreparedNamedBuilder.get();
    String agentS = """
        s.a.numLeggedHybridModularRobot(
          modules=4 * [
            s.a.l.module(
              legChunks=2*[s.a.l.legChunk()];
              trunkSensors=[s.s.rv(a=0);s.s.rv(a=90)];
              downConnectorSensors=[s.s.d(a=-90;r=1)]
            )
          ];
          function=s.f.noised(innerFunction=s.f.mlp();outputSigma=0.1)
        )
        """;
    NumLeggedHybridModularRobot lhmr = (NumLeggedHybridModularRobot) nb.build(agentS);
    ((Parametrized)lhmr.brain()).randomize(new Random(), DoubleRange.SYMMETRIC_UNIT);
    task.run(() -> lhmr, engineSupplier.get(), consumer);
  }

  private static void centralizedVsr(
      Supplier<Engine> engineSupplier,
      Task<Supplier<EmbodiedAgent>, ?> task,
      Consumer<Snapshot> consumer
  ) {
    NamedBuilder<Object> nb = PreparedNamedBuilder.get();
    String agentS = """
        s.a.centralizedNumGridVSR(
          body=s.a.vsr.gridBody(
            shape=s.a.vsr.s.biped(w=4;h=3);
            sensorizingFunction=s.a.vsr.sf.directional(
              sSensors=[s.s.d(a=-90)];
              headSensors=[
                s.s.sin();
                s.s.d(a=-30;r=8);
                s.s.d(a=-40;r=8)
              ];
              nSensors=[s.s.ar();s.s.rv(a=0);s.s.rv(a=90)]
            )
          );
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
    CentralizedNumGridVSR vsr = (CentralizedNumGridVSR) nb.build(agentS);
    ((Parametrized)vsr.brain()).randomize(new Random(33), new DoubleRange(-5, 5));
    task.run(() -> vsr, engineSupplier.get(), consumer);
  }

  private static void distributedVsr(
      Supplier<Engine> engineSupplier,
      Task<Supplier<EmbodiedAgent>, ?> task,
      Consumer<Snapshot> consumer
  ) {
    NamedBuilder<Object> nb = PreparedNamedBuilder.get();
    String agentS = """
        s.a.distributedNumGridVSR(
          body=s.a.vsr.gridBody(
            shape=s.a.vsr.s.biped(w=4;h=3);
            sensorizingFunction=s.a.vsr.sf.directional(
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
    DistributedNumGridVSR vsr = (DistributedNumGridVSR) nb.build(agentS);
    vsr.brains().forEach(b -> ((Parametrized)b).randomize(new Random(33), new DoubleRange(-5, 5)));
    task.run(() -> vsr, engineSupplier.get(), consumer);
  }

  public static void main(String[] args) {
    NamedBuilder<Object> nb = PreparedNamedBuilder.get();
    @SuppressWarnings("unchecked")
    Drawer drawer = ((Function<String, Drawer>) nb.build("sim.drawer(actions=true)")).apply("test");
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
    Supplier<Engine> engine = () -> ServiceLoader.load(Engine.class).findFirst().orElseThrow();
    Locomotion locomotion = (Locomotion) nb.build("sim.task.locomotion()");
    //do thing
    activeLegged(engine, locomotion, viewer);
  }

  private static void passiveLegged(
      Supplier<Engine> engineSupplier,
      Task<Supplier<EmbodiedAgent>, ?> task,
      Consumer<Snapshot> consumer
  ) {
    NamedBuilder<Object> nb = PreparedNamedBuilder.get();
    String agentS = """
        s.a.numLeggedHybridRobot(
          legs=3 * [
            s.a.l.leg(legChunks=[s.a.l.legChunk(); s.a.l.legChunk()];downConnector=soft)
          ];
          function=s.f.sinP(a=s.range(min=0.0;max=0.5);f=s.range(min=1.0;max=1.0);p=s.range(min=0.0;max=0.0))
        )
        """;
    NumLeggedHybridRobot lhmr = (NumLeggedHybridRobot) nb.build(agentS);
    ((Parametrized)lhmr.brain()).randomize(new Random(), DoubleRange.SYMMETRIC_UNIT);
    task.run(() -> lhmr, engineSupplier.get(), consumer);
  }

  private static void passiveModularLegged(
      Supplier<Engine> engineSupplier,
      Task<Supplier<EmbodiedAgent>, ?> task,
      Consumer<Snapshot> consumer
  ) {
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
    ((Parametrized)lhmr.brain()).randomize(new Random(), DoubleRange.SYMMETRIC_UNIT);
    task.run(() -> lhmr, engineSupplier.get(), consumer);
  }
}
