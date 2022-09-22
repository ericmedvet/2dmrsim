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


import it.units.erallab.mrsim2d.builder.Param;
import it.units.erallab.mrsim2d.core.agents.WithTimedRealFunction;
import it.units.erallab.mrsim2d.core.functions.TimedRealFunction;
import it.units.erallab.mrsim2d.core.util.Grid;
import it.units.erallab.mrsim2d.core.util.Utils;

import java.util.List;
import java.util.Objects;
import java.util.function.BiFunction;

/**
 * @author "Eric Medvet" on 2022/07/17 for 2dmrsim
 */
public class CentralizedNumGridVSR extends NumGridVSR implements WithTimedRealFunction {

  private TimedRealFunction timedRealFunction;

  public CentralizedNumGridVSR(
      Body body,
      double voxelSideLength,
      double voxelMass
  ) {
    super(body, voxelSideLength, voxelMass);
  }

  public CentralizedNumGridVSR(@Param("body") Body body) {
    this(body, VOXEL_SIDE_LENGTH, VOXEL_MASS);
  }

  public CentralizedNumGridVSR(Body body, TimedRealFunction timedRealFunction) {
    this(body);
    setTimedRealFunction(timedRealFunction);
  }

  private static BiFunction<Double, Grid<double[]>, Grid<Double>> buildGridFunction(
      TimedRealFunction timedRealFunction,
      Body body
  ) {
    int nOfInputs = nOfInputs(body);
    return (t, inputsGrid) -> {
      //build inputs
      double[] inputs = Utils.concat(inputsGrid.values().stream().filter(Objects::nonNull).toList());
      if (inputs.length != nOfInputs) {
        throw new IllegalArgumentException(String.format(
            "Wrong number of inputs: %d expected, %d found",
            nOfInputs,
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
    };

  }

  public static int nOfInputs(Body body) {
    return body.sensorsGrid().values().stream().filter(Objects::nonNull).mapToInt(List::size).sum();
  }

  public static int nOfOutputs(Body body) {
    return (int) body.sensorsGrid().values().stream().filter(Objects::nonNull).count();
  }

  @Override
  public TimedRealFunction getTimedRealFunction() {
    return timedRealFunction;
  }

  @Override
  public void setTimedRealFunction(TimedRealFunction timedRealFunction) {
    int nOfInputs = nOfInputs(getBody());
    int nOfOutputs = nOfOutputs(getBody());
    if (timedRealFunction.nOfInputs() != nOfInputs) {
      throw new IllegalArgumentException(String.format(
          "Function expects %d inputs; %d found",
          timedRealFunction.nOfInputs(),
          nOfInputs
      ));
    }
    if (timedRealFunction.nOfOutputs() != nOfOutputs) {
      throw new IllegalArgumentException(String.format(
          "Function produces %d outputs; %d found",
          timedRealFunction.nOfOutputs(),
          nOfOutputs
      ));
    }
    this.timedRealFunction = timedRealFunction;
    setTimedFunction(buildGridFunction(timedRealFunction, getBody()));
  }
}
