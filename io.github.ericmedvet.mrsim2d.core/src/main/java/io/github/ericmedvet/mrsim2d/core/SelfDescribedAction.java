
package io.github.ericmedvet.mrsim2d.core;

import io.github.ericmedvet.mrsim2d.core.engine.ActionException;
public interface SelfDescribedAction<O> extends Action<O> {
  O perform(ActionPerformer performer, Agent agent) throws ActionException;
}
