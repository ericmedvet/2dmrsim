package it.units.erallab.mrsim.core.geometry;

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

  public double length() {
    return p1.distance(p2);
  }
}
