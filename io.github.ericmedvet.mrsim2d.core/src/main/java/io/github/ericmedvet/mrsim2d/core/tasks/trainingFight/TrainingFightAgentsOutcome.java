package io.github.ericmedvet.mrsim2d.core.tasks.trainingFight;

import io.github.ericmedvet.mrsim2d.core.tasks.AgentsOutcome;

import java.util.SortedMap;

public class TrainingFightAgentsOutcome extends AgentsOutcome<TrainingFightObservation> {
    public TrainingFightAgentsOutcome(SortedMap<Double, TrainingFightObservation> observations) {
        super(observations);
    }
}
