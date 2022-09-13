package it.units.erallab.mrsim2d.core.tasks.locomotion;

import it.units.erallab.mrsim2d.core.geometry.Poly;
import it.units.erallab.mrsim2d.core.util.DoubleRange;

import java.util.List;
import java.util.SortedMap;

public record Outcome(SortedMap<Double, Observation> observations) {
  public record Observation(List<Poly> bodyPartPolies, double terrainHeight) {}

  public double duration() {
    return observations.lastKey() - observations.firstKey();
  }

  public Outcome subOutcome(DoubleRange tRange) {
    return new Outcome(observations.subMap(tRange.min(), tRange.max()));
  }

  @Override
  public String toString() {
    return "Outcome{" +
        "xVelocity=" + xVelocity() +
        '}';
  }

  public double xDistance() {
    double initX = observations.get(observations.firstKey()).bodyPartPolies().stream()
        .mapToDouble(p -> p.boundingBox().min().x())
        .min().orElseThrow(() -> new IllegalArgumentException("Unable to find agent"));
    double finalX = observations.get(observations.lastKey()).bodyPartPolies().stream()
        .mapToDouble(p -> p.boundingBox().min().x())
        .min().orElseThrow(() -> new IllegalArgumentException("Unable to find agent"));
    return finalX - initX;
  }

  public double xVelocity() {
    return xDistance() / duration();
  }
}
