
package io.github.ericmedvet.mrsim2d.core.actions;

import io.github.ericmedvet.jsdynsym.core.DoubleRange;
import io.github.ericmedvet.mrsim2d.core.bodies.Body;
public record SenseSinusoidal(double f, double phi, Body body) implements Sense<Body> {

  @Override
  public DoubleRange range() {
    return DoubleRange.SYMMETRIC_UNIT;
  }
}
