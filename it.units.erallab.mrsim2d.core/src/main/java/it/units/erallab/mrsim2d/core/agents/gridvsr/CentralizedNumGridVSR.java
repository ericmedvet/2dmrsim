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


import it.units.erallab.mrsim2d.builder.BuilderMethod;
import it.units.erallab.mrsim2d.builder.Param;
import it.units.erallab.mrsim2d.core.functions.TimedRealFunction;
import it.units.erallab.mrsim2d.core.util.Parametrized;

import java.util.function.BiFunction;

/**
 * @author "Eric Medvet" on 2022/07/17 for 2dmrsim
 */
public class CentralizedNumGridVSR extends NumGridVSR implements Parametrized {

  private final TimedRealFunction timedRealFunction;

  public CentralizedNumGridVSR(
      GridBody body,
      double voxelSideLength,
      double voxelMass,
      TimedRealFunction timedRealFunction
  ) {
    super(body, voxelSideLength, voxelMass, new GridTimedRealFunction(body, timedRealFunction));
    this.timedRealFunction = timedRealFunction;
  }

  @BuilderMethod
  public CentralizedNumGridVSR(
      @Param("body") GridBody body,
      @Param("function") BiFunction<Integer, Integer, ? extends TimedRealFunction> timedRealFunctionBuilder
  ) {
    this(body, timedRealFunctionBuilder.apply(
        GridTimedRealFunction.nOfInputs(body),
        GridTimedRealFunction.nOfOutputs(body)
    ));
  }

  public CentralizedNumGridVSR(GridBody body, TimedRealFunction timedRealFunction) {
    this(body, VOXEL_SIDE_LENGTH, VOXEL_MASS, timedRealFunction);
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
