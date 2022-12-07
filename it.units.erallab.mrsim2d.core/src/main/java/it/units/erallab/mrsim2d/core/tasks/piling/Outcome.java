/*
 * Copyright 2022 eric
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

package it.units.erallab.mrsim2d.core.tasks.piling;

import it.units.erallab.mrsim2d.core.geometry.Poly;
import it.units.erallab.mrsim2d.core.util.DoubleRange;

import java.util.*;

public final class Outcome {
  private final SortedMap<Double, Observation> observations;

  private Double averageAverageHeight;
  private Double averagePileHeight;


  public Outcome(SortedMap<Double, Observation> observations) {
    this.observations = Collections.unmodifiableSortedMap(observations);
  }

  public static final class Observation {
    private final List<List<Poly>> bodies;
    private Double avgCenterHeight;
    private Double pileHeight;

    public Observation(List<List<Poly>> bodies) {
      this.bodies = bodies;
    }

    public double avgCenterHeight() {
      if (avgCenterHeight == null) {
        if (bodies.isEmpty()) {
          avgCenterHeight = 0d;
        }
        double minY = bodies.stream()
            .flatMap(Collection::stream)
            .mapToDouble(p -> p.boundingBox().min().y())
            .min()
            .orElseThrow();
        avgCenterHeight = bodies.stream()
            .flatMap(Collection::stream)
            .mapToDouble(p -> p.boundingBox().center().y() - minY)
            .average()
            .orElseThrow();
      }
      return avgCenterHeight;
    }

    public List<List<Poly>> bodies() {
      return bodies;
    }

    @Override
    public int hashCode() {
      return Objects.hash(bodies);
    }

    @Override
    public boolean equals(Object obj) {
      if (obj == this)
        return true;
      if (obj == null || obj.getClass() != this.getClass())
        return false;
      var that = (Observation) obj;
      return Objects.equals(this.bodies, that.bodies);
    }

    @Override
    public String toString() {
      return "Observation[" +
          "bodies=" + bodies + ']';
    }

    public double pileHeight() {
      if (pileHeight == null) {
        if (bodies.isEmpty()) {
          pileHeight = 0d;
        }
        double minY = bodies.stream()
            .flatMap(Collection::stream)
            .mapToDouble(p -> p.boundingBox().min().y())
            .min()
            .orElseThrow();
        double maxY = bodies.stream()
            .flatMap(Collection::stream)
            .mapToDouble(p -> p.boundingBox().max().y())
            .max()
            .orElseThrow();
        pileHeight = maxY - minY;
      }
      return pileHeight;
    }

  }

  public double averageAverageHeight() {
    if (averageAverageHeight == null) {
      averageAverageHeight = observations.values().stream()
          .mapToDouble(Observation::avgCenterHeight)
          .average()
          .orElse(0d);
    }
    return averageAverageHeight;
  }

  public double averagePileHeight() {
    if (averagePileHeight == null) {
      averagePileHeight = observations.values().stream()
          .mapToDouble(Observation::pileHeight)
          .average()
          .orElse(0d);
    }
    return averagePileHeight;
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

  public Outcome subOutcome(DoubleRange tRange) {
    return new Outcome(observations.subMap(tRange.min(), tRange.max()));
  }

  @Override
  public String toString() {
    return "Outcome{" +
        "pileHeightDiff=" + pileHeightDiff() +
        '}';
  }

  @Override
  public int hashCode() {
    return Objects.hash(observations);
  }

  @Override
  public boolean equals(Object obj) {
    if (obj == this)
      return true;
    if (obj == null || obj.getClass() != this.getClass())
      return false;
    var that = (Outcome) obj;
    return Objects.equals(this.observations, that.observations);
  }

  public SortedMap<Double, Observation> observations() {
    return observations;
  }

}
