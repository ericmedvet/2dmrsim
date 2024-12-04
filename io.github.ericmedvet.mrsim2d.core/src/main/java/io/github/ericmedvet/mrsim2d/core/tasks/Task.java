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

import io.github.ericmedvet.jsdynsym.control.Simulation;
import io.github.ericmedvet.mrsim2d.core.Snapshot;
import io.github.ericmedvet.mrsim2d.core.engine.Engine;
import java.util.ServiceLoader;
import java.util.function.Consumer;

public interface Task<A, S extends AgentsObservation, O extends AgentsOutcome<S>> extends Simulation<A, S, O> {

    O run(A a, Engine engine, Consumer<Snapshot> snapshotConsumer);

    default O run(A a, Engine engine) {
        return run(a, engine, snapshot -> {});
    }

    @Override
    default O simulate(A a) {
        return run(a, ServiceLoader.load(Engine.class).findFirst().orElseThrow());
    }
}
