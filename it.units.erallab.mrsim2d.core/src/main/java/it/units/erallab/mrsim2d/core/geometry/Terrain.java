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

package it.units.erallab.mrsim2d.core.geometry;

import it.units.erallab.mrsim2d.core.util.DoubleRange;
import it.units.erallab.mrsim2d.core.util.PolyUtils;

import java.util.Arrays;

public record Terrain(Poly poly, DoubleRange withinBordersXRange) {

  public double maxHeightAt(DoubleRange xRange) {
    return Arrays.stream(poly().vertexes())
        .filter(v -> v.x() >= xRange.min() && v.x() <= xRange.max())
        .mapToDouble(v -> PolyUtils.maxYAtX(poly, v.x()))
        .max()
        .orElseThrow(() -> new IllegalArgumentException("Cannot find a terrain max y in range %.1f, %.1f.".formatted(
            xRange.min(),
            xRange.max()))
        );
  }
}
