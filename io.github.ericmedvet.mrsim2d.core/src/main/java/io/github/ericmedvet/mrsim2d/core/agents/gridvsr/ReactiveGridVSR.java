/*-
 * ========================LICENSE_START=================================
 * mrsim2d-core
 * %%
 * Copyright (C) 2020 - 2023 Eric Medvet
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * =========================LICENSE_END==================================
 */
package io.github.ericmedvet.mrsim2d.core.agents.gridvsr;

import io.github.ericmedvet.jsdynsym.core.numerical.NumericalDynamicalSystem;
import io.github.ericmedvet.jsdynsym.grid.Grid;
import io.github.ericmedvet.mrsim2d.core.Sensor;
import io.github.ericmedvet.mrsim2d.core.bodies.Body;
import io.github.ericmedvet.mrsim2d.core.bodies.Voxel;
import java.util.List;
import java.util.Map;

public class ReactiveGridVSR extends NumGridVSR {

  private final Grid<ReactiveVoxel> reactiveVoxelGrid;

  public ReactiveGridVSR(Grid<ReactiveVoxel> reactiveVoxelGrid) {
    this(reactiveVoxelGrid, VOXEL_SIDE_LENGTH, VOXEL_MASS);
  }

  public ReactiveGridVSR(Grid<ReactiveVoxel> reactiveVoxelGrid, double voxelSideLength, double voxelMass) {
    super(
        new GridBody(reactiveVoxelGrid.map(re -> new GridBody.SensorizedElement(re.element(), re.sensors()))),
        voxelSideLength,
        voxelMass);
    this.reactiveVoxelGrid = reactiveVoxelGrid;
  }

  public record ReactiveVoxel(
      GridBody.Element element,
      List<Sensor<? super Body>> sensors,
      NumericalDynamicalSystem<?> numericalDynamicalSystem) {
    public ReactiveVoxel {
      if (numericalDynamicalSystem.nOfInputs() != sensors.size()) {
        throw new IllegalArgumentException("Wrong number of inputs: %d found, %d expected by the controller"
            .formatted(sensors.size(), numericalDynamicalSystem.nOfInputs()));
      }
      if (numericalDynamicalSystem.nOfOutputs() != 4) {
        throw new IllegalArgumentException("Wrong number of outputs: %d produced by the controller, 4 expected"
            .formatted(numericalDynamicalSystem.nOfInputs()));
      }
    }
  }

  public record ReactiveVoxelAction(Map<Voxel.Side, Double> sideActions) {}

  @Override
  protected Grid<double[]> computeActuationValues(double t, Grid<double[]> inputsGrid) {
    return inputsGrid.map((k, inputs) -> inputs == null
        ? new double[4]
        : reactiveVoxelGrid.get(k).numericalDynamicalSystem().step(t, inputs));
  }
}
