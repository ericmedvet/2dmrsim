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

package io.github.ericmedvet.mrsim2d.viewer;

import io.github.ericmedvet.mrsim2d.core.Snapshot;
import java.awt.*;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.function.Function;

public abstract class AbstractMemoryDrawer<T> implements Drawer {

  private final Function<Snapshot, T> extractor;
  private final double windowT;
  private final WindowType windowType;

  private final SortedMap<Double, T> memory;
  private final Instant startingInstant;

  public AbstractMemoryDrawer(Function<Snapshot, T> extractor, double windowT, WindowType windowType) {
    this.extractor = extractor;
    this.windowT = windowT;
    this.windowType = windowType;
    memory = new TreeMap<>();
    startingInstant = Instant.now();
  }

  public enum WindowType {
    SNAPSHOT_TIME, WALL_TIME
  }

  protected abstract boolean innerDraw(SortedMap<Double, T> memory, Graphics2D g);

  @Override
  public boolean draw(List<Snapshot> snapshots, Graphics2D g) {
    double wallT = Duration.between(startingInstant, Instant.now()).toMillis() / 1000d;
    // update map
    snapshots.forEach(
        s -> memory.put(
            switch (windowType) {
              case WALL_TIME -> wallT;
              case SNAPSHOT_TIME -> s.t();
            },
            extractor.apply(s)
        )
    );
    double lastT = memory.lastKey();
    memory.keySet().stream().filter(t -> t < lastT - windowT).toList().forEach(memory.keySet()::remove);
    // call inner drawer
    return innerDraw(memory, g);
  }

  protected double getWindowT() {
    return windowT;
  }
}
