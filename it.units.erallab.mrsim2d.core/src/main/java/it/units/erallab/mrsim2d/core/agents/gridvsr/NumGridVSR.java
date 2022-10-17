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


import it.units.erallab.mrsim2d.core.Action;
import it.units.erallab.mrsim2d.core.ActionOutcome;
import it.units.erallab.mrsim2d.core.Sensor;
import it.units.erallab.mrsim2d.core.actions.ActuateVoxel;
import it.units.erallab.mrsim2d.core.actions.Sense;
import it.units.erallab.mrsim2d.core.bodies.Voxel;
import it.units.erallab.mrsim2d.core.util.DoubleRange;
import it.units.erallab.mrsim2d.core.util.Grid;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Function;

/**
 * @author "Eric Medvet" on 2022/07/09 for 2dmrsim
 */
public class NumGridVSR extends AbstractGridVSR {

  private final Grid<List<Sensor<? super Voxel>>> sensorsGrid;
  protected final Grid<double[]> inputsGrid;
  protected final Grid<Double> outputGrid;
  private final GridBody body;
  protected final BiFunction<Double, Grid<double[]>, Grid<double[]>> timedFunction;

  public NumGridVSR(
      GridBody body,
      double voxelSideLength,
      double voxelMass,
      BiFunction<Double, Grid<double[]>, Grid<double[]>> timedFunction
  ) {
    super(body.materialGrid(), voxelSideLength, voxelMass);
    this.sensorsGrid = body.sensorsGrid();
    this.body = body;
    inputsGrid = sensorsGrid.map(l -> l != null ? new double[l.size()] : null);
    outputGrid = voxelGrid.map(v -> v != null ? 0d : null);
    this.timedFunction = timedFunction;
  }

  public NumGridVSR(GridBody body, BiFunction<Double, Grid<double[]>, Grid<double[]>> timedFunction) {
    this(body, VOXEL_SIDE_LENGTH, VOXEL_MASS, timedFunction);
  }

  @SuppressWarnings("unchecked")
  @Override
  public List<? extends Action<?>> act(double t, List<ActionOutcome<?, ?>> previousActionOutcomes) {
    //read inputs from last request
    if (!previousActionOutcomes.isEmpty()) {
      int c = 0;
      for (Grid.Key key : inputsGrid.keys()) {
        double[] inputs = inputsGrid.get(key);
        if (inputs != null) {
          for (int i = 0; i < inputs.length; i++) {
            ActionOutcome<?, ?> outcome = previousActionOutcomes.get(c);
            if (outcome.action() instanceof Sense<?>) {
              ActionOutcome<? extends Sense<Voxel>, Double> o = (ActionOutcome<? extends Sense<Voxel>, Double>) outcome;
              inputs[i] = DoubleRange.SYMMETRIC_UNIT.denormalize(
                  o.action().range().normalize(o.outcome().orElse(0d))
              );
              c = c + 1;
            }
          }
        }
      }
    }
    computeActuationValues(t);
    //generate next sense actions
    List<Action<?>> actions = new ArrayList<>();
    actions.addAll(voxelGrid.entries().stream()
        .filter(e -> e.value() != null)
        .map(e -> sensorsGrid.get(e.key()).stream()
            .map(f -> f.apply(e.value()))
            .toList())
        .flatMap(Collection::stream)
        .toList());
    //generate actuation actions
    actions.addAll(voxelGrid.entries().stream()
        .filter(e -> e.value() != null)
        .map(e -> new ActuateVoxel(e.value(), outputGrid.get(e.key())))
        .toList());
    return actions;
  }

  protected void computeActuationValues(double t) {
    timedFunction.apply(t, inputsGrid).entries().forEach(e -> outputGrid.set(e.key(), e.value()[0]));
  }

  public GridBody getBody() {
    return body;
  }

  public BiFunction<Double, Grid<double[]>, Grid<double[]>> getTimedFunction() {
    return timedFunction;
  }
}
