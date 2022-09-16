package it.units.erallab.mrsim2d.sample;

import it.units.erallab.mrsim2d.core.EmbodiedAgent;
import it.units.erallab.mrsim2d.core.actions.*;
import it.units.erallab.mrsim2d.core.agents.independentvoxel.NumIndependentVoxel;
import it.units.erallab.mrsim2d.core.bodies.Voxel;
import it.units.erallab.mrsim2d.core.builders.TerrainBuilder;
import it.units.erallab.mrsim2d.core.engine.Engine;
import it.units.erallab.mrsim2d.core.functions.MultiLayerPerceptron;
import it.units.erallab.mrsim2d.core.tasks.piling.Outcome;
import it.units.erallab.mrsim2d.core.tasks.piling.StandPiling;
import it.units.erallab.mrsim2d.viewer.Drawer;
import it.units.erallab.mrsim2d.viewer.Drawers;
import it.units.erallab.mrsim2d.viewer.RealtimeViewer;

import java.util.List;
import java.util.Random;
import java.util.ServiceLoader;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.random.RandomGenerator;
import java.util.stream.IntStream;

public class StandPilingStarter {
  public static void main(String[] args) {
    Drawer drawer = Drawers.basic().profiled();
    RealtimeViewer viewer = new RealtimeViewer(30, drawer);
    Engine engine = ServiceLoader.load(Engine.class).findFirst().orElseThrow();
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
        v -> new SenseDistanceToBody(0, 0.75, v),
        v -> new SenseDistanceToBody(Math.PI / 2d, 0.75, v),
        v -> new SenseDistanceToBody(Math.PI, 0.75, v),
        v -> new SenseDistanceToBody(Math.PI / 2d * 3d, 0.75, v)
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
    StandPiling task = new StandPiling(60d, 5, 2d, TerrainBuilder.flat(100d, 10d, 1d, 10d));
    Outcome outcome = task.run(supplier, engine, viewer);
    System.out.println(outcome);
  }
}
