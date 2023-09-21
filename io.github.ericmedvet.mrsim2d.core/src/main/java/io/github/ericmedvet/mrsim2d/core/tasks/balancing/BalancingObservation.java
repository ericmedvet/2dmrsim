package io.github.ericmedvet.mrsim2d.core.tasks.balancing;

import io.github.ericmedvet.mrsim2d.core.tasks.AgentsObservation;

import java.util.List;

public class BalancingObservation extends AgentsObservation {
  private final double swingAngle;

  public BalancingObservation(List<Agent> agents, double swingAngle) {
    super(agents);
    this.swingAngle = swingAngle;
  }

  public double getSwingAngle() {
    return swingAngle;
  }
}
