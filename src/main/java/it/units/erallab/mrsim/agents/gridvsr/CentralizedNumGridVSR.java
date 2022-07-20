/*
 * Copyright 2022 Eric Medvet <eric.medvet@gmail.com> (as eric)
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

package it.units.erallab.mrsim.agents.gridvsr;

import it.units.erallab.mrsim.core.actions.Sense;
import it.units.erallab.mrsim.core.bodies.Voxel;
import it.units.erallab.mrsim.functions.TimedRealFunction;
import it.units.erallab.mrsim.util.Grid;
import it.units.erallab.mrsim.util.Utils;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @author "Eric Medvet" on 2022/07/17 for 2dmrsim
 */
public class CentralizedNumGridVSR extends NumGridVSR {

  public CentralizedNumGridVSR(
      Grid<Voxel.Material> materialGrid,
      double voxelSideLength,
      double voxelMass,
      Grid<List<Function<Voxel, Sense<? super Voxel>>>> sensorsGrid,
      TimedRealFunction timedRealFunction
  ) {
    super(materialGrid, voxelSideLength, voxelMass, sensorsGrid, buildGridFunction(timedRealFunction, sensorsGrid));
  }

  public CentralizedNumGridVSR(
      Grid<Voxel.Material> materialGrid,
      Grid<List<Function<Voxel, Sense<? super Voxel>>>> sensorsGrid,
      TimedRealFunction timedRealFunction
  ) {
    this(materialGrid, AbstractGridVSR.VOXEL_SIDE_LENGTH, AbstractGridVSR.VOXEL_MASS, sensorsGrid, timedRealFunction);
  }

  private static BiFunction<Double, Grid<double[]>, Grid<Double>> buildGridFunction(
      TimedRealFunction timedRealFunction,
      Grid<List<Function<Voxel, Sense<? super Voxel>>>> sensorsGrid
  ) {
    int nOfInputs = sensorsGrid.values().stream().filter(Objects::nonNull).mapToInt(List::size).sum();
    int nOfOutputs = (int) sensorsGrid.values().stream().filter(Objects::nonNull).count();
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

}
