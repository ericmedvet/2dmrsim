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

package io.github.ericmedvet.mrsim2d.core.agents.gridvsr;


import io.github.ericmedvet.jsdynsym.core.numerical.NumericalDynamicalSystem;
import io.github.ericmedvet.jsdynsym.grid.Grid;
import io.github.ericmedvet.mrsim2d.core.NumBrained;
import io.github.ericmedvet.mrsim2d.core.util.Utils;

import java.util.Objects;

/**
 * @author "Eric Medvet" on 2022/07/17 for 2dmrsim
 */
public class CentralizedNumGridVSR extends NumGridVSR implements NumBrained {

  private final NumericalDynamicalSystem<?> numericalDynamicalSystem;

  private double[] inputs;
  private double[] outputs;

  public CentralizedNumGridVSR(
      GridBody body,
      double voxelSideLength,
      double voxelMass,
      NumericalDynamicalSystem<?> numericalDynamicalSystem
  ) {
    super(body, voxelSideLength, voxelMass);
    numericalDynamicalSystem.checkDimension(nOfInputs(body), nOfOutputs(body));
    this.numericalDynamicalSystem = numericalDynamicalSystem;
  }

  public CentralizedNumGridVSR(GridBody body, NumericalDynamicalSystem<?> numericalDynamicalSystem) {
    this(body, VOXEL_SIDE_LENGTH, VOXEL_MASS, numericalDynamicalSystem);
  }

  public static int nOfInputs(GridBody body) {
    return body.grid().values().stream()
        .mapToInt(e -> e.sensors().size())
        .sum();
  }

  public static int nOfOutputs(GridBody body) {
    return (int) body.grid().values().stream()
        .filter(e -> !e.element().type().equals(GridBody.VoxelType.NONE))
        .count();
  }

  @Override
  public NumericalDynamicalSystem<?> brain() {
    return numericalDynamicalSystem;
  }

  @Override
  public BrainIO brainIO() {
    return new BrainIO(new RangedValues(inputs, INPUT_RANGE), new RangedValues(outputs, OUTPUT_RANGE));
  }

  @Override
  protected Grid<double[]> computeActuationValues(double t, Grid<double[]> inputsGrid) {
    //build inputs
    inputs = Utils.concat(inputsGrid.values().stream().filter(Objects::nonNull).toList());
    if (inputs.length != numericalDynamicalSystem.nOfInputs()) {
      throw new IllegalArgumentException(String.format(
          "Wrong number of inputs: %d expected, %d found",
          numericalDynamicalSystem.nOfInputs(),
          inputs.length
      ));
    }
    //compute outputs
    outputs = numericalDynamicalSystem.step(t, inputs);
    //split outputs
    Grid<double[]> outputsGrid = Grid.create(inputsGrid.w(), inputsGrid.h(), new double[4]);
    int c = 0;
    for (Grid.Entry<double[]> e : inputsGrid) {
      if (e.value() != null) {
        outputsGrid.set(
            e.key(), new double[]{
                outputs[c], outputs[c], outputs[c], outputs[c]
            }
        );
        c = c + 1;
      }
    }
    return outputsGrid;
  }
}
