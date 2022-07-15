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

import it.units.erallab.mrsim.engine.EngineSnapshot;
import it.units.erallab.mrsim.util.DoubleRange;
import it.units.erallab.mrsim.util.Pair;
import it.units.erallab.mrsim.viewer.AbstractMemoryDrawer;
import it.units.erallab.mrsim.viewer.DrawingUtils;

import java.awt.*;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.Map;
import java.util.SortedMap;
import java.util.stream.Collectors;

/**
 * @author "Eric Medvet" on 2022/07/15 for 2dmrsim
 */
public class EngineProfilingDrawer extends AbstractMemoryDrawer<Pair<Map<EngineSnapshot.TimeType, Double>,
    Map<EngineSnapshot.CounterType, Integer>>> {
  private final static double WINDOW_T = 1;
  private final static double BAR_W = 50;
  private final static double BAR_H = 10;
  private final static double GAP_W = 10;
  private final static double GAP_H = 10;

  private final Map<EngineSnapshot.CounterType, Integer> maxCounterValues;

  public EngineProfilingDrawer(double windowT) {
    super(
        snapshot -> {
          if (snapshot instanceof EngineSnapshot engineSnapshot) {
            return new Pair<>(engineSnapshot.times(), engineSnapshot.counters());
          } else {
            return new Pair<>(Map.of(), Map.of());
          }
        },
        windowT,
        WindowType.WALL_TIME
    );
    maxCounterValues = new EnumMap<>(EngineSnapshot.CounterType.class);
    Arrays.stream(EngineSnapshot.CounterType.values())
        .forEach(k -> maxCounterValues.put(k, 0));
  }

  public EngineProfilingDrawer() {
    this(WINDOW_T);
  }

  @Override
  protected boolean innerDraw(
      SortedMap<Double, Pair<Map<EngineSnapshot.TimeType, Double>, Map<EngineSnapshot.CounterType, Integer>>> memory,
      Graphics2D g
  ) {
    //check if empty
    if (memory.get(memory.firstKey()).first().isEmpty()) {
      return false;
    }
    //prepare
    double x = g.getClipBounds().width - BAR_W - GAP_W;
    double y = GAP_H;
    //obtain relative data
    Map<EngineSnapshot.TimeType, Double> relTimes = Arrays.stream(EngineSnapshot.TimeType.values())
        .collect(Collectors.toMap(
            k -> k,
            k -> memory.get(memory.lastKey()).first().get(k) - memory.get(memory.firstKey()).first().get(k)
        ));
    Map<EngineSnapshot.CounterType, Integer> relCounters = Arrays.stream(EngineSnapshot.CounterType.values())
        .collect(Collectors.toMap(
            k -> k,
            k -> memory.get(memory.lastKey()).second().get(k) - memory.get(memory.firstKey()).second().get(k)
        ));
    //update bounds
    DoubleRange percRange = new DoubleRange(0, 100);
    Arrays.stream(EngineSnapshot.CounterType.values())
        .forEach(k -> maxCounterValues.put(k, Math.max(maxCounterValues.get(k), relCounters.get(k))));
    //draw
    g.setFont(DrawingUtils.FONT);
    DrawingUtils.drawFilledBar(
        x,
        y,
        BAR_W,
        BAR_H,
        100 * relTimes.get(EngineSnapshot.TimeType.TICK) / relTimes.get(EngineSnapshot.TimeType.WALL),
        percRange,
        "t/w=%3.0f%%",
        g
    );
    y = y + g.getFontMetrics().getMaxAscent();
    DrawingUtils.drawFilledBar(
        x,
        y,
        BAR_W,
        BAR_H,
        100 * relTimes.get(EngineSnapshot.TimeType.INNER_TICK) / relTimes.get(EngineSnapshot.TimeType.TICK),
        percRange,
        "it/t=%3.0f%%",
        g
    );
    y = y + g.getFontMetrics().getMaxAscent();
    DrawingUtils.drawFilledBar(
        x,
        y,
        BAR_W,
        BAR_H,
        relCounters.get(EngineSnapshot.CounterType.ACTION),
        new DoubleRange(0, maxCounterValues.get(EngineSnapshot.CounterType.ACTION)),
        "#a=%4.0f",
        g
    );
    return true;
  }

}
