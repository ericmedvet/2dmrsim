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
import it.units.erallab.mrsim2d.core.Action;
import it.units.erallab.mrsim2d.core.ActionOutcome;
import it.units.erallab.mrsim2d.core.actions.ActuateVoxel;
import it.units.erallab.mrsim2d.core.actions.Sense;
import it.units.erallab.mrsim2d.core.bodies.Voxel;
import it.units.erallab.mrsim2d.core.util.DoubleRange;
import it.units.erallab.mrsim2d.core.util.Grid;
import it.units.erallab.mrsim2d.core.util.Pair;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Function;

/**
 * @author "Eric Medvet" on 2022/07/09 for 2dmrsim
 */
public class NumGridVSR extends AbstractGridVSR {

  private final Grid<List<Function<? super Voxel, Sense<? super Voxel>>>> sensorsGrid;
  private final Grid<double[]> inputsGrid;
  private final Grid<Double> outputGrid;
  private final Body body;
  private BiFunction<Double, Grid<double[]>, Grid<Double>> timedFunction;

  public NumGridVSR(
      Body body,
      double voxelSideLength,
      double voxelMass
  ) {
    super(body.materialGrid(), voxelSideLength, voxelMass);
    this.sensorsGrid = body.sensorsGrid();
    this.body = body;
    inputsGrid = sensorsGrid.map(l -> l != null ? new double[l.size()] : null);
    outputGrid = voxelGrid.map(v -> v != null ? 0d : null);
  }

  public NumGridVSR(Body body) {
    this(body, VOXEL_SIDE_LENGTH, VOXEL_MASS);
  }

  public NumGridVSR(Body body, BiFunction<Double, Grid<double[]>, Grid<Double>> timedFunction) {
    this(body);
    setTimedFunction(timedFunction);
  }

  public record Body(Grid<Pair<Voxel.Material, List<Function<? super Voxel, Sense<? super Voxel>>>>> grid) {
    @BuilderMethod()
    public Body(
        @Param("shape") Grid<Boolean> shape,
        @Param("sensorizingFunction") Function<Grid<Boolean>,
            Grid<List<Function<? super Voxel, Sense<? super Voxel>>>>> sensorizingFunction
    ) {
      this(Grid.create(
          shape.w(),
          shape.h(),
          (x, y) -> new Pair<>(
              shape.get(x, y) ? new Voxel.Material() : null,
              shape.get(x, y) ? sensorizingFunction.apply(shape).get(x, y) : null
          )
      ));
    }

    public Grid<Voxel.Material> materialGrid() {
      return Grid.create(grid, Pair::first);
    }

    public Grid<List<Function<? super Voxel, Sense<? super Voxel>>>> sensorsGrid() {
      return Grid.create(grid, Pair::second);
    }
  }

  @SuppressWarnings("unchecked")
  @Override
  public List<Action<?>> act(double t, List<ActionOutcome<?, ?>> previousActionOutcomes) {
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
    //compute actuation
    timedFunction.apply(t, inputsGrid).entries().forEach(e -> outputGrid.set(e.key(), e.value()));
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

  public Body getBody() {
    return body;
  }

  public BiFunction<Double, Grid<double[]>, Grid<Double>> getTimedFunction() {
    return timedFunction;
  }

  public void setTimedFunction(BiFunction<Double, Grid<double[]>, Grid<Double>> timedFunction) {
    this.timedFunction = timedFunction;
  }
}
