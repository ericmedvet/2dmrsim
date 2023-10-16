/*-
 * ========================LICENSE_START=================================
 * mrsim2d-viewer
 * %%
 * Copyright (C) 2020 - 2023 Eric Medvet
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

  public ComponentsDrawer(
      List<ComponentDrawer> componentDrawers, Function<Snapshot, Collection<?>> extractor) {
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
          if (componentDrawer.draw(snapshot.t(), component, g)) {
            drawn = true;
            break;
          }
        }
      }
    }
    return drawn;
  }
}
