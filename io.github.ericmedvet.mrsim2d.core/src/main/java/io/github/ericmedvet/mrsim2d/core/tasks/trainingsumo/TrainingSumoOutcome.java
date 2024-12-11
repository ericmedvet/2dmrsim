package io.github.ericmedvet.mrsim2d.core.tasks.trainingsumo;

import io.github.ericmedvet.mrsim2d.core.tasks.AgentsObservation;
import io.github.ericmedvet.mrsim2d.core.tasks.AgentsOutcome;

import java.util.SortedMap;

public class TrainingSumoOutcome extends AgentsOutcome<AgentsObservation> {

    public TrainingSumoOutcome(SortedMap<Double, AgentsObservation> observations) {
        super(observations);
    }
}
