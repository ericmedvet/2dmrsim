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

package it.units.erallab.mrsim.viewer.drawers;

import it.units.erallab.mrsim.util.Pair;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.function.BiPredicate;

/**
 * @author "Eric Medvet" on 2022/07/11 for 2dmrsim
 */
public abstract class AbstractLastingComponentDrawer<K> extends AbstractComponentDrawer<K> {

  private final List<Pair<BiPredicate<Double, Graphics2D>, Double>> tasks;

  public AbstractLastingComponentDrawer(
      Class<K> bodyClass
  ) {
    super(bodyClass);
    this.tasks = new ArrayList<>();
  }

  @Override
  protected boolean innerDraw(double t, K k, Graphics2D g) {
    List<Pair<BiPredicate<Double, Graphics2D>, Double>> toRemoveTasks = new ArrayList<>();
    for (Pair<BiPredicate<Double, Graphics2D>, Double> task : tasks) {
      if (task.first().test(t - task.second(), g)) {
        toRemoveTasks.add(task);
      }
    }
    boolean drawn = !tasks.isEmpty();
    tasks.removeAll(toRemoveTasks);
    return tasks.isEmpty();
  }

  protected abstract BiPredicate<Double, Graphics2D> buildTask(double t, K k);
}
