package io.github.ericmedvet.mrsim2d.core.actions;

import io.github.ericmedvet.mrsim2d.core.Action;
import io.github.ericmedvet.mrsim2d.core.bodies.Body;
import io.github.ericmedvet.mrsim2d.core.util.DoubleRange;

public interface Actuate<B extends Body> extends Action<B> {
  B body();

  DoubleRange range();
}
