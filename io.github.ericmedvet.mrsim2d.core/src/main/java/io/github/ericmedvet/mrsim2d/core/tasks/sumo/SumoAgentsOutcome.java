package io.github.ericmedvet.mrsim2d.core.tasks.sumo;

import io.github.ericmedvet.jnb.datastructure.DoubleRange;
import io.github.ericmedvet.mrsim2d.core.tasks.AgentsOutcome;
import io.github.ericmedvet.mrsim2d.core.tasks.balancing.BalancingAgentsOutcome;
import io.github.ericmedvet.mrsim2d.core.tasks.balancing.BalancingObservation;

import java.util.SortedMap;

public class SumoAgentsOutcome extends AgentsOutcome<SumoObservation> {
    public SumoAgentsOutcome(SortedMap<Double, SumoObservation> observations) {
        super(observations);
    }

    public double progressiveDistance() {
        return snapshots().values().stream()
                .mapToDouble(bo -> Math.abs(bo.getAgent1Distance()))
                .average()
                .orElseThrow(() -> new IllegalArgumentException("No observations: cannot compute average angle"));
    }

    @Override
    public SumoAgentsOutcome subOutcome(DoubleRange tRange) {
        return new SumoAgentsOutcome(super.subOutcome(tRange).snapshots());
    }
}
