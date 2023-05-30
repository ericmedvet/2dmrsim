package io.github.ericmedvet.mrsim2d.core.tasks;

import io.github.ericmedvet.jsdynsym.core.DoubleRange;
import io.github.ericmedvet.mrsim2d.core.geometry.Point;
import io.github.ericmedvet.mrsim2d.core.geometry.Poly;

import java.util.HashMap;
import java.util.Map;
import java.util.SortedMap;

public class Outcome {

  private final static int N_OF_CACHED_SUB_OUTCOMES = 3;
  private final SortedMap<Double, Observation> observations;
  private final Map<Key, Double> metricMap;
  private final Map<DoubleRange, Outcome> subOutcomes;

  public Outcome(SortedMap<Double, Observation> observations) {
    this.observations = observations;
    metricMap = new HashMap<>();
    subOutcomes = new HashMap<>();
  }

  private enum Aggregate {INITIAL, FINAL, AVERAGE, MIN, MAX}

  private enum Metric {X, Y, TERRAIN_H, BB_W, BB_H, BB_AREA}

  private enum Subject {FIRST, ALL}

  private record Key(Metric metric, Aggregate aggregate, Subject subject) {}

  public double allAgentsAverageHeight() {
    return get(Aggregate.AVERAGE, Metric.BB_H, Subject.ALL);
  }

  public double allAgentsAverageWidth() {
    return get(Aggregate.AVERAGE, Metric.BB_W, Subject.ALL);
  }

  public double allAgentsFinalHeight() {
    return get(Aggregate.FINAL, Metric.BB_H, Subject.ALL);
  }

  public double allAgentsFinalWidth() {
    return get(Aggregate.FINAL, Metric.BB_W, Subject.ALL);
  }

  public double allAgentsMaxHeight() {
    return get(Aggregate.MAX, Metric.BB_H, Subject.ALL);
  }

  public double allAgentsMaxWidth() {
    return get(Aggregate.MAX, Metric.BB_W, Subject.ALL);
  }

  public double duration() {
    return observations.lastKey() - observations.firstKey();
  }

  public double firstAgentAverageArea() {
    return get(Aggregate.AVERAGE, Metric.BB_AREA, Subject.FIRST);
  }

  public double firstAgentAverageTerrainHeight() {
    return get(Aggregate.AVERAGE, Metric.TERRAIN_H, Subject.FIRST);
  }

  public double firstAgentAverageY() {
    return get(Aggregate.AVERAGE, Metric.Y, Subject.FIRST);
  }

  public double firstAgentMaxY() {
    return get(Aggregate.MAX, Metric.Y, Subject.FIRST);
  }

  public double firstAgentXDistance() {
    return get(Aggregate.FINAL, Metric.X, Subject.FIRST) - get(Aggregate.INITIAL, Metric.X, Subject.FIRST);
  }

  public double firstAgentXVelocity() {
    return firstAgentXDistance() / duration();
  }

  private double get(Aggregate aggregate, Metric metric, Subject subject) {
    Double value = metricMap.get(new Key(metric, aggregate, subject));
    if (value == null) {
      value = switch (aggregate) {
        case FINAL -> get(metric, subject, observations.get(observations.lastKey()));
        case INITIAL -> get(metric, subject, observations.get(observations.firstKey()));
        case AVERAGE -> observations.values().stream().mapToDouble(o -> get(metric, subject, o)).average().orElse(0d);
        case MIN -> observations.values().stream().mapToDouble(o -> get(metric, subject, o)).min().orElse(0d);
        case MAX -> observations.values().stream().mapToDouble(o -> get(metric, subject, o)).max().orElse(0d);
      };
      metricMap.put(new Key(metric, aggregate, subject), value);
    }
    return value;
  }

  private double get(Metric metric, Subject subject, Observation observation) {
    return switch (metric) {
      case X -> subject.equals(Subject.FIRST) ? observation.getFirstAgentCenter().x() : observation.getAllBoundingBox()
          .center()
          .x();
      case Y -> subject.equals(Subject.FIRST) ? observation.getFirstAgentCenter().y() : observation.getAllBoundingBox()
          .center()
          .y();
      case TERRAIN_H -> {
        if (subject.equals(Subject.FIRST)) {
          yield observation.getFirstAgentCenter().y() - observation.getAgents().get(0).terrainHeight();
        } else {
          yield observation.getAgents().stream()
              .mapToDouble(a -> Point.average(
                  a.polies().stream().map(Poly::center).toArray(Point[]::new)
              ).y() - a.terrainHeight())
              .average().orElse(0d);
        }
      }
      case BB_AREA -> subject.equals(Subject.FIRST) ? observation.getFirstAgentBoundingBox()
          .area() : observation.getAllBoundingBox().area();
      case BB_W -> subject.equals(Subject.FIRST) ? observation.getFirstAgentBoundingBox()
          .width() : observation.getAllBoundingBox().width();
      case BB_H -> subject.equals(Subject.FIRST) ? observation.getFirstAgentBoundingBox()
          .height() : observation.getAllBoundingBox().height();
    };
  }

  public Outcome subOutcome(DoubleRange tRange) {
    Outcome subOutcome = subOutcomes.get(tRange);
    if (subOutcome == null) {
      subOutcome = new Outcome(observations.subMap(tRange.min(), tRange.max()));
      if (subOutcomes.size() >= N_OF_CACHED_SUB_OUTCOMES) {
        //remove one
        subOutcomes.remove(subOutcomes.keySet().iterator().next());
      }
      subOutcomes.put(tRange, subOutcome);
    }
    return subOutcome;
  }

  @Override
  public String toString() {
    return "Outcome[%.1f->%.1f]".formatted(observations.firstKey(), observations.lastKey());
  }
}
