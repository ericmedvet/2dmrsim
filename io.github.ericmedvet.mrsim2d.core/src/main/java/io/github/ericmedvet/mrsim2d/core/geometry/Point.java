
package io.github.ericmedvet.mrsim2d.core.geometry;

import java.util.Arrays;
public record Point(double x, double y) implements Shape {

  public static Point ORIGIN = new Point(0, 0);

  public Point(double direction) {
    this(Math.cos(direction), Math.sin(direction));
  }

  public static Point average(Point... points) {
    return new Point(
        Arrays.stream(points)
            .mapToDouble(Point::x)
            .average()
            .orElseThrow(() -> new IllegalArgumentException("There has to be at least one point")),
        Arrays.stream(points)
            .mapToDouble(Point::y)
            .average()
            .orElseThrow(() -> new IllegalArgumentException("There has to be at least one point"))
    );
  }

  public static Point max(Point... points) {
    return Arrays.stream(points).sequential()
        .reduce((p1, p2) -> new Point(
            Math.max(p1.x, p2.x),
            Math.max(p1.y, p2.y)
        ))
        .orElseThrow(() -> new IllegalArgumentException("There has to be at least one point"));
  }

  public static Point min(Point... points) {
    return Arrays.stream(points).sequential()
        .reduce((p1, p2) -> new Point(
            Math.min(p1.x, p2.x),
            Math.min(p1.y, p2.y)
        ))
        .orElseThrow(() -> new IllegalArgumentException("There has to be at least one point"));
  }

  public double angle(Point p) {
    return Math.acos((x * p.x() + y * p.y()) / magnitude() / p.magnitude());
  }

  @Override
  public BoundingBox boundingBox() {
    return new BoundingBox(this, this);
  }

  @Override
  public double area() {
    return 0d;
  }

  @Override
  public Point center() {
    return this;
  }

  public Point diff(Point p) {
    return new Point(x - p.x(), y - p.y());
  }

  public double direction() {
    return Math.atan2(y, x);
  }

  public double distance(Point p) {
    return diff(p).magnitude();
  }

  public double magnitude() {
    return Math.sqrt(x * x + y * y);
  }

  public Point scale(double r) {
    return new Point(r * x, r * y);
  }

  public Point sum(Point p) {
    return new Point(x + p.x(), y + p.y());
  }

  @Override
  public String toString() {
    return String.format("(%.1f;%.1f)", x, y);
  }

}
