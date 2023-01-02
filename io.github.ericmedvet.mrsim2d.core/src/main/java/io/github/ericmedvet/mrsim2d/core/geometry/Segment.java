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

package io.github.ericmedvet.mrsim2d.core.geometry;

import java.util.Optional;

public record Segment(Point p1, Point p2) implements Shape {
  @Override
  public BoundingBox boundingBox() {
    return new BoundingBox(
        new Point(Math.min(p1.x(), p2().x()), Math.min(p1.y(), p2().y())),
        new Point(Math.max(p1.x(), p2().x()), Math.max(p1.y(), p2().y()))
    );
  }

  @Override
  public double area() {
    return 0;
  }

  @Override
  public Point center() {
    return Point.average(p1, p2);
  }

  public double direction() {
    return p2.diff(p1).direction();
  }

  public double length() {
    return p1.distance(p2);
  }

  public Point pointAtDistance(double d) {
    return p1.sum(new Point(direction()).scale(d));
  }

  public Point pointAtRate(double r) {
    return new Point(
        p1.x() + (p2.x() - p1.x()) * r,
        p1.y() + (p2.y() - p1.y()) * r
    );
  }

  public Optional<Double> xAt(double y) {
    BoundingBox bb = boundingBox();
    if (bb.yRange().contains(y)) {
      return Optional.of(bb.xRange().denormalize(bb.yRange().normalize(y)));
    }
    return Optional.empty();
  }

  public Optional<Double> yAt(double x) {
    BoundingBox bb = boundingBox();
    if (bb.xRange().contains(x)) {
      return Optional.of(bb.yRange().denormalize(bb.xRange().normalize(x)));
    }
    return Optional.empty();
  }
}
