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

package it.units.erallab.mrsim.agents.gridvsr;

import it.units.erallab.mrsim.util.Grid;
import it.units.erallab.mrsim.util.StringUtils;

import java.util.Map;

/**
 * @author "Eric Medvet" on 2022/07/09 for 2dmrsim
 */
public class ShapeUtils {
  private ShapeUtils() {
  }

  public static Grid<Boolean> buildShape(String name) {
    String box = "(box|worm)-(?<w>\\d+)x(?<h>\\d+)";
    String biped = "biped-(?<w>\\d+)x(?<h>\\d+)";
    String tripod = "tripod-(?<w>\\d+)x(?<h>\\d+)";
    String ball = "ball-(?<d>\\d+)";
    String comb = "comb-(?<w>\\d+)x(?<h>\\d+)";
    String t = "t-(?<w>\\d+)x(?<h>\\d+)";
    String free = "free-(?<s>[01-]+)";
    String triangle = "triangle-(?<l>\\d+)";
    Map<String, String> params;
    if ((params = StringUtils.params(box, name)) != null) {
      int w = Integer.parseInt(params.get("w"));
      int h = Integer.parseInt(params.get("h"));
      return Grid.create(w, h, true);
    }
    if ((params = StringUtils.params(biped, name)) != null) {
      int w = Integer.parseInt(params.get("w"));
      int h = Integer.parseInt(params.get("h"));
      return Grid.create(w, h, (x, y) -> !(y < h / 2 && x >= w / 4 && x < w * 3 / 4));
    }
    if ((params = StringUtils.params(tripod, name)) != null) {
      int w = Integer.parseInt(params.get("w"));
      int h = Integer.parseInt(params.get("h"));
      return Grid.create(w, h, (x, y) -> !(y < h / 2 && x != 0 && x != w - 1 && x != w / 2));
    }
    if ((params = StringUtils.params(ball, name)) != null) {
      int d = Integer.parseInt(params.get("d"));
      return Grid.create(
          d,
          d,
          (x, y) -> Math.round(Math.sqrt((x - (d - 1) / 2d) * (x - (d - 1) / 2d) + (y - (d - 1) / 2d) * (y - (d - 1) / 2d))) <= (int) Math.floor(
              d / 2d)
      );
    }
    if ((params = StringUtils.params(comb, name)) != null) {
      int w = Integer.parseInt(params.get("w"));
      int h = Integer.parseInt(params.get("h"));
      return Grid.create(w, h, (x, y) -> (y >= h / 2 || x % 2 == 0));
    }
    if ((params = StringUtils.params(t, name)) != null) {
      int w = Integer.parseInt(params.get("w"));
      int h = Integer.parseInt(params.get("h"));
      int pad = (int) Math.floor((Math.floor((double) w / 2) / 2));
      return Grid.create(w, h, (x, y) -> (y == 0 || (x >= pad && x < h - pad - 1)));
    }
    if ((params = StringUtils.params(free, name)) != null) {
      String s = params.get("s");
      return Grid.create(
          s.split("-").length,
          s.split("-")[0].length(),
          (x, y) -> s.split("-")[x].charAt(y) == '1'
      );
    }
    if ((params = StringUtils.params(triangle, name)) != null) {
      int l = Integer.parseInt(params.get("l"));
      return Grid.create(l, l, (x, y) -> (y >= x));
    }
    throw new IllegalArgumentException(String.format("Unknown body name: %s", name));
  }

}
