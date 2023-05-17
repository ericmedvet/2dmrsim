package io.github.ericmedvet.mrsim2d.buildable.builders;

import io.github.ericmedvet.jnb.core.Param;
import io.github.ericmedvet.jsdynsym.core.StatelessSystem;
import io.github.ericmedvet.jsdynsym.core.numerical.NumericalDynamicalSystem;
import io.github.ericmedvet.jsdynsym.core.numerical.NumericalStatelessSystem;
import io.github.ericmedvet.mrsim2d.core.agents.gridvsr.GridBody;
import io.github.ericmedvet.mrsim2d.core.agents.gridvsr.ReactiveGridVSR;
import io.github.ericmedvet.mrsim2d.core.bodies.Voxel;

import java.util.List;
import java.util.function.Function;

public class ReactiveVoxels {

  private final static NumericalDynamicalSystem<StatelessSystem.State> EMPTY_NDS = nds(0, inputs -> new double[4]);

  private ReactiveVoxels() {
  }

  public static ReactiveGridVSR.ReactiveVoxel asin(
      @Param(value = "f", dD=1.0) double f
  ) {
    return new ReactiveGridVSR.ReactiveVoxel(
        new GridBody.Element(GridBody.VoxelType.NONE, Voxel.DEFAULT_MATERIAL),
        List.of(),
        EMPTY_NDS
    );
  }

  private static NumericalStatelessSystem nds(int nOfInputs, Function<double[], double[]> f) {
    return new NumericalStatelessSystem() {
      @Override
      public int nOfInputs() {
        return nOfInputs;
      }

      @Override
      public int nOfOutputs() {
        return 4;
      }

      @Override
      public double[] step(double t, double[] inputs) {
        return f.apply(inputs);
      }
    };
  }

  @SuppressWarnings("unused")
  public static ReactiveGridVSR.ReactiveVoxel none() {
    return new ReactiveGridVSR.ReactiveVoxel(
        new GridBody.Element(GridBody.VoxelType.NONE, Voxel.DEFAULT_MATERIAL),
        List.of(),
        EMPTY_NDS
    );
  }

  @SuppressWarnings("unused")
  public static ReactiveGridVSR.ReactiveVoxel ph() {
    return new ReactiveGridVSR.ReactiveVoxel(
        new GridBody.Element(GridBody.VoxelType.RIGID, Voxel.DEFAULT_MATERIAL),
        List.of(),
        EMPTY_NDS
    );
  }

  @SuppressWarnings("unused")
  public static ReactiveGridVSR.ReactiveVoxel ps() {
    return new ReactiveGridVSR.ReactiveVoxel(
        new GridBody.Element(GridBody.VoxelType.SOFT, Voxel.DEFAULT_MATERIAL),
        List.of(),
        EMPTY_NDS
    );
  }

}
