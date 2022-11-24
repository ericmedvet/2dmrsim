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

import it.units.erallab.mrsim2d.core.Sensor;
import it.units.erallab.mrsim2d.core.bodies.Voxel;
import it.units.erallab.mrsim2d.core.util.Grid;
import it.units.erallab.mrsim2d.core.util.Pair;
import it.units.malelab.jnb.core.BuilderMethod;
import it.units.malelab.jnb.core.Param;

import java.util.List;
import java.util.function.Function;

public record GridBody(Grid<Pair<Voxel.Material, List<Sensor<? super Voxel>>>> grid) {
  @BuilderMethod()
  public GridBody(
      @Param("shape") Grid<Boolean> shape,
      @Param("sensorizingFunction") Function<Grid<Boolean>,
          Grid<List<Sensor<? super Voxel>>>> sensorizingFunction
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

  public Grid<List<Sensor<? super Voxel>>> sensorsGrid() {
    return Grid.create(grid, Pair::second);
  }
}
