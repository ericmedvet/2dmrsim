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
import it.units.erallab.mrsim2d.core.agents.gridvsr.CentralizedNumGridVSR;
import it.units.erallab.mrsim2d.core.engine.Engine;
import it.units.erallab.mrsim2d.core.geometry.Terrain;
import it.units.erallab.mrsim2d.core.tasks.locomotion.Locomotion;
import it.units.erallab.mrsim2d.core.tasks.locomotion.Outcome;
import it.units.erallab.mrsim2d.core.util.DoubleRange;
import it.units.erallab.mrsim2d.viewer.Drawer;
import it.units.erallab.mrsim2d.viewer.Drawers;
import it.units.erallab.mrsim2d.viewer.RealtimeViewer;

import java.util.Random;
import java.util.ServiceLoader;
import java.util.function.Supplier;

public class LocomotionStarter {

  public static void main(String[] args) {
    Drawer drawer = Drawers.basic().profiled();
    RealtimeViewer viewer = new RealtimeViewer(30, drawer);
    Engine engine = ServiceLoader.load(Engine.class).findFirst().orElseThrow();
    //do thing
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
    Locomotion locomotion = new Locomotion(30, (Terrain) nb.build("s.t.hilly()"));
    Outcome outcome = locomotion.run(agentSupplier, engine, viewer);
    System.out.println(outcome);
  }
}
