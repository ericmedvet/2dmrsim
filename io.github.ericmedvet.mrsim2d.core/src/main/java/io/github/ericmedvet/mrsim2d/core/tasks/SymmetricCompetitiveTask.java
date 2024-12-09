package io.github.ericmedvet.mrsim2d.core.tasks;

import io.github.ericmedvet.mrsim2d.core.Snapshot;
import io.github.ericmedvet.mrsim2d.core.engine.Engine;

import java.util.function.Consumer;

public interface SymmetricCompetitiveTask<A, S extends AgentsObservation, O extends AgentsOutcome<S>> extends Task<A, S, O> {
    O run(A a1, A a2, Engine engine, Consumer<Snapshot> snapshotConsumer);

    @Override
    default O run(A a, Engine engine, Consumer<Snapshot> snapshotConsumer) {
        return run(a, a, engine, snapshotConsumer);
    }
}
