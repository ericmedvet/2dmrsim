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

import it.units.erallab.mrsim.core.geometry.Point;
import it.units.erallab.mrsim.engine.EngineSnapshot;
import it.units.erallab.mrsim.util.DoubleRange;
import it.units.erallab.mrsim.util.Pair;
import it.units.erallab.mrsim.viewer.AbstractMemoryDrawer;
import it.units.erallab.mrsim.viewer.DrawingUtils;

import java.awt.*;
import java.util.Arrays;
import java.util.Map;
import java.util.SortedMap;
import java.util.stream.Collectors;

/**
 * @author "Eric Medvet" on 2022/07/15 for 2dmrsim
 */
public class EnginProfilingDrawer extends AbstractMemoryDrawer<Pair<Map<EngineSnapshot.TimeType, Double>,
    Map<EngineSnapshot.CounterType, Integer>>> {
  private final static double WINDOW_T = 1;
  private final static double BAR_W = 50;
  private final static double BAR_H = 15;
  private final static double GAP_W = 10;
  private final static double GAP_H = 10;

  public EnginProfilingDrawer(double windowT) {
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
  }

  public EnginProfilingDrawer() {
    this(WINDOW_T);
  }

  @Override
  protected boolean innerDraw(
      SortedMap<Double, Pair<Map<EngineSnapshot.TimeType, Double>, Map<EngineSnapshot.CounterType, Integer>>> memory,
      Graphics2D g
  ) {
    double x = g.getClipBounds().width - BAR_W - GAP_W;
    double y = GAP_H;
    Point size = new Point(BAR_W, BAR_H);
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
    DoubleRange range = new DoubleRange(0, relTimes.get(EngineSnapshot.TimeType.WALL));
    //draw
    DrawingUtils.drawFilledBar(new Point(x, y), size, relTimes.get(EngineSnapshot.TimeType.TICK), range, g);
    return true;
  }
}
