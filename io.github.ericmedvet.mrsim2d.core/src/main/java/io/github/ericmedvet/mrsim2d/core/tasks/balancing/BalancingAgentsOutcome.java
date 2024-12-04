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
package io.github.ericmedvet.mrsim2d.core.tasks.balancing;

import io.github.ericmedvet.jnb.datastructure.DoubleRange;
import io.github.ericmedvet.mrsim2d.core.tasks.AgentsOutcome;
import java.util.SortedMap;

public class BalancingAgentsOutcome extends AgentsOutcome<BalancingObservation> {
    public BalancingAgentsOutcome(SortedMap<Double, BalancingObservation> observations) {
        super(observations);
    }

    public double avgSwingAngle() {
        return snapshots().values().stream()
                .mapToDouble(bo -> Math.abs(bo.getSwingAngle()))
                .average()
                .orElseThrow(() -> new IllegalArgumentException("No observations: cannot compute average angle"));
    }

    public double avgSwingAngleWithMalus(double malus) {
        return snapshots().values().stream()
                .mapToDouble(bo -> Math.abs(bo.getSwingAngle()) + (bo.areAllAgentsOnSwing() ? 0 : malus))
                .average()
                .orElseThrow(() -> new IllegalArgumentException("No observations: cannot compute average angle"));
    }

    @Override
    public BalancingAgentsOutcome subOutcome(DoubleRange tRange) {
        return new BalancingAgentsOutcome(super.subOutcome(tRange).snapshots());
    }
}
