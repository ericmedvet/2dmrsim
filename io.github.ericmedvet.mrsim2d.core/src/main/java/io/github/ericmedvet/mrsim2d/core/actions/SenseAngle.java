
package io.github.ericmedvet.mrsim2d.core.actions;

import io.github.ericmedvet.jsdynsym.core.DoubleRange;
import io.github.ericmedvet.mrsim2d.core.ActionPerformer;
import io.github.ericmedvet.mrsim2d.core.Agent;
import io.github.ericmedvet.mrsim2d.core.SelfDescribedAction;
import io.github.ericmedvet.mrsim2d.core.bodies.Body;
import io.github.ericmedvet.mrsim2d.core.engine.ActionException;

public record SenseAngle(Body body) implements Sense<Body>, SelfDescribedAction<Double> {

  private final static DoubleRange RANGE = new DoubleRange(-Math.PI, Math.PI);

  @Override
  public Double perform(ActionPerformer performer, Agent agent) throws ActionException {
    double a = body.angle();
    if (a > Math.PI) {
      a = a - 2d * Math.PI;
    }
    if (a < -Math.PI) {
      a = a + 2d * Math.PI;
    }
    return a;
  }

  @Override
  public DoubleRange range() {
    return RANGE;
  }
}
