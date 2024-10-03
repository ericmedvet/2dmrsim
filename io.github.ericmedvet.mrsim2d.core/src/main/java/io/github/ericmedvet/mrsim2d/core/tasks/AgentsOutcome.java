/*-
 * ========================LICENSE_START=================================
 * mrsim2d-core
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
package io.github.ericmedvet.mrsim2d.core.tasks;

import io.github.ericmedvet.jnb.datastructure.DoubleRange;
import io.github.ericmedvet.jsdynsym.control.Simulation;
import io.github.ericmedvet.mrsim2d.core.geometry.Point;
import io.github.ericmedvet.mrsim2d.core.geometry.Poly;
import java.util.HashMap;
import java.util.Map;
import java.util.SortedMap;

public class AgentsOutcome<O extends AgentsObservation> implements Simulation.Outcome<O> {

  private static final int N_OF_CACHED_SUB_OUTCOMES = 3;
  protected final SortedMap<Double, O> observations;
  private final Map<Key, Double> metricMap;
  private final Map<DoubleRange, AgentsOutcome<O>> subOutcomes;

  public AgentsOutcome(SortedMap<Double, O> observations) {
    this.observations = observations;
    metricMap = new HashMap<>();
    subOutcomes = new HashMap<>();
  }

  private enum Aggregate {
    INITIAL,
    FINAL,
    AVERAGE,
    MIN,
    MAX
  }

  private enum Metric {
    X,
    Y,
    AVG_X,
    TERRAIN_H,
    BB_W,
    BB_H,
    BB_AREA,
    BB_MAX_X,
    BB_MAX_Y,
    BB_MIN_X,
    BB_MIN_Y
  }

  private enum Subject {
    FIRST,
    ALL
  }

  private record Key(Metric metric, Aggregate aggregate, Subject subject) {}

  public double allAgentsAverageHeight() {
    return get(Aggregate.AVERAGE, Metric.BB_H, Subject.ALL);
  }

  public double allAgentsAverageWidth() {
    return get(Aggregate.AVERAGE, Metric.BB_W, Subject.ALL);
  }

  public double allAgentsFinalAverageWidth() {
    return get(Aggregate.FINAL, Metric.AVG_X, Subject.ALL);
  }

  public double allAgentsFinalMidrangeHeight() {
    return get(Aggregate.FINAL, Metric.Y, Subject.ALL);
  }

  public double allAgentsFinalMidrangeWidth() {
    return get(Aggregate.FINAL, Metric.X, Subject.ALL);
  }

  public double allAgentsFinalMaxHeight() {
    return get(Aggregate.FINAL, Metric.BB_MAX_Y, Subject.ALL);
  }

  public double allAgentsFinalMaxWidth() {
    return get(Aggregate.FINAL, Metric.BB_MAX_X, Subject.ALL);
  }

  public double allAgentsFinalMinHeight() {
    return get(Aggregate.FINAL, Metric.BB_MIN_Y, Subject.ALL);
  }

  public double allAgentsFinalMinWidth() {
    return get(Aggregate.FINAL, Metric.BB_MIN_X, Subject.ALL);
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

  public double firstAgentAverageBBMinY() {
    return get(Aggregate.AVERAGE, Metric.BB_MIN_Y, Subject.FIRST);
  }

  public double firstAgentMaxY() {
    return get(Aggregate.MAX, Metric.Y, Subject.FIRST);
  }

  public double firstAgentMaxBBMinY() {
    return get(Aggregate.MAX, Metric.BB_MIN_Y, Subject.FIRST);
  }

  public double firstAgentXDistance() {
    return get(Aggregate.FINAL, Metric.X, Subject.FIRST) - get(Aggregate.INITIAL, Metric.X, Subject.FIRST);
  }

  public double firstAgentXVelocity() {
    return firstAgentXDistance() / duration();
  }

  public double firstAgentMaxRelativeJumpHeight() {
    return get(Aggregate.MAX, Metric.BB_MIN_Y, Subject.FIRST) / get(Aggregate.AVERAGE, Metric.BB_H, Subject.FIRST);
  }

  private double get(Aggregate aggregate, Metric metric, Subject subject) {
    Double value = metricMap.get(new Key(metric, aggregate, subject));
    if (value == null) {
      value = switch (aggregate) {
        case FINAL -> get(metric, subject, observations.get(observations.lastKey()));
        case INITIAL -> get(metric, subject, observations.get(observations.firstKey()));
        case AVERAGE -> observations.values().stream()
            .mapToDouble(o -> get(metric, subject, o))
            .average()
            .orElse(0d);
        case MIN -> observations.values().stream()
            .mapToDouble(o -> get(metric, subject, o))
            .min()
            .orElse(0d);
        case MAX -> observations.values().stream()
            .mapToDouble(o -> get(metric, subject, o))
            .max()
            .orElse(0d);};
      metricMap.put(new Key(metric, aggregate, subject), value);
    }
    return value;
  }

  private double get(Metric metric, Subject subject, AgentsObservation observation) {
    return switch (metric) {
      case X -> subject.equals(Subject.FIRST)
          ? observation.getFirstAgentCenter().x()
          : observation.getAllBoundingBox().center().x();
      case Y -> subject.equals(Subject.FIRST)
          ? observation.getFirstAgentCenter().y()
          : observation.getAllBoundingBox().center().y();
      case AVG_X -> {
        if (subject.equals(Subject.FIRST)) {
          yield observation.getFirstAgentCenter().x();
        } else {
          yield observation.getAgents().stream()
              .mapToDouble(a -> Point.average(a.polies().stream()
                      .map(Poly::center)
                      .toArray(Point[]::new))
                  .x())
              .average()
              .orElse(0d);
        }
      }
      case TERRAIN_H -> {
        if (subject.equals(Subject.FIRST)) {
          yield observation.getFirstAgentCenter().y()
              - observation.getAgents().getFirst().terrainHeight();
        } else {
          yield observation.getAgents().stream()
              .mapToDouble(a -> Point.average(a.polies().stream()
                          .map(Poly::center)
                          .toArray(Point[]::new))
                      .y()
                  - a.terrainHeight())
              .average()
              .orElse(0d);
        }
      }
      case BB_AREA -> subject.equals(Subject.FIRST)
          ? observation.getFirstAgentBoundingBox().area()
          : observation.getAllBoundingBox().area();
      case BB_W -> subject.equals(Subject.FIRST)
          ? observation.getFirstAgentBoundingBox().width()
          : observation.getAllBoundingBox().width();
      case BB_H -> subject.equals(Subject.FIRST)
          ? observation.getFirstAgentBoundingBox().height()
          : observation.getAllBoundingBox().height();
      case BB_MIN_Y -> subject.equals(Subject.FIRST)
          ? observation.getFirstAgentBoundingBox().min().y()
          : observation.getAllBoundingBox().min().y();
      case BB_MAX_Y -> subject.equals(Subject.FIRST)
          ? observation.getFirstAgentBoundingBox().max().y()
          : observation.getAllBoundingBox().max().y();
      case BB_MIN_X -> subject.equals(Subject.FIRST)
          ? observation.getFirstAgentBoundingBox().min().x()
          : observation.getAllBoundingBox().min().x();
      case BB_MAX_X -> subject.equals(Subject.FIRST)
          ? observation.getFirstAgentBoundingBox().max().x()
          : observation.getAllBoundingBox().max().x();
    };
  }

  public AgentsOutcome<O> subOutcome(DoubleRange tRange) {
    AgentsOutcome<O> subOutcome = subOutcomes.get(tRange);
    if (subOutcome == null) {
      subOutcome = new AgentsOutcome<>(observations.subMap(tRange.min(), tRange.max()));
      if (subOutcomes.size() >= N_OF_CACHED_SUB_OUTCOMES) {
        // remove one
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

  @Override
  public SortedMap<Double, O> snapshots() {
    return observations;
  }
}
