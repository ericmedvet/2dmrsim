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

import io.github.ericmedvet.mrsim2d.core.geometry.BoundingBox;
import io.github.ericmedvet.mrsim2d.core.tasks.AgentsObservation;

import java.util.List;

public class SumoObservation extends AgentsObservation {
    private final double agent1Distance;
    private final double agent2Distance;
    private final double agent1InitialPosition;
    private final double agent2InitialPosition;
    private final double agent1FinalPosition;
    private final double agent2FinalPosition;

    public SumoObservation(
            List<Agent> agents,
            double agent1Distance,
            double agent2Distance,
            double agent1InitialPosition,
            double agent1FinalPosition,
            double agent2InitialPosition,
            double agent2FinalPosition) {
        super(agents);
        this.agent1Distance = agent1Distance;
        this.agent2Distance = agent2Distance;
        this.agent1InitialPosition = agent1InitialPosition;
        this.agent1FinalPosition = agent1FinalPosition;
        this.agent2InitialPosition = agent2InitialPosition;
        this.agent2FinalPosition = agent2FinalPosition;
    }

    public double getAgent1Distance() {
        return agent1Distance;
    }

    public double getAgent2Distance() {
        return agent2Distance;
    }

    public double getAgent1InitialPosition() {
        return agent1InitialPosition;
    }

    public double getAgent1FinalPosition() {
        return agent1FinalPosition;
    }

    public double getAgent2InitialPosition() {
        return agent2InitialPosition;
    }

    public double getAgent2FinalPosition() {
        return agent2FinalPosition;
    }
}
