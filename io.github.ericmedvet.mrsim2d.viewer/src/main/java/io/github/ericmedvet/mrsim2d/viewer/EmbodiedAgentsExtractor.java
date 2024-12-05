/*-
 * ========================LICENSE_START=================================
 * mrsim2d-viewer
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
package io.github.ericmedvet.mrsim2d.viewer;

import io.github.ericmedvet.mrsim2d.core.*;
import io.github.ericmedvet.mrsim2d.core.bodies.Body;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.Function;

public class EmbodiedAgentsExtractor implements Function<Snapshot, List<Snapshot>> {

  private final List<EmbodiedAgent> agents;

  public EmbodiedAgentsExtractor() {
    this.agents = new ArrayList<>();
  }

  private record EmbodiedAgentSnapshot(
      Collection<ActionOutcome<?, ?>> actionOutcomes,
      Collection<Agent> agents,
      Collection<Body> bodies,
      Collection<NFCMessage> nfcMessages,
      double t)
      implements Snapshot {}

  private static EmbodiedAgentSnapshot from(EmbodiedAgent a, Snapshot s) {
    return new EmbodiedAgentSnapshot(
        s.actionOutcomes().stream().filter(ao -> a.equals(ao.agent())).toList(),
        List.of(a),
        s.bodies().stream().filter(b -> a.bodyParts().contains(b)).toList(),
        s.nfcMessages(),
        s.t());
  }

  @Override
  public List<Snapshot> apply(Snapshot snapshot) {
    // update agents memory
    snapshot.agents().stream()
        .filter(a -> a instanceof EmbodiedAgent)
        .filter(a -> !agents.contains(a))
        .forEach(a -> agents.add((EmbodiedAgent) a));
    // build snapshots
    return agents.stream().map(a -> (Snapshot) from(a, snapshot)).toList();
  }
}
