package it.units.erallab.mrsim.core.actions;

import it.units.erallab.mrsim.core.bodies.Body;
import it.units.erallab.mrsim.util.DoubleRange;

public record SenseDirectedVelocity(double direction, Body body) implements Sense<Body> {
  @Override
  public DoubleRange range() {
    return DoubleRange.UNBOUNDED;
  }
}
