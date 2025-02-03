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
package io.github.ericmedvet.mrsim2d.core.tasks.sumo;

import io.github.ericmedvet.jnb.datastructure.DoubleRange;
import io.github.ericmedvet.mrsim2d.core.geometry.Point;
import io.github.ericmedvet.mrsim2d.core.tasks.AgentsObservation;
import io.github.ericmedvet.mrsim2d.core.tasks.AgentsOutcome;
import java.util.SortedMap;

public class SumoAgentsOutcome extends AgentsOutcome<SumoAgentsObservation> {

  public SumoAgentsOutcome(SortedMap<Double, SumoAgentsObservation> observations) {
    super(observations);
  }

  public double getMaxYTerrain() {
    return observations.values().stream()
        .flatMap(obs -> obs.getAgents().stream())
        .mapToDouble(AgentsObservation.Agent::terrainHeight)
        .max()
        .orElse(Double.NaN);
  }

  public Point getAgent1InitialPosition() {
    return snapshots().values().stream()
        .map(observation -> observation.getCenters().getFirst())
        .findFirst()
        .orElseThrow(() -> new IllegalStateException("No observations available"));
  }

  public Point getAgent2InitialPosition() {
    return snapshots().values().stream()
        .map(observation -> observation.getCenters().getLast())
        .findFirst()
        .orElseThrow(() -> new IllegalStateException("No observations available"));
  }

  public Point getAgent1FinalPosition() {
    return snapshots().values().stream()
        .map(observation -> observation.getCenters().getFirst())
        .reduce((first, second) -> second)
        .orElseThrow(() -> new IllegalStateException("No observations available"));
  }

  public Point getAgent2FinalPosition() {
    return snapshots().values().stream()
        .map(observation -> observation.getCenters().getLast())
        .reduce((first, second) -> second)
        .orElseThrow(() -> new IllegalStateException("No observations available"));
  }

  public Double getAgent1InitialMaxY() {
    return snapshots().values().stream()
        .map(observation ->
            observation.getBoundingBoxes().getFirst().max().y())
        .findFirst()
        .orElseThrow(() -> new IllegalStateException("No observations available"));
  }

  public Double getAgent2InitialMaxY() {
    return snapshots().values().stream()
        .map(observation ->
            observation.getBoundingBoxes().getLast().max().y())
        .findFirst()
        .orElseThrow(() -> new IllegalStateException("No observations available"));
  }

  public Double getAgent1FinalMaxY() {
    return snapshots().values().stream()
        .map(observation ->
            observation.getBoundingBoxes().getFirst().max().y())
        .reduce((first, second) -> second)
        .orElseThrow(() -> new IllegalStateException("No observations available"));
  }

  public Double getAgent2FinalMaxY() {
    return snapshots().values().stream()
        .map(observation ->
            observation.getBoundingBoxes().getLast().max().y())
        .reduce((first, second) -> second)
        .orElseThrow(() -> new IllegalStateException("No observations available"));
  }

  @Override
  public SumoAgentsOutcome subOutcome(DoubleRange tRange) {
    return new SumoAgentsOutcome(super.subOutcome(tRange).snapshots());
  }
}
