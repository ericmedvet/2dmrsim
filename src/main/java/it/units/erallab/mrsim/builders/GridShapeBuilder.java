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

import it.units.erallab.mrsim.util.Grid;
import it.units.erallab.mrsim.util.builder.NamedBuilder;
import it.units.erallab.mrsim.util.builder.ParamMap;

/**
 * @author "Eric Medvet" on 2022/08/11 for 2dmrsim
 */
public class GridShapeBuilder extends NamedBuilder<Grid<Boolean>> {
  private GridShapeBuilder() {
    register("worm", GridShapeBuilder::createWorm);
    register("biped", GridShapeBuilder::createBiped);
    register("tripod", GridShapeBuilder::createTripod);
    register("ball", GridShapeBuilder::createBall);
    register("comb", GridShapeBuilder::createComb);
    register("t", GridShapeBuilder::createT);
    register("free", GridShapeBuilder::createFree);
    register("triangle", GridShapeBuilder::createTriangle);
  }

  private final static GridShapeBuilder INSTANCE = new GridShapeBuilder();

  public static GridShapeBuilder getInstance() {
    return INSTANCE;
  }

  private static Grid<Boolean> createWorm(ParamMap m, NamedBuilder<?> nb) {
    return Grid.create(m.i("w"), m.i("h"), true);
  }

  private static Grid<Boolean> createBiped(ParamMap m, NamedBuilder<?> nb) {
    return Grid.create(
        m.i("w"),
        m.i("h"),
        (x, y) -> !(y < m.i("h") / 2 && x >= m.i("w") / 4 && x < m.i("w") * 3 / 4)
    );
  }

  private static Grid<Boolean> createTripod(ParamMap m, NamedBuilder<?> nb) {
    return Grid.create(
        m.i("w"),
        m.i("h"),
        (x, y) -> !(y < m.i("h") / 2 && x != 0 && x != m.i("w") - 1 && x != m.i("w") / 2)
    );
  }

  private static Grid<Boolean> createBall(ParamMap m, NamedBuilder<?> nb) {
    return Grid.create(
        m.i("d"),
        m.i("d"),
        (x, y) -> Math.round(Math.sqrt((x - (m.i("d") - 1) / 2d) * (x - (m.i("d") - 1) / 2d) + (y - (m.i("d") - 1) / 2d) * (y - (m.i(
            "d") - 1) / 2d))) <= (int) Math.floor(
            m.i("d") / 2d)
    );
  }

  private static Grid<Boolean> createComb(ParamMap m, NamedBuilder<?> nb) {
    return Grid.create(m.i("w"), m.i("h"), (x, y) -> (y >= m.i("h") / 2 || x % 2 == 0));
  }

  private static Grid<Boolean> createT(ParamMap m, NamedBuilder<?> nb) {
    int w = m.i("w");
    int h = m.i("h");
    int pad = (int) Math.floor((Math.floor((double) w / 2) / 2));
    return Grid.create(w, h, (x, y) -> (y == 0 || (x >= pad && x < h - pad - 1)));
  }

  private static Grid<Boolean> createFree(ParamMap m, NamedBuilder<?> nb) {
    String s = m.fs("s", "[01]+(-[01]+)?");
    return Grid.create(
        s.split("-").length,
        s.split("-")[0].length(),
        (x, y) -> s.split("-")[x].charAt(y) == '1'
    );
  }

  private static Grid<Boolean> createTriangle(ParamMap m, NamedBuilder<?> nb) {
    return Grid.create(m.i("l"), m.i("l"), (x, y) -> (y >= x));
  }

}
