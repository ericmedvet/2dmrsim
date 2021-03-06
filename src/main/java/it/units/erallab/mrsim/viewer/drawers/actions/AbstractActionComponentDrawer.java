/*
 * Copyright 2022 Eric Medvet <eric.medvet@gmail.com> (as eric)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package it.units.erallab.mrsim.viewer.drawers.actions;

import it.units.erallab.mrsim.core.Action;
import it.units.erallab.mrsim.core.ActionOutcome;
import it.units.erallab.mrsim.viewer.ComponentDrawer;

import java.awt.*;

/**
 * @author "Eric Medvet" on 2022/07/15 for 2dmrsim
 */
public abstract class AbstractActionComponentDrawer<A extends Action<O>, O> implements ComponentDrawer {
  private final Class<A> actionClass;

  public AbstractActionComponentDrawer(Class<A> actionClass) {
    this.actionClass = actionClass;
  }

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

  protected abstract boolean innerDraw(double t, ActionOutcome<A, O> actionOutcome, Graphics2D g);
}
