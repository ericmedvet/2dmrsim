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

package it.units.erallab.mrsim2d.core.builders;

import it.units.erallab.mrsim2d.builder.Param;
import it.units.erallab.mrsim2d.core.Sensor;
import it.units.erallab.mrsim2d.core.bodies.Voxel;
import it.units.erallab.mrsim2d.core.util.Grid;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.function.Function;

/**
 * @author "Eric Medvet" on 2022/08/11 for 2dmrsim
 */
public class VSRSensorizingFunctionBuilder {

  public static Function<Grid<Boolean>, Grid<List<Sensor<? super Voxel>>>> directional(
      @Param(value = "nSensors") List<Sensor<? super Voxel>> nSensors,
      @Param(value = "eSensors") List<Sensor<? super Voxel>> eSensors,
      @Param(value = "sSensors") List<Sensor<? super Voxel>> sSensors,
      @Param(value = "wSensors") List<Sensor<? super Voxel>> wSensors,
      @Param(value = "headSensors") List<Sensor<? super Voxel>> headSensors
  ) {
    return shape -> Grid.create(shape.w(), shape.h(), (Integer x, Integer y) -> {
      if (!shape.get(x, y)) {
        return null;
      }
      int maxX = shape.entries()
          .stream()
          .filter(e -> e.key().y() == y && e.value())
          .mapToInt(e -> e.key().x())
          .max()
          .orElse(0);
      int minX = shape.entries()
          .stream()
          .filter(e -> e.key().y() == y && e.value())
          .mapToInt(e -> e.key().x())
          .min()
          .orElse(0);
      Grid.Key headKey = shape.entries().stream()
          .filter(Grid.Entry::value)
          .sorted(Comparator.comparingInt(e -> -e.key().x() - e.key().y()))
          .limit(1)
          .toList()
          .get(0)
          .key();
      List<Sensor<? super Voxel>> localSensors = new ArrayList<>();
      if (x == maxX) {
        localSensors.addAll(eSensors);
      }
      if (x == minX) {
        localSensors.addAll(wSensors);
      }
      if (y == 0) {
        localSensors.addAll(sSensors);
      }
      if (y == shape.h() - 1) {
        localSensors.addAll(nSensors);
      }
      if (x == headKey.x() && y == headKey.y()) {
        localSensors.addAll(headSensors);
      }
      return localSensors;
    });
  }

  public static Function<Grid<Boolean>, Grid<List<Sensor<? super Voxel>>>> empty() {
    return shape -> Grid.create(shape, b -> List.of());
  }

  public static Function<Grid<Boolean>, Grid<List<Sensor<? super Voxel>>>> uniform(
      @Param(value = "sensors") List<Sensor<? super Voxel>> sensors
  ) {
    return shape -> Grid.create(
        shape,
        b -> sensors
    );
  }

}
