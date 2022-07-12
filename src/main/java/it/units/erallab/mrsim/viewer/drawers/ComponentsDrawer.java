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

import it.units.erallab.mrsim.core.Snapshot;
import it.units.erallab.mrsim.viewer.ComponentDrawer;
import it.units.erallab.mrsim.viewer.Drawer;

import java.awt.*;
import java.util.Collection;
import java.util.List;
import java.util.function.Function;

/**
 * @author "Eric Medvet" on 2022/07/07 for 2dmrsim
 */
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
