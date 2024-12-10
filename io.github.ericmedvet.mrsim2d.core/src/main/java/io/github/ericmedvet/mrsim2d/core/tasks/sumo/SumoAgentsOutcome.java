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
import io.github.ericmedvet.mrsim2d.core.tasks.AgentsOutcome;
import java.util.SortedMap;

public class SumoAgentsOutcome extends AgentsOutcome<SumoObservation> {
  public SumoAgentsOutcome(SortedMap<Double, SumoObservation> observations) {
    super(observations);
  }

  public double progressiveDistance() {
    return snapshots().values().stream()
        .mapToDouble(bo -> Math.abs(bo.getAgent1Distance()))
        .average()
        .orElseThrow(() -> new IllegalArgumentException("No observations: cannot compute average angle"));
  }

  @Override
  public SumoAgentsOutcome subOutcome(DoubleRange tRange) {
    return new SumoAgentsOutcome(super.subOutcome(tRange).snapshots());
  }
}
