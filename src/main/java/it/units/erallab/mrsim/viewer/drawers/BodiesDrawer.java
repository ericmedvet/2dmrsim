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
import it.units.erallab.mrsim.core.bodies.Body;
import it.units.erallab.mrsim.viewer.BodyDrawer;
import it.units.erallab.mrsim.viewer.Drawer;

import java.awt.*;
import java.util.List;
import java.util.stream.IntStream;

/**
 * @author "Eric Medvet" on 2022/07/07 for 2dmrsim
 */
public class BodiesDrawer implements Drawer {
  private final List<BodyDrawer> bodyDrawers;

  public BodiesDrawer(List<BodyDrawer> bodyDrawers) {
    this.bodyDrawers = bodyDrawers;
  }

  @Override
  public boolean draw(Snapshot s, Graphics2D g) {
    int c = 0;
    boolean drawn = false;
    for (Body<?> body : s.bodies()) {
      int i = c;
      for (BodyDrawer bodyDrawer : bodyDrawers) {
        if (bodyDrawer.draw(s.t(), body, i, g)) {
          drawn = true;
          break;
        }
      }
      c = c + 1;
    }
    return drawn;
  }
}
