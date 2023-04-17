package io.github.ericmedvet.mrsim2d.core.actions;

import io.github.ericmedvet.jsdynsym.core.DoubleRange;
import io.github.ericmedvet.mrsim2d.core.Action;
import io.github.ericmedvet.mrsim2d.core.bodies.Body;

public interface Actuate<B extends Body, O> extends Action<O> {
  B body();

  DoubleRange range();
}
