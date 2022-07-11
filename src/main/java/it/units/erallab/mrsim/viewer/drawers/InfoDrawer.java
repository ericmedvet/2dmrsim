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
import it.units.erallab.mrsim.engine.EngineSnapshot;
import it.units.erallab.mrsim.viewer.Drawer;
import it.units.erallab.mrsim.viewer.DrawingUtils;

import java.awt.*;
import java.util.EnumSet;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @author "Eric Medvet" on 2022/07/07 for 2dmrsim
 */
public class InfoDrawer implements Drawer {
  public enum EngineInfo implements Function<EngineSnapshot, String> {
    ENGINE_T(s -> String.format("eT=%5.1fms", s.engineT() * 1000d)),
    WALL_T(s -> String.format("wT=%5.1fs", s.wallT())),
    ENGINE_TPS(s -> String.format("eTPS=%5.1f", s.nOfTicks() / s.engineT())),
    WALL_TPS(s -> String.format("wTPS=%5.1f", s.nOfTicks() / s.wallT())),
    N_OF_BODIES(s -> String.format("#bodies=%d", s.bodies().size())),
    N_OF_AGENTS(s -> String.format("#agents=%d", s.agents().size()));
    private final Function<EngineSnapshot, String> function;

    EngineInfo(Function<EngineSnapshot, String> function) {
      this.function = function;
    }

    @Override
    public String apply(EngineSnapshot engineSnapshot) {
      return function.apply(engineSnapshot);
    }
  }

  private final String string;
  private final Set<EngineInfo> engineInfos;

  public InfoDrawer(String string, Set<EngineInfo> engineInfos) {
    this.string = string;
    this.engineInfos = EnumSet.noneOf(EngineInfo.class);
    this.engineInfos.addAll(engineInfos);
  }

  public InfoDrawer(String string) {
    this(string, EnumSet.allOf(EngineInfo.class));
  }

  public InfoDrawer() {
    this("");
  }

  @Override
  public boolean draw(Snapshot s, Graphics2D g) {
    //prepare string
    StringBuilder sb = new StringBuilder();
    if (!string.isEmpty()) {
      sb.append(string);
      sb.append("\n");
    }
    sb.append(String.format("t=%4.1fs%n", s.t()));
    if (!engineInfos.isEmpty()) {
      if (s instanceof EngineSnapshot es) {
        sb.append(engineInfos.stream().map(ei -> ei.apply(es)).collect(Collectors.joining("; ")));
        sb.append("\n");
      }
    }
    //write
    g.setColor(DrawingUtils.Colors.TEXT);
    int relY = g.getClipBounds().y + 1;
    for (String line : sb.toString().split(String.format("%n"))) {
      g.drawString(line, g.getClipBounds().x + 1, relY + g.getFontMetrics().getMaxAscent());
      relY = relY + g.getFontMetrics().getMaxAscent() + 1;
    }
    return true;
  }
}
