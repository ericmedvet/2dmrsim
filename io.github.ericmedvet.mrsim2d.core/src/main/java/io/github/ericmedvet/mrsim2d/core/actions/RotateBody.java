
package io.github.ericmedvet.mrsim2d.core.actions;

import io.github.ericmedvet.mrsim2d.core.Action;
import io.github.ericmedvet.mrsim2d.core.bodies.Body;
import io.github.ericmedvet.mrsim2d.core.geometry.Point;
public record RotateBody(
    Body body,
    Point point,
    double angle
) implements Action<Body> {
  public RotateBody(Body body, double angle) {
    this(body, body.poly().center(), angle);
  }
}
