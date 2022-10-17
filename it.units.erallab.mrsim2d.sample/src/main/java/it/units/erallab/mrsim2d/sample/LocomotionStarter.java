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
import it.units.erallab.mrsim2d.core.agents.gridvsr.AbstractGridVSR;
import it.units.erallab.mrsim2d.core.agents.gridvsr.CentralizedNumGridVSR;
import it.units.erallab.mrsim2d.core.agents.gridvsr.DistributedNumGridVSR;
import it.units.erallab.mrsim2d.core.agents.gridvsr.GridBody;
import it.units.erallab.mrsim2d.core.builders.GridShapeBuilder;
import it.units.erallab.mrsim2d.core.builders.SensorBuilder;
import it.units.erallab.mrsim2d.core.builders.TerrainBuilder;
import it.units.erallab.mrsim2d.core.builders.VSRSensorizingFunctionBuilder;
import it.units.erallab.mrsim2d.core.engine.Engine;
import it.units.erallab.mrsim2d.core.functions.MultiLayerPerceptron;
import it.units.erallab.mrsim2d.core.geometry.Terrain;
import it.units.erallab.mrsim2d.core.tasks.locomotion.Locomotion;
import it.units.erallab.mrsim2d.core.tasks.locomotion.Outcome;
import it.units.erallab.mrsim2d.viewer.Drawer;
import it.units.erallab.mrsim2d.viewer.Drawers;
import it.units.erallab.mrsim2d.viewer.RealtimeViewer;

import java.util.List;
import java.util.Objects;
import java.util.Random;
import java.util.ServiceLoader;
import java.util.random.RandomGenerator;
import java.util.stream.IntStream;

public class LocomotionStarter {

  public static void main(String[] args) {
    Drawer drawer = Drawers.basic().profiled();
    RealtimeViewer viewer = new RealtimeViewer(30, drawer);
    Engine engine = ServiceLoader.load(Engine.class).findFirst().orElseThrow();
    //do thing
    NamedBuilder<Object> nb = NamedBuilder.empty()
        .and(NamedBuilder.fromClass(GridBody.class))
        .and(List.of("terrain", "t"), NamedBuilder.fromUtilityClass(TerrainBuilder.class))
        .and(List.of("shape", "s"), NamedBuilder.fromUtilityClass(GridShapeBuilder.class))
        .and(List.of("sensorizingFunction", "sf"), NamedBuilder.fromUtilityClass(VSRSensorizingFunctionBuilder.class))
        .and(List.of("voxelSensor", "vs"), NamedBuilder.fromUtilityClass(SensorBuilder.class));
    /*String bodyS = """
        gridBody(
          shape=s.biped(w=4;h=3);
          sensorizingFunction=sf.directional(
            sSensors=[vs.rv(a=-90)];
            headSensors=[
              vs.d(a=-30;r=8);
              vs.d(a=-40;r=8)
            ];
            nSensors=[vs.ar();vs.rv(a=0);vs.rv(a=90)]
        ))
        """;*/
    String bodyS = """
        gridBody(
          shape=s.biped(w=4;h=3);
          sensorizingFunction=sf.uniform(
            sensors=[vs.ar();vs.rv(a=0);vs.rv(a=90)]
        ))
        """;
    GridBody body = (GridBody) nb.build(bodyS);
    int nSignals = 2;
    boolean directional = true;

    int nOfInputs = body.sensorsGrid().values().stream().filter(Objects::nonNull).findFirst().get().size() + 4 * nSignals;
    int nOfOutputs = 1 + (directional ? 4 * nSignals : nSignals);
    MultiLayerPerceptron mlp = new MultiLayerPerceptron(
        MultiLayerPerceptron.ActivationFunction.TANH,
        nOfInputs,
        new int[]{8},
        nOfOutputs
    );
    RandomGenerator rg = new Random();
    mlp.setParams(IntStream.range(0, mlp.getParams().length).mapToDouble(i -> rg.nextDouble(-1, 1)).toArray());
    AbstractGridVSR vsr = new DistributedNumGridVSR(
        body,
        mlp,
        nSignals,
        directional
    );
    Locomotion locomotion = new Locomotion(30, (Terrain) nb.build("t.hilly()"));
    Outcome outcome = locomotion.run(() -> vsr, engine, viewer);
    System.out.println(outcome);
  }
}
