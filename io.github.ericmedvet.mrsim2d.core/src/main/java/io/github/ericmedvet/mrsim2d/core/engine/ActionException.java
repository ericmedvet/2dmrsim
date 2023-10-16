
package io.github.ericmedvet.mrsim2d.core.engine;

import io.github.ericmedvet.mrsim2d.core.Action;
public class ActionException extends Exception {
  private final Action<?> action;

  public ActionException(Action<?> action, String cause) {
    super(String.format("Cannot perform action %s: %s", action, cause));
    this.action = action;
  }

  public ActionException(String message) {
    super(message);
    this.action = null;
  }

  public Action<?> getAction() {
    return action;
  }

}
