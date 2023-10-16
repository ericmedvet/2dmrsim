
package io.github.ericmedvet.mrsim2d.core.actions;

import io.github.ericmedvet.jsdynsym.core.DoubleRange;
import io.github.ericmedvet.mrsim2d.core.ActionPerformer;
import io.github.ericmedvet.mrsim2d.core.Agent;
import io.github.ericmedvet.mrsim2d.core.SelfDescribedAction;
import io.github.ericmedvet.mrsim2d.core.bodies.Body;
import io.github.ericmedvet.mrsim2d.core.bodies.SoftBody;
import io.github.ericmedvet.mrsim2d.core.engine.ActionException;

public record SenseAreaRatio(Body body) implements Sense<Body>, SelfDescribedAction<Double> {
  private final static DoubleRange RANGE = new DoubleRange(0.5, 1.5);

  @Override
  public Double perform(ActionPerformer performer, Agent agent) throws ActionException {
    if (body instanceof SoftBody softBody) {
      return RANGE.clip(softBody.areaRatio());
    }
    return 1d;
  }

  @Override
  public DoubleRange range() {
    return RANGE;
  }
}
