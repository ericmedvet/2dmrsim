
package io.github.ericmedvet.mrsim2d.core.geometry;
public interface Shape {
  BoundingBox boundingBox();

  default double area() {
    return boundingBox().area();
  }

  default Point center() {
    return boundingBox().center();
  }
}
