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

import java.util.Collection;
import java.util.List;
import java.util.SortedMap;

public record Outcome(SortedMap<Double, Observation> observations) {

  public record Observation(List<List<Poly>> bodies) {
    public double avgCenterHeight() {
      if (bodies.isEmpty()) {
        return 0d;
      }
      double minY = bodies.stream()
          .flatMap(Collection::stream)
          .mapToDouble(p -> p.boundingBox().min().y())
          .min()
          .orElseThrow();
      return bodies.stream()
          .flatMap(Collection::stream)
          .mapToDouble(p -> p.boundingBox().center().y() - minY)
          .average()
          .orElseThrow();
    }

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
          .mapToDouble(p -> p.boundingBox().max().y())
          .max()
          .orElseThrow();
      return maxY - minY;
    }
  }

  public double averageAverageHeight() {
    return observations.values().stream()
        .mapToDouble(Observation::avgCenterHeight)
        .average()
        .orElse(0d);
  }

  public double averageMaxHeight() {
    return observations.values().stream()
        .mapToDouble(Observation::pileHeight)
        .average()
        .orElse(0d);
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
}
