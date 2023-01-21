/*
 * Copyright 2022 Eric Medvet <eric.medvet@gmail.com> (as eric)
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

package io.github.ericmedvet.mrsim2d.core.engine;

import io.github.ericmedvet.mrsim2d.core.ActionOutcome;
import io.github.ericmedvet.mrsim2d.core.Agent;
import io.github.ericmedvet.mrsim2d.core.NFCMessage;
import io.github.ericmedvet.mrsim2d.core.Snapshot;
import io.github.ericmedvet.mrsim2d.core.bodies.Body;

import java.util.Collection;
import java.util.Map;

/**
 * @author "Eric Medvet" on 2022/07/07 for 2dmrsim
 */
public record EngineSnapshot(
    double t,
    Collection<Body> bodies,
    Collection<Agent> agents,
    Collection<ActionOutcome<?, ?>> actionOutcomes,
    Collection<NFCMessage> nfcMessages,
    Map<TimeType, Double> times,
    Map<CounterType, Integer> counters
) implements Snapshot {
  public enum CounterType {TICK, ACTION, ILLEGAL_ACTION, UNSUPPORTED_ACTION}

  public enum TimeType {ENVIRONMENT, WALL, TICK, INNER_TICK, PERFORM}

}
