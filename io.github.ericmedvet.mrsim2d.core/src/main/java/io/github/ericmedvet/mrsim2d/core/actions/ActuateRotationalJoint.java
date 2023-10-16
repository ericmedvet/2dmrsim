
package io.github.ericmedvet.mrsim2d.core.actions;

import io.github.ericmedvet.jsdynsym.core.DoubleRange;
import io.github.ericmedvet.mrsim2d.core.bodies.RotationalJoint;
public record ActuateRotationalJoint(RotationalJoint body, double value) implements Actuate<RotationalJoint, RotationalJoint> {
  @Override
  public DoubleRange range() {
    return DoubleRange.SYMMETRIC_UNIT;
  }
}
