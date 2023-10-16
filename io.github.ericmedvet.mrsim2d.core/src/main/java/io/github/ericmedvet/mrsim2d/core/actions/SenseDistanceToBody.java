
package io.github.ericmedvet.mrsim2d.core.actions;

import io.github.ericmedvet.jsdynsym.core.DoubleRange;
import io.github.ericmedvet.mrsim2d.core.bodies.Body;
public record SenseDistanceToBody(
    double direction, double distanceRange, Body body
) implements Sense<Body> {

  @Override
  public DoubleRange range() {
    return new DoubleRange(0, distanceRange);
  }
}
