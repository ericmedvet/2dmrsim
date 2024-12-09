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
package io.github.ericmedvet.mrsim2d.core.tasks;

import io.github.ericmedvet.mrsim2d.core.Snapshot;
import io.github.ericmedvet.mrsim2d.core.engine.Engine;
import java.util.function.Consumer;

public interface SymmetricCompetitiveTask<A, S extends AgentsObservation, O extends AgentsOutcome<S>>
    extends Task<A, S, O> {
  O run(A a1, A a2, Engine engine, Consumer<Snapshot> snapshotConsumer);

  @Override
  default O run(A a, Engine engine, Consumer<Snapshot> snapshotConsumer) {
    return run(a, a, engine, snapshotConsumer);
  }
}
