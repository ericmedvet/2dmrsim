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

package it.units.erallab.mrsim2d.viewer.drawers;

import it.units.erallab.mrsim2d.core.Snapshot;
import it.units.erallab.mrsim2d.core.engine.EngineSnapshot;
import it.units.erallab.mrsim2d.viewer.Drawer;
import it.units.erallab.mrsim2d.viewer.DrawingUtils;

import java.awt.*;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @author "Eric Medvet" on 2022/07/07 for 2dmrsim
 */
public class InfoDrawer implements Drawer {
  private final String string;
  private final Set<EngineInfo> engineInfos;
  public InfoDrawer(String string, Set<EngineInfo> engineInfos) {
    this.string = string;
    this.engineInfos = EnumSet.noneOf(EngineInfo.class);
    this.engineInfos.addAll(engineInfos);
  }

  public InfoDrawer(String string) {
    this(string, EnumSet.of(EngineInfo.N_OF_BODIES, EngineInfo.N_OF_AGENTS));
  }

  public InfoDrawer() {
    this("");
  }

  public enum EngineInfo implements Function<EngineSnapshot, String> {
    N_OF_BODIES(s -> String.format("#bodies=%d", s.bodies().size())),
    N_OF_AGENTS(s -> String.format("#agents=%d", s.agents().size())),
    N_OF_ACTIONS(s -> String.format("#actions=%d", s.actionOutcomes().size()));
    private final Function<EngineSnapshot, String> function;

    EngineInfo(Function<EngineSnapshot, String> function) {
      this.function = function;
    }

    @Override
    public String apply(EngineSnapshot engineSnapshot) {
      return function.apply(engineSnapshot);
    }
  }

  @Override
  public boolean draw(List<Snapshot> snapshots, Graphics2D g) {
    Snapshot snapshot = snapshots.get(snapshots.size() - 1);
    //prepare string
    StringBuilder sb = new StringBuilder();
    if (!string.isEmpty()) {
      sb.append(string);
      sb.append("\n");
    }
    sb.append(String.format("t=%4.1fs%n", snapshot.t()));
    if (!engineInfos.isEmpty()) {
      if (snapshot instanceof EngineSnapshot es) {
        sb.append(engineInfos.stream().map(ei -> ei.apply(es)).collect(Collectors.joining("; ")));
        sb.append("\n");
      }
    }
    //write
    g.setFont(DrawingUtils.FONT);
    g.setColor(DrawingUtils.Colors.TEXT);
    int relY = g.getClipBounds().y + 1;
    for (String line : sb.toString().split(String.format("%n"))) {
      g.drawString(line, g.getClipBounds().x + 1, relY + g.getFontMetrics().getMaxAscent());
      relY = relY + g.getFontMetrics().getMaxAscent() + 1;
    }
    return true;
  }
}
