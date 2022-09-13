package it.units.erallab.mrsim2d.sample;

import it.units.erallab.mrsim2d.builder.NamedBuilder;
import it.units.erallab.mrsim2d.core.agents.gridvsr.AbstractGridVSR;
import it.units.erallab.mrsim2d.core.agents.gridvsr.CentralizedNumGridVSR;
import it.units.erallab.mrsim2d.core.agents.gridvsr.NumGridVSR;
import it.units.erallab.mrsim2d.core.builders.GridShapeBuilder;
import it.units.erallab.mrsim2d.core.builders.TerrainBuilder;
import it.units.erallab.mrsim2d.core.builders.VSRSensorizingFunctionBuilder;
import it.units.erallab.mrsim2d.core.builders.VoxelSensorBuilder;
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
        .and(NamedBuilder.fromClass(NumGridVSR.Body.class))
        .and(List.of("terrain", "t"), NamedBuilder.fromUtilityClass(TerrainBuilder.class))
        .and(List.of("shape", "s"), NamedBuilder.fromUtilityClass(GridShapeBuilder.class))
        .and(List.of("sensorizingFunction", "sf"), NamedBuilder.fromUtilityClass(VSRSensorizingFunctionBuilder.class))
        .and(List.of("voxelSensor", "vs"), NamedBuilder.fromUtilityClass(VoxelSensorBuilder.class));
    String bodyS = """
        body(
          shape=s.biped(w=4;h=3);
          sensorizingFunction=sf.directional(
            sSensors=[vs.d(a=-90)];
            headSensors=[vs.sin();vs.d(a=-15;r=5)];
            nSensors=[vs.ar();vs.rv(a=0);vs.rv(a=90)]
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
    Locomotion locomotion = new Locomotion(30, (Terrain) nb.build("t.hilly()"));
    Outcome outcome = locomotion.run(() -> vsr, engine, viewer);
    System.out.println(outcome);
  }
}
