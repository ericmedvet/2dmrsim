package io.github.ericmedvet.mrsim2d.core.tasks.balancing;

import io.github.ericmedvet.jsdynsym.core.DoubleRange;
import io.github.ericmedvet.mrsim2d.core.tasks.Outcome;

import java.util.SortedMap;

public class BalancingOutcome extends Outcome<BalancingObservation> {
  public BalancingOutcome(SortedMap<Double, BalancingObservation> observations) {
    super(observations);
  }

  public double avgSwingAngle() {
    return getObservations().values()
        .stream()
        .mapToDouble(bo -> Math.abs(bo.getSwingAngle()))
        .average()
        .orElseThrow(() -> new IllegalArgumentException("No observations: cannot compute average angle"));
  }

  public double avgSwingAngleWithMalus(double malus) {
    return getObservations().values()
        .stream()
        .mapToDouble(bo -> Math.abs(bo.getSwingAngle()) + (bo.areAllAgentsOnSwing() ? 0 : malus))
        .average()
        .orElseThrow(() -> new IllegalArgumentException("No observations: cannot compute average angle"));
  }

  @Override
  public BalancingOutcome subOutcome(DoubleRange tRange) {
    return new BalancingOutcome(super.subOutcome(tRange).getObservations());
  }

}
