/*-
 * ========================LICENSE_START=================================
 * mrsim2d-core
 * %%
 * Copyright (C) 2020 - 2025 Eric Medvet
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

import io.github.ericmedvet.jnb.datastructure.DoubleRange;
import io.github.ericmedvet.jsdynsym.control.Simulation;
import io.github.ericmedvet.mrsim2d.core.Snapshot;
import io.github.ericmedvet.mrsim2d.core.engine.ConfigurableEngine;
import io.github.ericmedvet.mrsim2d.core.engine.Engine;
import java.util.ServiceLoader;
import java.util.function.Consumer;

public interface Task<A, S extends AgentsObservation, O extends AgentsOutcome<S>> extends Simulation<A, S, O> {

  O run(A a, double duration, Engine engine, Consumer<Snapshot> snapshotConsumer);

  default O run(A a, double duration, Engine engine) {
    return run(a, duration, engine, snapshot -> {});
  }

  @Override
  default O simulate(A a, double dT, DoubleRange tRange) {
    if (tRange.min() != 0) {
      throw new IllegalArgumentException(
          "Unsupported non-zero starting time: tRange=%s".formatted(tRange)
      );
    }
    Engine engine = ServiceLoader.load(Engine.class).findFirst().orElseThrow();
    if (engine instanceof ConfigurableEngine configurableEngine) {
      configurableEngine.setTimeStep(dT);
    } else {
      throw new UnsupportedOperationException(
          "Engine %s does not support setting the time step".formatted(engine.getClass().getSimpleName())
      );
    }
    return run(a, tRange.max(), engine);
  }
}
