
package io.github.ericmedvet.mrsim2d.core.geometry;

import io.github.ericmedvet.jsdynsym.core.DoubleRange;

import java.util.Arrays;
public record BoundingBox(Point min, Point max) implements Shape {

  public enum Anchor {LL, CL, UL, LC, CC, UC, LU, CU, UU}

  public static BoundingBox enclosing(BoundingBox... boxes) {
    return Arrays.stream(boxes).sequential()
        .reduce((b1, b2) -> new BoundingBox(
            Point.min(b1.min, b2.min),
            Point.max(b1.max, b2.max)
        ))
        .orElseThrow(() -> new IllegalArgumentException("There has to be at least one bounding box"));
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
