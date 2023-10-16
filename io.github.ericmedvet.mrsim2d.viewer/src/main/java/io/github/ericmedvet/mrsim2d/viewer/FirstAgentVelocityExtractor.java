package io.github.ericmedvet.mrsim2d.viewer;

import io.github.ericmedvet.mrsim2d.core.EmbodiedAgent;
import io.github.ericmedvet.mrsim2d.core.Snapshot;
import io.github.ericmedvet.mrsim2d.core.geometry.Point;

import java.util.Optional;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.function.Function;
public class FirstAgentVelocityExtractor implements Function<Snapshot, Optional<Point>> {
  private final double windowT;
  private final SortedMap<Double, Point> memory;

  public FirstAgentVelocityExtractor(double windowT) {
    this.windowT = windowT;
    memory = new TreeMap<>();
  }

  @Override
  public Optional<Point> apply(Snapshot snapshot) {
    if (snapshot.agents().stream().noneMatch(a -> a instanceof EmbodiedAgent)) {
      return Optional.empty();
    }
    //add new sample
    Point c = snapshot.agents().stream()
        .filter(a -> a instanceof EmbodiedAgent)
        .map(a -> (EmbodiedAgent) a)
        .findFirst().orElseThrow().boundingBox().center();
    //update memory
    memory.put(snapshot.t(), c);
    memory.keySet().stream()
        .filter(t -> t < snapshot.t() - windowT)
        .toList()
        .forEach(memory.keySet()::remove);
    //compute and return
    Point lC = memory.get(memory.lastKey());
    Point fC = memory.get(memory.firstKey());
    return Optional.of(lC.diff(fC).scale(1d / windowT));
  }
}
