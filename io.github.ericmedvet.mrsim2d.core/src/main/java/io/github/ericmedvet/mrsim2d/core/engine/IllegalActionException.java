
package io.github.ericmedvet.mrsim2d.core.engine;

import io.github.ericmedvet.mrsim2d.core.Action;
public class IllegalActionException extends ActionException{
  public IllegalActionException(Action<?> action, String cause) {
    super(action, cause);
  }
}
