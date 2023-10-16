
package io.github.ericmedvet.mrsim2d.core;

import io.github.ericmedvet.mrsim2d.core.bodies.Body;
import io.github.ericmedvet.mrsim2d.core.engine.ActionException;
import io.github.ericmedvet.mrsim2d.core.geometry.BoundingBox;
import io.github.ericmedvet.mrsim2d.core.geometry.Point;
import io.github.ericmedvet.mrsim2d.core.geometry.Shape;

import java.util.List;
public interface EmbodiedAgent extends Agent, Shape {
  void assemble(ActionPerformer actionPerformer) throws ActionException;

  List<Body> bodyParts();

  @Override
  default BoundingBox boundingBox() {
    return bodyParts().stream()
        .map(b -> b.poly().boundingBox())
        .reduce(BoundingBox::enclosing)
        .orElseThrow();
  }

  @Override
  default double area() {
    return bodyParts().stream().mapToDouble(b -> b.poly().area()).sum();
  }

  @Override
  default Point center() {
    //could be weighted by area
    return Point.average(bodyParts().stream().map(b -> b.poly().center()).toArray(Point[]::new));
  }
}
