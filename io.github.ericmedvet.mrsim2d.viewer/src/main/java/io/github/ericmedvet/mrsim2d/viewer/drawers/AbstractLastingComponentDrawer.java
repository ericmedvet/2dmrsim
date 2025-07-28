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

package io.github.ericmedvet.mrsim2d.viewer.drawers;

import io.github.ericmedvet.jnb.datastructure.Pair;
import io.github.ericmedvet.mrsim2d.viewer.ComponentDrawer;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.function.BiPredicate;

public abstract class AbstractLastingComponentDrawer implements ComponentDrawer {

  private final List<Pair<BiPredicate<Double, Graphics2D>, Double>> tasks;

  public AbstractLastingComponentDrawer() {
    this.tasks = new ArrayList<>();
  }

  protected abstract BiPredicate<Double, Graphics2D> buildTask(double t, Object o);

  @Override
  public boolean draw(double t, Object o, Graphics2D g) {
    BiPredicate<Double, Graphics2D> newTask = buildTask(t, o);
    if (newTask != null) {
      tasks.add(new Pair<>(newTask, t));
    }
    List<Pair<BiPredicate<Double, Graphics2D>, Double>> toRemoveTasks = new ArrayList<>();
    for (Pair<BiPredicate<Double, Graphics2D>, Double> task : tasks) {
      if (task.first().test(t - task.second(), g)) {
        toRemoveTasks.add(task);
      }
    }
    boolean drawn = !tasks.isEmpty();
    tasks.removeAll(toRemoveTasks);
    return drawn;
  }
}
