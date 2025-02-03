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
import java.util.Arrays;
import java.util.List;
import java.util.SortedMap;

public class TrainingSumoAgentOutcome extends AgentsOutcome<TrainingSumoObservation> {

  public TrainingSumoAgentOutcome(SortedMap<Double, TrainingSumoObservation> observations) {
    super(observations);
  }

  public List<Point> getAgentPositions() {
    return List.of(
        snapshots().firstEntry().getValue().getCenters().getFirst(),
        snapshots().lastEntry().getValue().getCenters().getFirst()
    );
  }

  public List<Point> getBoxPositions() {
    return List.of(
        snapshots()
            .firstEntry()
            .getValue()
            .getRigidBodyPoly()
            .boundingBox()
            .center(),
        snapshots()
            .lastEntry()
            .getValue()
            .getRigidBodyPoly()
            .boundingBox()
            .center()
    );
  }

  public double getMaxYTerrain() {
    return observations.values()
        .stream()
        .flatMap(obs -> obs.getAgents().stream())
        .mapToDouble(AgentsObservation.Agent::terrainHeight)
        .max()
        .orElse(Double.NaN);
  }

  public double getMaxYBox() {
    return snapshots().values()
        .stream()
        .mapToDouble(
            obs -> Arrays.stream(obs.getRigidBodyPoly().vertexes())
                .mapToDouble(Point::y)
                .max()
                .orElse(Double.NaN)
        )
        .max()
        .orElse(Double.NaN);
  }

  @Override
  public TrainingSumoAgentOutcome subOutcome(DoubleRange tRange) {
    return new TrainingSumoAgentOutcome(super.subOutcome(tRange).snapshots());
  }
}
