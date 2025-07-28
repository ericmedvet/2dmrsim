/*-
 * ========================LICENSE_START=================================
 * mrsim2d-buildable
 * %%
 * Copyright (C) 2020 - 2025 Eric Medvet
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

package io.github.ericmedvet.mrsim2d.buildable.builders;

import io.github.ericmedvet.jnb.core.Discoverable;
import io.github.ericmedvet.jnb.core.Param;
import io.github.ericmedvet.jnb.datastructure.Grid;
import io.github.ericmedvet.mrsim2d.core.agents.gridvsr.GridBody;
import io.github.ericmedvet.mrsim2d.core.agents.gridvsr.GridBody.VoxelType;

@Discoverable(prefixTemplate = "sim|s.agent|a.vsr.shape|s")
public class GridShapes {

  private GridShapes() {
  }

  @SuppressWarnings("unused")
  public static Grid<VoxelType> ball(@Param("d") Integer d) {
    return Grid.create(
        d,
        d,
        (x, y) -> Math.round(
            Math.sqrt((x - (d - 1) / 2d) * (x - (d - 1) / 2d) + (y - (d - 1) / 2d) * (y - (d - 1) / 2d))
        ) <= (int) Math.floor(d / 2d) ? GridBody.VoxelType.SOFT : GridBody.VoxelType.NONE
    );
  }

  @SuppressWarnings("unused")
  public static Grid<GridBody.VoxelType> biped(@Param("w") Integer w, @Param("h") Integer h) {
    return Grid.create(
        w,
        h,
        (x, y) -> !(y < h / 2 && x >= w / 4 && x < w * 3 / 4) ? GridBody.VoxelType.SOFT : GridBody.VoxelType.NONE
    );
  }

  @SuppressWarnings("unused")
  public static Grid<GridBody.VoxelType> comb(@Param("w") Integer w, @Param("h") Integer h) {
    return Grid.create(
        w,
        h,
        (x, y) -> (y >= h / 2 || x % 2 == 0) ? GridBody.VoxelType.SOFT : GridBody.VoxelType.NONE
    );
  }

  @SuppressWarnings("unused")
  public static Grid<GridBody.VoxelType> free(@Param(value = "s", dS = "rsr-s.s") String s) {
    String innerS = s.toLowerCase();
    return Grid.create(
        s.split("-")[0].length(),
        s.split("-").length,
        (x, y) -> switch (innerS.split("-")[innerS.split("-").length - y - 1].charAt(x)) {
          case 's', '1' -> GridBody.VoxelType.SOFT;
          case 'r' -> GridBody.VoxelType.RIGID;
          default -> GridBody.VoxelType.NONE;
        }
    );
  }

  @SuppressWarnings("unused")
  public static Grid<GridBody.VoxelType> t(@Param("w") Integer w, @Param("h") Integer h) {
    int pad = (int) Math.floor((Math.floor((double) w / 2) / 2));
    return Grid.create(
        w,
        h,
        (x, y) -> (y == 0 || (x >= pad && x < h - pad - 1)) ? GridBody.VoxelType.SOFT : GridBody.VoxelType.NONE
    );
  }

  @SuppressWarnings("unused")
  public static Grid<GridBody.VoxelType> triangle(@Param("l") Integer l) {
    return Grid.create(l, l, (x, y) -> (y >= x) ? GridBody.VoxelType.SOFT : GridBody.VoxelType.NONE);
  }

  @SuppressWarnings("unused")
  public static Grid<GridBody.VoxelType> tripod(@Param("w") Integer w, @Param("h") Integer h) {
    return Grid.create(
        w,
        h,
        (x, y) -> !(y < h / 2 && x != 0 && x != w - 1 && x != w / 2) ? GridBody.VoxelType.SOFT : GridBody.VoxelType.NONE
    );
  }

  @SuppressWarnings("unused")
  public static Grid<GridBody.VoxelType> worm(@Param("w") Integer w, @Param("h") Integer h) {
    return Grid.create(w, h, GridBody.VoxelType.SOFT);
  }
}
