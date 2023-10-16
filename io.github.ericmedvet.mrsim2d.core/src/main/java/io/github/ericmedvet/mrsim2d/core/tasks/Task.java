
package io.github.ericmedvet.mrsim2d.core.tasks;

import io.github.ericmedvet.mrsim2d.core.Snapshot;
import io.github.ericmedvet.mrsim2d.core.engine.Engine;

import java.util.function.Consumer;

public interface Task<A, O> {

  O run(A a, Engine engine, Consumer<Snapshot> snapshotConsumer);

  default O run(A a, Engine engine) {
    return run(a, engine, snapshot -> {});
  }
}
