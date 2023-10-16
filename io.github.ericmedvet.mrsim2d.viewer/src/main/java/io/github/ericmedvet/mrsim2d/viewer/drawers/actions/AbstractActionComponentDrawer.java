
package io.github.ericmedvet.mrsim2d.viewer.drawers.actions;

import io.github.ericmedvet.mrsim2d.core.Action;
import io.github.ericmedvet.mrsim2d.core.ActionOutcome;
import io.github.ericmedvet.mrsim2d.viewer.ComponentDrawer;

import java.awt.*;
public abstract class AbstractActionComponentDrawer<A extends Action<O>, O> implements ComponentDrawer {
  private final Class<A> actionClass;

  public AbstractActionComponentDrawer(Class<A> actionClass) {
    this.actionClass = actionClass;
  }

  protected abstract boolean innerDraw(double t, ActionOutcome<A, O> actionOutcome, Graphics2D g);

  @SuppressWarnings("unchecked")
  @Override
  public boolean draw(double t, Object component, Graphics2D g) {
    if (component instanceof ActionOutcome<?, ?> actionOutcome) {
      if (actionClass.isAssignableFrom(actionOutcome.action().getClass())) {
        return innerDraw(t, (ActionOutcome<A, O>) actionOutcome, g);
      }
    }
    return false;
  }
}
