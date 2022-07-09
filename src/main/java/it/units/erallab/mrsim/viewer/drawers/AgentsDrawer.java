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

import it.units.erallab.mrsim.core.Action;
import it.units.erallab.mrsim.core.ActionOutcome;
import it.units.erallab.mrsim.core.Agent;
import it.units.erallab.mrsim.core.Snapshot;
import it.units.erallab.mrsim.core.bodies.Body;
import it.units.erallab.mrsim.util.Pair;
import it.units.erallab.mrsim.viewer.AgentDrawer;
import it.units.erallab.mrsim.viewer.BodyDrawer;
import it.units.erallab.mrsim.viewer.Drawer;

import java.awt.*;
import java.util.List;

/**
 * @author "Eric Medvet" on 2022/07/07 for 2dmrsim
 */
public class AgentsDrawer implements Drawer {
  private final List<AgentDrawer> agentDrawers;

  public AgentsDrawer(List<AgentDrawer> agentDrawers) {
    this.agentDrawers = agentDrawers;
  }

  @Override
  public boolean draw(Snapshot s, Graphics2D g) {
    int c = 0;
    boolean drawn = false;
    for (Pair<Agent, List<ActionOutcome<?, ?>>> pair : s.agentPairs()) {
      int i = c;
      for (AgentDrawer agentDrawer : agentDrawers) {
        if (agentDrawer.draw(s.t(), pair.first(), pair.second(), i, g)) {
          drawn = true;
          break;
        }
      }
      c = c + 1;
    }
    return drawn;
  }
}
