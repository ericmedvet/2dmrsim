package it.units.erallab.mrsim2d.core.tasks.piling;

import it.units.erallab.mrsim2d.core.geometry.Poly;
import it.units.erallab.mrsim2d.core.util.DoubleRange;

import java.util.Collection;
import java.util.List;
import java.util.SortedMap;

public record Outcome(SortedMap<Double, Observation> observations) {

  public record Observation(List<List<Poly>> bodies) {
    public double pileHeight() {
      if (bodies.isEmpty()) {
        return 0d;
      }
      double minY = bodies.stream()
          .flatMap(Collection::stream)
          .mapToDouble(p -> p.boundingBox().min().y())
          .min()
          .orElseThrow();
      double maxY = bodies.stream()
          .flatMap(Collection::stream)
          .mapToDouble(p -> p.boundingBox().min().y())
          .max()
          .orElseThrow();
      return maxY - minY;
    }
  }

  public double duration() {
    return observations.lastKey() - observations.firstKey();
  }

  public int nOfAgentsDiff() {
    return observations.get(observations.lastKey()).bodies().size()
        - observations.get(observations.firstKey()).bodies().size();
  }

  public double pileHeightDiff() {
    return observations.get(observations.lastKey()).pileHeight()
        - observations.get(observations.firstKey()).pileHeight();
  }

  public double averageMaxHeight() {
    return observations.values().stream()
        .mapToDouble(Observation::pileHeight)
        .average()
        .orElse(0d);
  }

  public Outcome subOutcome(DoubleRange tRange) {
    return new Outcome(observations.subMap(tRange.min(), tRange.max()));
  }

  @Override
  public String toString() {
    return "Outcome{" +
        "pileHeightDiff=" + pileHeightDiff() +
        '}';
  }
}
