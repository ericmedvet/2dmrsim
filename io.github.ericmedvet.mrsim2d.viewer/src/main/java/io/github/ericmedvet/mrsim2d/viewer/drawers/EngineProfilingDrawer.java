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

import io.github.ericmedvet.jnb.datastructure.DoubleRange;
import io.github.ericmedvet.jnb.datastructure.Pair;
import io.github.ericmedvet.mrsim2d.core.engine.EngineSnapshot;
import io.github.ericmedvet.mrsim2d.viewer.AbstractMemoryDrawer;
import io.github.ericmedvet.mrsim2d.viewer.DrawingUtils;
import java.awt.Graphics2D;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.Map;
import java.util.SortedMap;
import java.util.stream.Collectors;

public class EngineProfilingDrawer
    extends AbstractMemoryDrawer<
        Pair<Map<EngineSnapshot.TimeType, Double>, Map<EngineSnapshot.CounterType, Integer>>> {
  public static final String TICK_PS_FORMAT = "t/w=%3.0f%%";
  public static final String INNER_TICK_RATE_FORMAT = "it/t=%3.0f%%";
  public static final String ACTION_COUNT_FORMAT = "#a=%4.0f";
  private static final double WINDOW_T = 1;
  private static final double BAR_W = 50;
  private static final double BAR_H = 10;
  private static final double MARGIN = 1;
  private final VerticalPosition verticalPosition;
  private final HorizontalPosition horizontalPosition;

  private final Map<EngineSnapshot.CounterType, Integer> maxCounterValues;

  public EngineProfilingDrawer(
      double windowT, VerticalPosition verticalPosition, HorizontalPosition horizontalPosition) {
    super(
        snapshot -> {
          if (snapshot instanceof EngineSnapshot engineSnapshot) {
            return new Pair<>(engineSnapshot.times(), engineSnapshot.counters());
          } else {
            return new Pair<>(Map.of(), Map.of());
          }
        },
        windowT,
        WindowType.WALL_TIME);
    maxCounterValues = new EnumMap<>(EngineSnapshot.CounterType.class);
    Arrays.stream(EngineSnapshot.CounterType.values()).forEach(k -> maxCounterValues.put(k, 0));
    this.verticalPosition = verticalPosition;
    this.horizontalPosition = horizontalPosition;
  }

  public EngineProfilingDrawer() {
    this(WINDOW_T, VerticalPosition.BOTTOM, HorizontalPosition.RIGHT);
  }

  @Override
  protected boolean innerDraw(
      SortedMap<Double, Pair<Map<EngineSnapshot.TimeType, Double>, Map<EngineSnapshot.CounterType, Integer>>>
          memory,
      Graphics2D g) {
    // check if empty
    if (memory.get(memory.firstKey()).first().isEmpty()) {
      return false;
    }
    // obtain relative data
    Map<EngineSnapshot.TimeType, Double> relTimes = Arrays.stream(EngineSnapshot.TimeType.values())
        .collect(Collectors.toMap(
            k -> k,
            k -> memory.get(memory.lastKey()).first().get(k)
                - memory.get(memory.firstKey()).first().get(k)));
    Map<EngineSnapshot.CounterType, Integer> relCounters = Arrays.stream(EngineSnapshot.CounterType.values())
        .collect(Collectors.toMap(
            k -> k,
            k -> memory.get(memory.lastKey()).second().get(k)
                - memory.get(memory.firstKey()).second().get(k)));
    // update bounds
    DoubleRange percRange = new DoubleRange(0, 100);
    Arrays.stream(EngineSnapshot.CounterType.values())
        .forEach(k -> maxCounterValues.put(k, Math.max(maxCounterValues.get(k), relCounters.get(k))));
    // compute box w and h
    double bbW = 0d;
    bbW = Math.max(
        bbW,
        BAR_W
            + g.getFontMetrics().charWidth('x')
            + g.getFontMetrics()
                .stringWidth(TICK_PS_FORMAT.formatted(100
                    * relTimes.get(EngineSnapshot.TimeType.TICK)
                    / relTimes.get(EngineSnapshot.TimeType.WALL))));
    bbW = Math.max(
        bbW,
        BAR_W
            + g.getFontMetrics().charWidth('x')
            + g.getFontMetrics()
                .stringWidth(INNER_TICK_RATE_FORMAT.formatted(100
                    * relTimes.get(EngineSnapshot.TimeType.INNER_TICK)
                    / relTimes.get(EngineSnapshot.TimeType.TICK))));
    bbW = Math.max(
        bbW,
        BAR_W
            + g.getFontMetrics().charWidth('x')
            + g.getFontMetrics().stringWidth(ACTION_COUNT_FORMAT.formatted((float)
                relCounters.get(EngineSnapshot.CounterType.ACTION))));
    double bbH = 3 * g.getFontMetrics().getHeight();
    // draw
    double x =
        switch (horizontalPosition) {
          case LEFT -> MARGIN;
          case RIGHT -> g.getClipBounds().getMaxX() - bbW - MARGIN;
        };
    double y =
        switch (verticalPosition) {
          case TOP -> MARGIN;
          case BOTTOM -> g.getClipBounds().getMaxY() - bbH - MARGIN;
        };
    g.setFont(DrawingUtils.FONT);
    DrawingUtils.drawFilledBar(
        x,
        y,
        BAR_W,
        BAR_H,
        100 * relTimes.get(EngineSnapshot.TimeType.TICK) / relTimes.get(EngineSnapshot.TimeType.WALL),
        percRange,
        TICK_PS_FORMAT,
        g);
    y = y + g.getFontMetrics().getHeight();
    DrawingUtils.drawFilledBar(
        x,
        y,
        BAR_W,
        BAR_H,
        100 * relTimes.get(EngineSnapshot.TimeType.INNER_TICK) / relTimes.get(EngineSnapshot.TimeType.TICK),
        percRange,
        INNER_TICK_RATE_FORMAT,
        g);
    y = y + g.getFontMetrics().getHeight();
    DrawingUtils.drawFilledBar(
        x,
        y,
        BAR_W,
        BAR_H,
        relCounters.get(EngineSnapshot.CounterType.ACTION),
        new DoubleRange(0, maxCounterValues.get(EngineSnapshot.CounterType.ACTION)),
        ACTION_COUNT_FORMAT,
        g);
    return true;
  }
}
