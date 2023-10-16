
package io.github.ericmedvet.mrsim2d.viewer.drawers;

import io.github.ericmedvet.mrsim2d.core.Snapshot;
import io.github.ericmedvet.mrsim2d.viewer.ComponentDrawer;
import io.github.ericmedvet.mrsim2d.viewer.Drawer;

import java.awt.*;
import java.util.Collection;
import java.util.List;
import java.util.function.Function;
public class ComponentsDrawer implements Drawer {
  private final List<ComponentDrawer> componentDrawers;
  private final Function<Snapshot, Collection<?>> extractor;

  public ComponentsDrawer(List<ComponentDrawer> componentDrawers, Function<Snapshot, Collection<?>> extractor) {
    this.componentDrawers = componentDrawers;
    this.extractor = extractor;
  }

  @Override
  public boolean draw(List<Snapshot> snapshots, Graphics2D g) {
    boolean drawn = false;
    for (Snapshot snapshot : snapshots) {
      Collection<?> components = extractor.apply(snapshot);
      for (Object component : components) {
        for (ComponentDrawer componentDrawer : componentDrawers) {
          if (componentDrawer.draw(
              snapshot.t(),
              component,
              g
          )) {
            drawn = true;
            break;
          }
        }
      }
    }
    return drawn;
  }
}
