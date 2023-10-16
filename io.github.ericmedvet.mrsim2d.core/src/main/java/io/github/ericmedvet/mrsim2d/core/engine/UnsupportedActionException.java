
package io.github.ericmedvet.mrsim2d.core.engine;

import io.github.ericmedvet.mrsim2d.core.Action;
public class UnsupportedActionException extends ActionException {
  public UnsupportedActionException(Action<?> action) {
    super(action, "unsupported");
  }
}
