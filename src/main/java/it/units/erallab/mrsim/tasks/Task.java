package it.units.erallab.mrsim.tasks;

import it.units.erallab.mrsim.core.Snapshot;
import it.units.erallab.mrsim.engine.Engine;

import java.util.function.Consumer;

public interface Task<A, O> {

  O run(A a, Engine engine, Consumer<Snapshot> snapshotConsumer);

  default O run(A a, Engine engine) {
    return run(a, engine, snapshot -> {});
  }
}
