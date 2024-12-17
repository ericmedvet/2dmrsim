package io.github.ericmedvet.mrsim2d.core.tasks.trainingFight;

import io.github.ericmedvet.mrsim2d.core.EmbodiedAgent;
import io.github.ericmedvet.mrsim2d.core.Snapshot;
import io.github.ericmedvet.mrsim2d.core.XMirrorer;
import io.github.ericmedvet.mrsim2d.core.actions.AddAgent;
import io.github.ericmedvet.mrsim2d.core.actions.CreateUnmovableBody;
import io.github.ericmedvet.mrsim2d.core.actions.TranslateAgent;
import io.github.ericmedvet.mrsim2d.core.bodies.Body;
import io.github.ericmedvet.mrsim2d.core.engine.Engine;
import io.github.ericmedvet.mrsim2d.core.geometry.BoundingBox;
import io.github.ericmedvet.mrsim2d.core.geometry.Point;
import io.github.ericmedvet.mrsim2d.core.geometry.Terrain;
import io.github.ericmedvet.mrsim2d.core.tasks.AgentsObservation;
import io.github.ericmedvet.mrsim2d.core.tasks.BiTask;
import io.github.ericmedvet.mrsim2d.core.tasks.WrappedTask;
import io.github.ericmedvet.mrsim2d.core.util.PolyUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class TrainingFight implements WrappedTask<Supplier<EmbodiedAgent>, TrainingFightObservation, TrainingFightAgentsOutcome> {

    private static final double INITIAL_X_GAP = 7;
    private static final double INITIAL_Y_GAP = 0.25;
    private final double duration;
    private final Terrain terrain;
    private final double initialXGap;
    private final double initialYGap;
    private final BiTask<Supplier<EmbodiedAgent>, Supplier<EmbodiedAgent>, TrainingFightObservation, TrainingFightAgentsOutcome> biTask;

    public TrainingFight(double duration, Terrain terrain,
                         BiTask<Supplier<EmbodiedAgent>, Supplier<EmbodiedAgent>, TrainingFightObservation, TrainingFightAgentsOutcome> biTask,
                         double initialXGap, double initialYGap) {
        this.duration = duration;
        this.terrain = terrain;
        this.initialXGap = initialXGap;
        this.initialYGap = initialYGap;
        this.biTask = biTask;
    }

    public TrainingFight(double duration, Terrain terrain,
                         BiTask<Supplier<EmbodiedAgent>, Supplier<EmbodiedAgent>, TrainingFightObservation, TrainingFightAgentsOutcome> biTask) {
        this(duration, terrain, biTask, INITIAL_X_GAP, INITIAL_Y_GAP);
    }

    @Override
    public BiTask<Supplier<EmbodiedAgent>, Supplier<EmbodiedAgent>, TrainingFightObservation, TrainingFightAgentsOutcome> getBiTask() {
        return biTask;
    }

    @Override
    public Supplier<EmbodiedAgent> createPredefinedAgent() {

    }

    @Override
    public TrainingFightAgentsOutcome run(Supplier<EmbodiedAgent> embodiedAgentSupplier, Engine engine, Consumer<Snapshot> snapshotConsumer) {
        Supplier<EmbodiedAgent> predefinedAgentSupplier = createPredefinedAgent();
        EmbodiedAgent agent1 = embodiedAgentSupplier.get();
        EmbodiedAgent agent2 = predefinedAgentSupplier.get();
        engine.registerActionsFilter(agent2, new XMirrorer<>());

        // Build world
        engine.perform(new CreateUnmovableBody(terrain.poly()));
        engine.perform(new AddAgent(agent1));
        engine.perform(new AddAgent(agent2));

        // Place first agent
        BoundingBox agent1BB = agent1.boundingBox();
        engine.perform(new TranslateAgent(
                agent1,
                new Point(
                        terrain.withinBordersXRange().min()
                                + initialXGap
                                - agent1BB.min().x(),
                        0)));
        agent1BB = agent1.boundingBox();
        double maxY1 = terrain.maxHeightAt(agent1BB.xRange());
        double y1 = maxY1 + initialYGap - agent1BB.min().y();
        engine.perform(new TranslateAgent(agent1, new Point(0, y1)));

        // Place second agent
        BoundingBox agent2BB = agent2.boundingBox();
        engine.perform(new TranslateAgent(
                agent2,
                new Point(
                        terrain.withinBordersXRange().max()
                                - initialXGap * 2
                                - agent2BB.max().x(),
                        0)));
        agent2BB = agent2.boundingBox();
        double maxY2 = terrain.maxHeightAt(agent2BB.xRange());
        double y2 = maxY2 + initialYGap - agent2BB.min().y();
        engine.perform(new TranslateAgent(agent2, new Point(0, y2)));

        // Run for defined time
        Map<Double, TrainingFightObservation> observations = new HashMap<>();
        while (engine.t() < duration) {
            Snapshot snapshot = engine.tick();
            snapshotConsumer.accept(snapshot);

            observations.put(
                    engine.t(),
                    new TrainingFightObservation(
                            List.of(
                                    new AgentsObservation.Agent(
                                            agent1.bodyParts().stream().map(Body::poly).toList(),
                                            PolyUtils.maxYAtX(
                                                    terrain.poly(),
                                                    agent1.boundingBox().center().x())),
                                    new AgentsObservation.Agent(
                                            agent2.bodyParts().stream().map(Body::poly).toList(),
                                            PolyUtils.maxYAtX(
                                                    terrain.poly(),
                                                    agent2.boundingBox().center().x())))));
        }

        return new TrainingFightAgentsOutcome(new TreeMap<>(observations));
    }
}
