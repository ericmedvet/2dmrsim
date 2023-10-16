
package io.github.ericmedvet.mrsim2d.core.actions;

import io.github.ericmedvet.jsdynsym.core.DoubleRange;
import io.github.ericmedvet.mrsim2d.core.ActionPerformer;
import io.github.ericmedvet.mrsim2d.core.Agent;
import io.github.ericmedvet.mrsim2d.core.SelfDescribedAction;
import io.github.ericmedvet.mrsim2d.core.bodies.Body;
import io.github.ericmedvet.mrsim2d.core.engine.ActionException;
import io.github.ericmedvet.mrsim2d.core.geometry.Point;

public record SenseVelocity(double direction, Body body) implements Sense<Body>, SelfDescribedAction<Double> {
  private final static DoubleRange RANGE = new DoubleRange(-10, 10);

  @Override
  public Double perform(ActionPerformer performer, Agent agent) throws ActionException {
    Point v = body.centerLinearVelocity();
    double a = v.direction() - direction;
    return v.magnitude() * Math.cos(a);
  }

  @Override
  public DoubleRange range() {
    return RANGE;
  }
}
