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

package it.units.erallab.mrsim.builders;

import it.units.erallab.mrsim.core.actions.Sense;
import it.units.erallab.mrsim.core.bodies.Voxel;
import it.units.erallab.mrsim.util.Grid;
import it.units.erallab.mrsim.util.builder.NamedBuilder;
import it.units.erallab.mrsim.util.builder.ParamMap;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

/**
 * @author "Eric Medvet" on 2022/08/11 for 2dmrsim
 */
public class VSRSensorizingFunctionBuilder extends NamedBuilder<Object> {
  private VSRSensorizingFunctionBuilder() {
    register(List.of("sensor","s"), VoxelSensorBuilder.getInstance());
    register("empty",VSRSensorizingFunctionBuilder::createEmpty);
    register("uniform",VSRSensorizingFunctionBuilder::createUniform);
    register("directional",VSRSensorizingFunctionBuilder::createDirectional);
  }

  private static Function<Grid<Boolean>, Grid<List<Function<Voxel, Sense<? super Voxel>>>>> createEmpty(
      ParamMap m,
      NamedBuilder<?> nb
  ) {
    return shape -> Grid.create(shape, b -> List.of());
  }

  @SuppressWarnings("unchecked")
  private static Function<Grid<Boolean>, Grid<List<Function<Voxel, Sense<? super Voxel>>>>> createUniform(
      ParamMap m,
      NamedBuilder<?> nb
  ) {
    return shape -> Grid.create(
        shape,
        b -> m.npms("sensors").stream()
            .map(sm -> (Function<Voxel, Sense<? super Voxel>>) nb.build(sm).orElseThrow())
            .toList()
    );
  }

  @SuppressWarnings("unchecked")
  private static Function<Grid<Boolean>, Grid<List<Function<Voxel, Sense<? super Voxel>>>>> createDirectional(
      ParamMap m,
      NamedBuilder<?> nb
  ) {
    List<Function<Voxel, Sense<? super Voxel>>> nSensors = m.npms("nSensors", List.of()).stream()
        .map(sm -> (Function<Voxel, Sense<? super Voxel>>) nb.build(sm).orElseThrow(() -> new IllegalArgumentException("No value for "+sm.getName())))
        .toList();
    List<Function<Voxel, Sense<? super Voxel>>> sSensors = m.npms("sSensors", List.of()).stream()
        .map(sm -> (Function<Voxel, Sense<? super Voxel>>) nb.build(sm).orElseThrow(() -> new IllegalArgumentException("No value for "+sm.getName())))
        .toList();
    List<Function<Voxel, Sense<? super Voxel>>> eSensors = m.npms("eSensors", List.of()).stream()
        .map(sm -> (Function<Voxel, Sense<? super Voxel>>) nb.build(sm).orElseThrow(() -> new IllegalArgumentException("No value for "+sm.getName())))
        .toList();
    List<Function<Voxel, Sense<? super Voxel>>> wSensors = m.npms("wSensors", List.of()).stream()
        .map(sm -> (Function<Voxel, Sense<? super Voxel>>) nb.build(sm).orElseThrow(() -> new IllegalArgumentException("No value for "+sm.getName())))
        .toList();
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
      List<Function<Voxel, Sense<? super Voxel>>> localSensors = new ArrayList<>();
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
      return localSensors;
    });
  }

  private final static VSRSensorizingFunctionBuilder INSTANCE = new VSRSensorizingFunctionBuilder();

  public static VSRSensorizingFunctionBuilder getInstance() {
    return INSTANCE;
  }

}
