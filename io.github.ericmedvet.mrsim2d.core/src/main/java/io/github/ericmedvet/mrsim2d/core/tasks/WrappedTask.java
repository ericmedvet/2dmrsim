package io.github.ericmedvet.mrsim2d.core.tasks;

import io.github.ericmedvet.jsdynsym.control.Simulation;
import io.github.ericmedvet.mrsim2d.core.Snapshot;
import io.github.ericmedvet.mrsim2d.core.engine.Engine;
import io.github.ericmedvet.mrsim2d.core.tasks.trainingFight.TrainingFightObservation;

import java.util.ServiceLoader;
import java.util.function.Consumer;

public interface WrappedTask<A, S extends TrainingFightObservation, O extends AgentsOutcome<S>> extends Simulation<A, S, O> {

    BiTask<A, A, S, O> getBiTask();

    A createPredefinedAgent();

    O run(A a, Engine engine, Consumer<Snapshot> snapshotConsumer);

    default O run(A a, Engine engine) {
        return run(a, engine, snapshot -> {});
    }

    @Override
    default O simulate(A a) {
        return run(a, ServiceLoader.load(Engine.class).findFirst().orElseThrow());
    }
}
