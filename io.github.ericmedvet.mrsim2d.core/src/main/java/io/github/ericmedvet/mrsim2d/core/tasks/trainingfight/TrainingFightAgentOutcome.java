/*-
 * ========================LICENSE_START=================================
 * mrsim2d-core
 * %%
 * Copyright (C) 2020 - 2025 Eric Medvet
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
package io.github.ericmedvet.mrsim2d.core.tasks.trainingfight;

import io.github.ericmedvet.jnb.datastructure.DoubleRange;
import io.github.ericmedvet.mrsim2d.core.geometry.Point;
import io.github.ericmedvet.mrsim2d.core.tasks.AgentsObservation;
import io.github.ericmedvet.mrsim2d.core.tasks.AgentsOutcome;
import java.util.List;
import java.util.SortedMap;

public class TrainingFightAgentOutcome extends AgentsOutcome<TrainingFightObservation> {

  public TrainingFightAgentOutcome(SortedMap<Double, TrainingFightObservation> observations) {
    super(observations);
  }

  public double getMaxYTerrain() {
    return observations.values().stream()
            .flatMap(obs -> obs.getAgents().stream())
            .mapToDouble(AgentsObservation.Agent::terrainHeight)
            .max()
            .orElse(Double.NaN);
  }

  public List<Point> getAgent1Positions() {
    TrainingFightObservation firstObservation = observations.firstEntry().getValue();
    TrainingFightObservation lastObservation = observations.lastEntry().getValue();
    Point firstPosition = firstObservation.getCenters().getFirst();
    Point lastPosition = lastObservation.getCenters().getFirst();
    return List.of(firstPosition, lastPosition);
  }

  public List<Point> getAgent2Positions() {
    TrainingFightObservation firstObservation = observations.firstEntry().getValue();
    TrainingFightObservation lastObservation = observations.lastEntry().getValue();
    Point firstPosition = firstObservation.getCenters().getLast();
    Point lastPosition = lastObservation.getCenters().getLast();
    return List.of(firstPosition, lastPosition);
  }

  public List<Double> getAgent1MaxY() {
    Double firstMaxY = observations.firstEntry().getValue().getBoundingBoxes().getFirst().max().y();
    Double lastMaxY = observations.lastEntry().getValue().getBoundingBoxes().getFirst().max().y();
    return List.of(firstMaxY, lastMaxY);
  }

  public List<Double> getAgent2MaxY() {
    Double firstMaxY = observations.firstEntry().getValue().getBoundingBoxes().getLast().max().y();
    Double lastMaxY = observations.lastEntry().getValue().getBoundingBoxes().getLast().max().y();
    return List.of(firstMaxY, lastMaxY);
  }

  @Override
  public TrainingFightAgentOutcome subOutcome(DoubleRange tRange) {
    return new TrainingFightAgentOutcome(super.subOutcome(tRange).snapshots());
  }
}
