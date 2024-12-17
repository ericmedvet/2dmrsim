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
import java.util.List;
import java.util.SortedMap;

public class SumoAgentsOutcome extends AgentsOutcome<AgentsObservation> {

  public SumoAgentsOutcome(SortedMap<Double, AgentsObservation> observations) {
    super(observations);
  }

  public List<Point> getAgent1Positions() {
    return snapshots().values().stream()
        .map(observation -> observation.getCenters().getFirst()) // Assumendo agent1 primo nella list
        .toList();
  }

  public List<Point> getAgent2Positions() {
    return snapshots().values().stream()
        .map(observation -> observation.getCenters().getLast()) // Asumendo agent2 secondo nella lits
        .toList();
  }

  public double getTotalDistance() {
    double distanceAgent1 = getAgent1Positions().getLast().x()
        - getAgent1Positions().getFirst().x();
    double distanceAgent2 = getAgent2Positions().getLast().x()
        - getAgent2Positions().getFirst().x();
    return distanceAgent1 + distanceAgent2;
  }

  public double getHigherAgent() {
    double yAgent1 = getAgent1Positions().getLast().y()
        - getAgent1Positions().getFirst().y();
    double yAgent2 = getAgent2Positions().getLast().y()
        - getAgent2Positions().getFirst().y();
    return yAgent1 + yAgent2;
  }

  @Override
  public SumoAgentsOutcome subOutcome(DoubleRange tRange) {
    return new SumoAgentsOutcome(super.subOutcome(tRange).snapshots());
  }
}
