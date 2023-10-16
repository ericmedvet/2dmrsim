
package io.github.ericmedvet.mrsim2d.core.actions;

import io.github.ericmedvet.jsdynsym.core.DoubleRange;
import io.github.ericmedvet.mrsim2d.core.ActionPerformer;
import io.github.ericmedvet.mrsim2d.core.Agent;
import io.github.ericmedvet.mrsim2d.core.SelfDescribedAction;
import io.github.ericmedvet.mrsim2d.core.bodies.Body;
import io.github.ericmedvet.mrsim2d.core.engine.ActionException;

import java.util.Collection;
import java.util.List;

public record SenseContact(Body body) implements Sense<Body>, SelfDescribedAction<Double> {

  @Override
  public Double perform(ActionPerformer performer, Agent agent) throws ActionException {
    Collection<Body> bodies = performer.perform(new FindInContactBodies(body), agent).outcome().orElse(List.of());
    return bodies.isEmpty() ? 0d : 1d;
  }

  @Override
  public DoubleRange range() {
    return DoubleRange.UNIT;
  }
}
