/*-
 * ========================LICENSE_START=================================
 * mrsim2d-core
 * %%
 * Copyright (C) 2020 - 2024 Eric Medvet
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

import io.github.ericmedvet.jnb.datastructure.Grid;
import io.github.ericmedvet.jsdynsym.core.numerical.NumericalDynamicalSystem;
import io.github.ericmedvet.mrsim2d.core.NumMultiBrained;
import io.github.ericmedvet.mrsim2d.core.Sensor;
import io.github.ericmedvet.mrsim2d.core.bodies.Body;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

public class DistributedNumGridVSR extends NumGridVSR implements NumMultiBrained {

  private final Grid<NumericalDynamicalSystem<?>> numericalDynamicalSystemGrid;
  private final int nOfSignals;
  private final boolean directional;
  private final Grid<double[]> signalsGrid;

  private final Grid<double[]> fullInputsGrid;
  private final Grid<double[]> fullOutputsGrid;

  public DistributedNumGridVSR(
      GridBody body,
      Grid<NumericalDynamicalSystem<?>> numericalDynamicalSystemGrid,
      int nOfSignals,
      boolean directional
  ) {
    super(body);
    int communicationSize = directional ? nOfSignals * 4 : nOfSignals;
    body.grid().entries().forEach(e -> {
      if (e.value().element().type().equals(GridBody.VoxelType.NONE)) {
        if (numericalDynamicalSystemGrid.get(e.key()) != null) {
          throw new IllegalArgumentException("Unexpected non-null NDS at %s".formatted(e.key()));
        }
      } else {
        numericalDynamicalSystemGrid
            .get(e.key())
            .checkDimension(
                nOfInputs(body, e.key(), nOfSignals, directional),
                nOfOutputs(body, e.key(), nOfSignals, directional)
            );
      }
    });
    this.nOfSignals = nOfSignals;
    this.directional = directional;
    this.numericalDynamicalSystemGrid = numericalDynamicalSystemGrid;
    signalsGrid = bodyGrid.map(v -> v != null ? new double[communicationSize] : null);
    fullInputsGrid = Grid.create(
        body.grid().w(),
        body.grid().h(),
        k -> new double[nOfInputs(body, k, nOfSignals, directional)]
    );
    fullOutputsGrid = Grid.create(
        body.grid().w(),
        body.grid().h(),
        k -> new double[nOfOutputs(body, k, nOfSignals, directional)]
    );
  }

  public static int nOfInputs(GridBody body, Grid.Key key, int nOfSignals, boolean directional) {
    return nOfInputs(body.grid().get(key).sensors(), nOfSignals, directional);
  }

  public static int nOfInputs(List<Sensor<? super Body>> sensors, int nOfSignals, boolean directional) {
    return sensors.size() + 4 * nOfSignals;
  }

  public static int nOfOutputs(GridBody body, Grid.Key key, int nOfSignals, boolean directional) {
    return nOfOutputs(body.grid().get(key).sensors(), nOfSignals, directional);
  }

  public static int nOfOutputs(List<Sensor<? super Body>> sensors, int nOfSignals, boolean directional) {
    int communicationSize = directional ? nOfSignals * 4 : nOfSignals;
    return communicationSize + 1;
  }

  @Override
  public List<BrainIO> brainIOs() {
    return getBody().grid()
        .stream()
        .filter(e -> !e.value().element().type().equals(GridBody.VoxelType.NONE))
        .map(
            e -> new BrainIO(
                new RangedValues(fullInputsGrid.get(e.key()), INPUT_RANGE),
                new RangedValues(fullOutputsGrid.get(e.key()), OUTPUT_RANGE)
            )
        )
        .toList();
  }

  @Override
  public List<NumericalDynamicalSystem<?>> brains() {
    return numericalDynamicalSystemGrid.values()
        .stream()
        .filter(Objects::nonNull)
        .toList();
  }

  @Override
  protected Grid<double[]> computeActuationValues(double t, Grid<double[]> inputsGrid) {
    // create actual input grid (concat sensed values and communication signals)
    for (Grid.Key key : inputsGrid.keys()) {
      if (inputsGrid.get(key) == null) {
        continue;
      }
      double[] sensoryInputs = inputsGrid.get(key);
      double[] signals0 = getLastSignals(key.x(), key.y() + 1, 0);
      double[] signals1 = getLastSignals(key.x() + 1, key.y(), 1);
      double[] signals2 = getLastSignals(key.x(), key.y() - 1, 2);
      double[] signals3 = getLastSignals(key.x() - 1, key.y(), 3);
      double[] fullInputs = Stream.of(sensoryInputs, signals0, signals1, signals2, signals3)
          .flatMapToDouble(Arrays::stream)
          .toArray();
      fullInputsGrid.set(key, fullInputs);
    }
    // process values
    for (Grid.Key key : numericalDynamicalSystemGrid.keys()) {
      if (numericalDynamicalSystemGrid.get(key) == null) {
        continue;
      }
      double[] inputs = fullInputsGrid.get(key);
      if (inputs.length != numericalDynamicalSystemGrid.get(key).nOfInputs()) {
        throw new IllegalArgumentException(
            String.format(
                "Wrong number of inputs in position (%d,%d): %d expected, %d found",
                key.x(),
                key.y(),
                numericalDynamicalSystemGrid.get(key).nOfInputs(),
                inputs.length
            )
        );
      }
      fullOutputsGrid.set(key, numericalDynamicalSystemGrid.get(key).step(t, inputs));
    }
    // split actuation and communication for next step
    Grid<double[]> outputsGrid = Grid.create(inputsGrid.w(), inputsGrid.h(), new double[4]);
    for (Grid.Key key : fullOutputsGrid.keys()) {
      if (fullOutputsGrid.get(key) == null) {
        continue;
      }
      double[] fullOutputs = fullOutputsGrid.get(key);
      double actuationValue = fullOutputs[0];
      double[] signals = Arrays.stream(fullOutputs, 1, fullOutputs.length).toArray();
      outputsGrid.set(key, new double[]{actuationValue, actuationValue, actuationValue, actuationValue});
      signalsGrid.set(key, signals);
    }
    return outputsGrid;
  }

  private double[] getLastSignals(int x, int y, int c) {
    if (x < 0 || y < 0 || x >= signalsGrid.w() || y >= signalsGrid.h() || signalsGrid.get(x, y) == null) {
      return new double[nOfSignals];
    }
    double[] allSignals = signalsGrid.get(x, y);
    return directional ? Arrays.stream(allSignals, c * nOfSignals, (c + 1) * nOfSignals)
        .toArray() : allSignals;
  }
}
