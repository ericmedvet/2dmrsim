/*-
 * ========================LICENSE_START=================================
 * mrsim2d-viewer
 * %%
 * Copyright (C) 2020 - 2024 Eric Medvet
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
import io.github.ericmedvet.mrsim2d.core.engine.EngineSnapshot;
import io.github.ericmedvet.mrsim2d.viewer.Drawer;
import io.github.ericmedvet.mrsim2d.viewer.DrawingUtils;
import java.awt.*;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

public class InfoDrawer implements Drawer {
  private static final double MARGIN = 1;
  private final String string;
  private final Set<EngineInfo> engineInfos;
  private final VerticalPosition verticalPosition;
  private final HorizontalPosition horizontalPosition;

  public InfoDrawer(
      String string,
      Set<EngineInfo> engineInfos,
      VerticalPosition verticalPosition,
      HorizontalPosition horizontalPosition) {
    this.string = string;
    this.engineInfos = EnumSet.noneOf(EngineInfo.class);
    this.engineInfos.addAll(engineInfos);
    this.verticalPosition = verticalPosition;
    this.horizontalPosition = horizontalPosition;
  }

  public InfoDrawer(String string) {
    this(
        string,
        EnumSet.of(EngineInfo.N_OF_BODIES, EngineInfo.N_OF_AGENTS),
        VerticalPosition.TOP,
        HorizontalPosition.LEFT);
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
    // prepare string
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
    // write
    g.setFont(DrawingUtils.FONT);
    String[] lines = sb.toString().split(String.format("%n"));
    double bbW = Arrays.stream(lines)
        .mapToDouble(s -> g.getFontMetrics().stringWidth(s))
        .max()
        .orElse(0d);
    double bbH = lines.length * g.getFontMetrics().getHeight();
    double x =
        switch (horizontalPosition) {
          case LEFT -> g.getClipBounds().getMinX() + MARGIN;
          case RIGHT -> g.getClipBounds().getMaxX() - bbW - MARGIN;
        };
    double y =
        switch (verticalPosition) {
          case TOP -> g.getClipBounds().getMinY() + MARGIN;
          case BOTTOM -> g.getClipBounds().getMaxY() - bbH - MARGIN;
        };
    g.setColor(DrawingUtils.Colors.TEXT);
    for (String line : lines) {
      g.drawString(line, (float) x, (float) (y + g.getFontMetrics().getHeight()));
      y = y + g.getFontMetrics().getHeight();
    }
    return true;
  }
}
