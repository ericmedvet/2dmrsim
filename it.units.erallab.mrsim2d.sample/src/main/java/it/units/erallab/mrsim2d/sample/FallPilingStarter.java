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
import it.units.erallab.mrsim2d.core.Sensor;
import it.units.erallab.mrsim2d.core.agents.independentvoxel.NumIndependentVoxel;
import it.units.erallab.mrsim2d.core.bodies.Voxel;
import it.units.erallab.mrsim2d.core.builders.SensorBuilder;
import it.units.erallab.mrsim2d.core.builders.TerrainBuilder;
import it.units.erallab.mrsim2d.core.engine.Engine;
import it.units.erallab.mrsim2d.core.functions.MultiLayerPerceptron;
import it.units.erallab.mrsim2d.core.tasks.piling.FallPiling;
import it.units.erallab.mrsim2d.core.tasks.piling.Outcome;
import it.units.erallab.mrsim2d.viewer.Drawer;
import it.units.erallab.mrsim2d.viewer.Drawers;
import it.units.erallab.mrsim2d.viewer.RealtimeViewer;

import java.util.List;
import java.util.Random;
import java.util.ServiceLoader;
import java.util.function.Supplier;
import java.util.random.RandomGenerator;
import java.util.stream.IntStream;

public class FallPilingStarter {
  public static void main(String[] args) {
    Drawer drawer = Drawers.basic().profiled();
    RealtimeViewer viewer = new RealtimeViewer(30, drawer);
    Engine engine = ServiceLoader.load(Engine.class).findFirst().orElseThrow();
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
    MultiLayerPerceptron mlp = new MultiLayerPerceptron(
        MultiLayerPerceptron.ActivationFunction.TANH,
        sensors.size(),
        new int[] {sensors.size()},
        8
    );
    RandomGenerator rg = new Random(1);
    mlp.setParams(IntStream.range(0, mlp.getParams().length).mapToDouble(i -> rg.nextDouble(-1, 1)).toArray());
    Supplier<EmbodiedAgent> supplier = () -> new NumIndependentVoxel(sensors, mlp);
    FallPiling task = new FallPiling(60d, 5d, 5, 0.11d, rg, TerrainBuilder.flat(100d, 10d, 1d, 10d), 1, 10);
    Outcome outcome = task.run(supplier, engine, viewer);
    System.out.println(outcome);
  }
}
