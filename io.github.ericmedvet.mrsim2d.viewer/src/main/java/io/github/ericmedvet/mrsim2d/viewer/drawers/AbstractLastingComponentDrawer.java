
package io.github.ericmedvet.mrsim2d.viewer.drawers;

import io.github.ericmedvet.mrsim2d.core.util.Pair;
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
