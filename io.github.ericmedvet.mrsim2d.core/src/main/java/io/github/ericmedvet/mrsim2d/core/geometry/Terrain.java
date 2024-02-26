/*-
 * ========================LICENSE_START=================================
 * mrsim2d-core
 * %%
 * Copyright (C) 2020 - 2023 Eric Medvet
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

package io.github.ericmedvet.mrsim2d.core.geometry;

import io.github.ericmedvet.jnb.datastructure.DoubleRange;
import io.github.ericmedvet.mrsim2d.core.util.PolyUtils;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public record Terrain(Poly poly, DoubleRange withinBordersXRange) {

  public double maxHeightAt(DoubleRange xRange) {
    List<Double> xs = new ArrayList<>(Arrays.stream(poly().vertexes())
        .filter(v -> v.x() >= xRange.min() && v.x() <= xRange.max())
        .map(Point::x)
        .toList());
    xs.add(xRange.min());
    xs.add(xRange.max());
    return xs.stream()
        .distinct()
        .mapToDouble(x -> PolyUtils.maxYAtX(poly, x))
        .filter(d -> !Double.isNaN(d))
        .max()
        .orElseThrow(() -> new IllegalArgumentException(
            "Cannot find a terrain max y in range %.1f, %.1f.".formatted(xRange.min(), xRange.max())));
  }

  public static Terrain fromPath(Path partialPath, double terrainH, double borderW, double borderH) {
    Path path = new Path(Point.ORIGIN)
        .moveBy(0, borderH)
        .moveBy(borderW, 0)
        .moveBy(0, -borderH)
        .moveBy(partialPath)
        .moveBy(0, borderH)
        .moveBy(borderW, 0)
        .moveBy(0, -borderH);
    double maxX = Arrays.stream(path.points()).mapToDouble(Point::x).max().orElse(borderW);
    double minY = Arrays.stream(path.points()).mapToDouble(Point::y).min().orElse(borderW);
    path = path.add(maxX, minY - terrainH).moveBy(-maxX, 0);
    return new Terrain(path.toPoly(), new DoubleRange(borderW, maxX - borderW));
  }
}
