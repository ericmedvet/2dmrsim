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

import io.github.ericmedvet.mrsim2d.core.tasks.AgentsObservation;
import io.github.ericmedvet.mrsim2d.core.tasks.AgentsOutcome;

import java.util.ArrayList;
import java.util.List;
import java.util.SortedMap;

public class SumoAgentsOutcome extends AgentsOutcome<AgentsObservation> {

  public SumoAgentsOutcome(SortedMap<Double, AgentsObservation> observations) {
    super(observations);
  }

  public List<AgentsObservation.Agent> getAgent1Outcome() {
    List<AgentsObservation.Agent> agent1Observations = new ArrayList<>();

    for (AgentsObservation observation : observations.values()) {
      List<AgentsObservation.Agent> agents = observation.getAgents();
      if (agents != null && !agents.isEmpty()) {
        agent1Observations.add(agents.getFirst());
      }
    }

    return agent1Observations;
  }

  public List<AgentsObservation.Agent> getAgent2Outcome() {
    List<AgentsObservation.Agent> agent2Observations = new ArrayList<>();

    for (AgentsObservation observation : observations.values()) {
      List<AgentsObservation.Agent> agents = observation.getAgents();
      if (agents != null && agents.size() > 1) {
        agent2Observations.add(agents.get(1));
      }
    }

    return agent2Observations;
  }
}
