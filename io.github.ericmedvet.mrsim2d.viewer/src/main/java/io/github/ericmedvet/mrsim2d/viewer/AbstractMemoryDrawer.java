
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

  public enum WindowType {SNAPSHOT_TIME, WALL_TIME}

  protected abstract boolean innerDraw(SortedMap<Double, T> memory, Graphics2D g);

  @Override
  public boolean draw(List<Snapshot> snapshots, Graphics2D g) {
    double wallT = Duration.between(startingInstant, Instant.now()).toMillis() / 1000d;
    //update map
    snapshots.forEach(s -> memory.put(
        switch (windowType) {
          case WALL_TIME -> wallT;
          case SNAPSHOT_TIME -> s.t();
        },
        extractor.apply(s)
    ));
    double lastT = memory.lastKey();
    memory.keySet().stream()
        .filter(t -> t < lastT - windowT)
        .toList()
        .forEach(memory.keySet()::remove);
    //call inner drawer
    return innerDraw(memory, g);
  }

  protected double getWindowT() {
    return windowT;
  }
}
