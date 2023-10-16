
package io.github.ericmedvet.mrsim2d.viewer.drawers.actions;

import io.github.ericmedvet.mrsim2d.core.Action;
import io.github.ericmedvet.mrsim2d.core.ActionOutcome;
import io.github.ericmedvet.mrsim2d.viewer.drawers.AbstractLastingComponentDrawer;

import java.awt.*;
import java.util.function.BiPredicate;
public abstract class AbstractLastingActionOutcomeDrawer<A extends Action<O>, O> extends AbstractLastingComponentDrawer {

  protected final static double DURATION = 0.5;
  protected final double duration;
  private final Class<A> actionClass;

  public AbstractLastingActionOutcomeDrawer(Class<A> actionClass, double duration) {
    this.actionClass = actionClass;
    this.duration = duration;
  }

  protected abstract BiPredicate<Double, Graphics2D> innerBuildTask(double t, ActionOutcome<A, O> o);

  @SuppressWarnings("unchecked")
  @Override
  protected BiPredicate<Double, Graphics2D> buildTask(double t, Object o) {
    if (o instanceof ActionOutcome<?, ?> actionOutcome) {
      if (actionClass.isAssignableFrom(actionOutcome.action().getClass())) {
        return innerBuildTask(t, (ActionOutcome<A, O>) actionOutcome);
      }
    }
    return null;
  }
}
