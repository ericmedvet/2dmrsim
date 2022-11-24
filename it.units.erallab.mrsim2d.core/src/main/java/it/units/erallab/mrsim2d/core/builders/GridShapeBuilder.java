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

package it.units.erallab.mrsim2d.core.builders;


import it.units.erallab.mrsim2d.core.util.Grid;
import it.units.malelab.jnb.core.Param;

/**
 * @author "Eric Medvet" on 2022/08/11 for 2dmrsim
 */
public class GridShapeBuilder {

  public static Grid<Boolean> ball(@Param("d") Integer d) {
    return Grid.create(
        d,
        d,
        (x, y) -> Math.round(Math.sqrt((x - (d - 1) / 2d) * (x - (d - 1) / 2d) + (y - (d - 1) / 2d) * (y - (d - 1) / 2d))) <= (int) Math.floor(
            d / 2d)
    );
  }

  public static Grid<Boolean> biped(@Param("w") Integer w, @Param("h") Integer h) {
    return Grid.create(w, h, (x, y) -> !(y < h / 2 && x >= w / 4 && x < w * 3 / 4));
  }

  public static Grid<Boolean> comb(@Param("w") Integer w, @Param("h") Integer h) {
    return Grid.create(w, h, (x, y) -> (y >= h / 2 || x % 2 == 0));
  }

  public static Grid<Boolean> free(@Param(value = "s", dS = "111-101") String s) {
    return Grid.create(
        s.split("-")[0].length(),
        s.split("-").length,
        (x, y) -> s.split("-")[s.split("-").length - y - 1].charAt(x) == '1'
    );
  }

  public static Grid<Boolean> t(@Param("w") Integer w, @Param("h") Integer h) {
    int pad = (int) Math.floor((Math.floor((double) w / 2) / 2));
    return Grid.create(w, h, (x, y) -> (y == 0 || (x >= pad && x < h - pad - 1)));
  }

  public static Grid<Boolean> triangle(@Param("l") Integer l) {
    return Grid.create(l, l, (x, y) -> (y >= x));
  }

  public static Grid<Boolean> tripod(@Param("w") Integer w, @Param("h") Integer h) {
    return Grid.create(w, h, (x, y) -> !(y < h / 2 && x != 0 && x != w - 1 && x != w / 2));
  }

  public static Grid<Boolean> worm(@Param("w") Integer w, @Param("h") Integer h) {
    return Grid.create(w, h, true);
  }

}
