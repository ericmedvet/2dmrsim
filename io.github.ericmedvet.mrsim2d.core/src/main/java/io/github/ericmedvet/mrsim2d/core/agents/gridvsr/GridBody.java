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

package io.github.ericmedvet.mrsim2d.core.agents.gridvsr;

import io.github.ericmedvet.mrsim2d.core.Sensor;
import io.github.ericmedvet.mrsim2d.core.bodies.Body;
import io.github.ericmedvet.mrsim2d.core.bodies.Voxel;
import io.github.ericmedvet.mrsim2d.core.util.Grid;

import java.util.List;
import java.util.function.Function;

public record GridBody(Grid<SensorizedElement> grid) {

  public GridBody(
      Grid<VoxelType> shape,
      Function<Grid<Boolean>, Grid<List<Sensor<? super Body>>>> sensorizingFunction
  ) {
    this(Grid.create(
        shape.w(),
        shape.h(),
        k -> new SensorizedElement(
            new Element(
                shape.get(k),
                new Voxel.Material()
            ),
            !shape.get(k).equals(VoxelType.NONE) ? sensorizingFunction.apply(shape.map(t -> !t.equals(VoxelType.NONE)))
                .get(k) : List.of()
        )
    ));
  }

  public enum VoxelType {NONE, SOFT, RIGID}

  public record Element(VoxelType type, Voxel.Material material) {}

  public record SensorizedElement(Element element, List<Sensor<? super Body>> sensors) {}

}
