/*-
 * ========================LICENSE_START=================================
 * mrsim2d-core
 * %%
 * Copyright (C) 2020 - 2024 Eric Medvet
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

import java.util.Arrays;
import java.util.stream.Collectors;

public record Path(Point[] points) {

  public Path(Point point) {
    this(new Point[] {point});
  }

  public Path add(Point point) {
    Point[] newPoints = new Point[points().length + 1];
    System.arraycopy(points, 0, newPoints, 0, points.length);
    newPoints[newPoints.length - 1] = point;
    return new Path(newPoints);
  }

  public Path add(double x, double y) {
    return add(new Point(x, y));
  }

  public Path add(Path other) {
    Path path = this;
    for (Point p : other.points) {
      path = path.add(p);
    }
    return path;
  }

  public Path moveBy(double x, double y) {
    return moveBy(new Point(x, y));
  }

  public Path moveBy(Point point) {
    return add(points[points.length - 1].sum(point));
  }

  public Path moveBy(Path other) {
    Path path = this;
    for (Point p : other.points) {
      path = path.add(p.sum(points[points.length - 1]));
    }
    return path;
  }

  public Poly toPoly() {
    return new Poly(points);
  }

  @Override
  public String toString() {
    return "[" + Arrays.stream(points).map(Point::toString).collect(Collectors.joining("->")) + "]";
  }
}
