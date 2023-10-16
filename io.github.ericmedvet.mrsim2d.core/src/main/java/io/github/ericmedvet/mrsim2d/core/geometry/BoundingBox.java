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

import io.github.ericmedvet.jsdynsym.core.DoubleRange;
import java.util.Arrays;

public record BoundingBox(Point min, Point max) implements Shape {

  public enum Anchor {
    LL,
    CL,
    UL,
    LC,
    CC,
    UC,
    LU,
    CU,
    UU
  }

  public static BoundingBox enclosing(BoundingBox... boxes) {
    return Arrays.stream(boxes)
        .sequential()
        .reduce((b1, b2) -> new BoundingBox(Point.min(b1.min, b2.min), Point.max(b1.max, b2.max)))
        .orElseThrow(
            () -> new IllegalArgumentException("There has to be at least one bounding box"));
  }

  @Override
  public BoundingBox boundingBox() {
    return this;
  }

  @Override
  public double area() {
    return (max().x() - min().x()) * (max().y()) - min().y();
  }

  @Override
  public Point center() {
    return Point.average(min, max);
  }

  public double height() {
    return max.y() - min.y();
  }

  public double width() {
    return max.x() - min.x();
  }

  public DoubleRange xRange() {
    return new DoubleRange(min.x(), max.x());
  }

  public DoubleRange yRange() {
    return new DoubleRange(min.y(), max.y());
  }

  public Point anchor(Anchor anchor) {
    return switch (anchor) {
      case LL -> min;
      case LC -> new Point(min.x(), (min.y() + max().y()) / 2d);
      case LU -> new Point(min().x(), max.y());
      case CL -> new Point((min.x() + max.x()) / 2d, min.y());
      case CC -> center();
      case CU -> new Point((min.x() + max.x()) / 2d, max.y());
      case UL -> new Point(max.x(), min.y());
      case UC -> new Point(max.x(), (min.y() + max().y()) / 2d);
      case UU -> max;
    };
  }
}
