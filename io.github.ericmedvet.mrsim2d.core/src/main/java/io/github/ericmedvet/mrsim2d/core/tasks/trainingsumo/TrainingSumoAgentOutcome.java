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
package io.github.ericmedvet.mrsim2d.core.tasks.trainingsumo;

import io.github.ericmedvet.jnb.datastructure.DoubleRange;
import io.github.ericmedvet.mrsim2d.core.geometry.Point;
import io.github.ericmedvet.mrsim2d.core.tasks.AgentsObservation;
import io.github.ericmedvet.mrsim2d.core.tasks.AgentsOutcome;
import java.util.List;
import java.util.SortedMap;
import java.util.stream.Collectors;

public class TrainingSumoAgentOutcome extends AgentsOutcome<TrainingSumoObservation> {

  public TrainingSumoAgentOutcome(SortedMap<Double, TrainingSumoObservation> observations) {
    super(observations);
  }

  public double getMaxYTerrain() {
    return observations.values().stream()
            .flatMap(obs -> obs.getAgents().stream())
            .mapToDouble(AgentsObservation.Agent::terrainHeight)
            .max()
            .orElse(Double.NaN);
  }

  public List<Point> getAgentPositions() {
    TrainingSumoObservation firstObservation = observations.firstEntry().getValue();
    TrainingSumoObservation lastObservation = observations.lastEntry().getValue();
    Point firstPosition = firstObservation.getCenters().getFirst();
    Point lastPosition = lastObservation.getCenters().getFirst();
    return List.of(firstPosition, lastPosition);
  }

  public List<Double> getAgentMaxY() {
    Double firstMaxY = observations.firstEntry().getValue().getBoundingBoxes().getFirst().max().y();
    Double lastMaxY = observations.lastEntry().getValue().getBoundingBoxes().getFirst().max().y();
    return List.of(firstMaxY, lastMaxY);
  }

  public List<Point> getBoxPositions() {
    TrainingSumoObservation firstObservation = observations.firstEntry().getValue();
    TrainingSumoObservation lastObservation = observations.lastEntry().getValue();
    Point firstPosition = firstObservation.getRigidBodyPosition();
    Point lastPosition = lastObservation.getRigidBodyPosition();
    return List.of(firstPosition, lastPosition);
  }

  public double getMaxYBox() {
    List<Point> boxPositions = getBoxPositions();
    if (boxPositions.isEmpty()) {
      return Double.NaN;
    }
    return boxPositions.stream().mapToDouble(Point::y).max().orElse(Double.NaN);
  }

  @Override
  public TrainingSumoAgentOutcome subOutcome(DoubleRange tRange) {
    return new TrainingSumoAgentOutcome(super.subOutcome(tRange).snapshots());
  }
}
