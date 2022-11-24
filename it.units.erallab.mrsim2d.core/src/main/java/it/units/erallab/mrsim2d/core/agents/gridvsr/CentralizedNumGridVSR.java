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

package it.units.erallab.mrsim2d.core.agents.gridvsr;


import it.units.erallab.mrsim2d.core.functions.TimedRealFunction;
import it.units.erallab.mrsim2d.core.util.Grid;
import it.units.erallab.mrsim2d.core.util.Parametrized;
import it.units.erallab.mrsim2d.core.util.Utils;
import it.units.malelab.jnb.core.BuilderMethod;
import it.units.malelab.jnb.core.Param;

import java.util.List;
import java.util.Objects;
import java.util.function.BiFunction;

/**
 * @author "Eric Medvet" on 2022/07/17 for 2dmrsim
 */
public class CentralizedNumGridVSR extends NumGridVSR {

  private final TimedRealFunction timedRealFunction;

  public CentralizedNumGridVSR(
      GridBody body,
      double voxelSideLength,
      double voxelMass,
      TimedRealFunction timedRealFunction
  ) {
    super(body, voxelSideLength, voxelMass);
    this.timedRealFunction = timedRealFunction;
  }

  @BuilderMethod
  public CentralizedNumGridVSR(
      @Param("body") GridBody body,
      @Param("function") BiFunction<Integer, Integer, ? extends TimedRealFunction> timedRealFunctionBuilder
  ) {
    this(body, timedRealFunctionBuilder.apply(
        nOfInputs(body),
        nOfOutputs(body)
    ));
  }

  public CentralizedNumGridVSR(GridBody body, TimedRealFunction timedRealFunction) {
    this(body, VOXEL_SIDE_LENGTH, VOXEL_MASS, timedRealFunction);
  }

  public static int nOfInputs(GridBody body) {
    return body.sensorsGrid().values().stream().filter(Objects::nonNull).mapToInt(List::size).sum();
  }

  public static int nOfOutputs(GridBody body) {
    return (int) body.sensorsGrid().values().stream().filter(Objects::nonNull).count();
  }

  @Override
  protected Grid<Double> computeActuationValues(double t, Grid<double[]> inputsGrid) {
    //build inputs
    double[] inputs = Utils.concat(inputsGrid.values().stream().filter(Objects::nonNull).toList());
    if (inputs.length != timedRealFunction.nOfInputs()) {
      throw new IllegalArgumentException(String.format(
          "Wrong number of inputs: %d expected, %d found",
          timedRealFunction.nOfInputs(),
          inputs.length
      ));
    }
    //compute outputs
    double[] outputs = timedRealFunction.apply(t, inputs);
    //split outputs
    Grid<Double> outputsGrid = Grid.create(inputsGrid.w(), inputsGrid.h(), 0d);
    int c = 0;
    for (Grid.Entry<double[]> e : inputsGrid) {
      if (e.value() != null) {
        outputsGrid.set(e.key(), outputs[c]);
        c = c + 1;
      }
    }
    return outputsGrid;
  }

  @Override
  public double[] getParams() {
    if (timedRealFunction instanceof Parametrized parametrized) {
      return parametrized.getParams();
    }
    return new double[0];
  }

  @Override
  public void setParams(double[] params) {
    if (timedRealFunction instanceof Parametrized parametrized) {
      parametrized.setParams(params);
    } else if (params.length > 0) {
      throw new IllegalArgumentException(
          "Cannot set params because the function %s has no params".formatted(
              timedRealFunction
          ));
    }
  }
}
