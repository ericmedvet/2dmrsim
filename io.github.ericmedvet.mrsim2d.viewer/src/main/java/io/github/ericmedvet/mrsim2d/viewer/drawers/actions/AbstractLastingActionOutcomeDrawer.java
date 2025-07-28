/*-
 * ========================LICENSE_START=================================
 * mrsim2d-viewer
 * %%
 * Copyright (C) 2020 - 2025 Eric Medvet
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * =========================LICENSE_END==================================
 */

package io.github.ericmedvet.mrsim2d.viewer.drawers.actions;

import io.github.ericmedvet.mrsim2d.core.Action;
import io.github.ericmedvet.mrsim2d.core.ActionOutcome;
import io.github.ericmedvet.mrsim2d.viewer.drawers.AbstractLastingComponentDrawer;
import java.awt.*;
import java.util.function.BiPredicate;

public abstract class AbstractLastingActionOutcomeDrawer<A extends Action<O>, O> extends AbstractLastingComponentDrawer {

  protected static final double DURATION = 0.5;
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
