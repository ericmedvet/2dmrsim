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

package it.units.erallab.mrsim.util.builder;

import it.units.erallab.mrsim.agents.gridvsr.NumGridVSR;
import it.units.erallab.mrsim.core.actions.Sense;
import it.units.erallab.mrsim.core.bodies.Voxel;
import it.units.erallab.mrsim.util.Grid;
import it.units.erallab.mrsim.util.Pair;

import java.util.List;
import java.util.function.Function;

/**
 * @author "Eric Medvet" on 2022/08/11 for 2dmrsim
 */
public class GridVSRBodyBuilder extends NamedBuilder<Object> {
  private GridVSRBodyBuilder() {
    register(List.of("shape", "s"), GridShapeBuilder.getInstance());
    register(List.of("sensorizingFunction", "sf"), VSRSensorizingFunctionBuilder.getInstance());
    register("plain", GridVSRBodyBuilder::createBody);
  }

  @SuppressWarnings("unchecked")
  private static NumGridVSR.Body createBody(ParamMap m, NamedBuilder<?> nb) {
    Voxel.Material material = new Voxel.Material();
    Grid<Boolean> shape = (Grid<Boolean>) nb.build(m.npm("shape")).orElseThrow(() -> new IllegalArgumentException(
        "No value for shape"));
    Function<Grid<Boolean>, Grid<List<Function<Voxel, Sense<? super Voxel>>>>> sensorizingFunction =
        (Function<Grid<Boolean>, Grid<List<Function<Voxel, Sense<? super Voxel>>>>>) nb.build(
            m.npm("sensorizingFunction")).orElseThrow(() -> new IllegalArgumentException(
            "No value for sensorizingFunction"));
    Grid<List<Function<Voxel, Sense<? super Voxel>>>> sensors = sensorizingFunction.apply(shape);
    return new NumGridVSR.Body(Grid.create(
        shape.w(),
        shape.h(),
        (x, y) -> new Pair<>(shape.get(x, y) ? material : null, shape.get(x, y) ? sensors.get(x, y) : null)
    ));
  }

  private final static GridVSRBodyBuilder INSTANCE = new GridVSRBodyBuilder();

  public static GridVSRBodyBuilder getInstance() {
    return INSTANCE;
  }


}
