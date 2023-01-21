package io.github.ericmedvet.mrsim2d.core.actions;

import io.github.ericmedvet.mrsim2d.core.NFCMessage;
import io.github.ericmedvet.mrsim2d.core.bodies.Body;
import io.github.ericmedvet.mrsim2d.core.geometry.Point;
import io.github.ericmedvet.mrsim2d.core.util.DoubleRange;

/**
 * @author "Eric Medvet" on 2023/01/21 for 2dmrsim
 */
public record EmitNFCMessage(
    Body body,
    Point displacement,
    double relativeAngle,
    short channel,
    double value
) implements Actuate<Body, NFCMessage> {
  @Override
  public DoubleRange range() {
    return DoubleRange.SYMMETRIC_UNIT;
  }
}
