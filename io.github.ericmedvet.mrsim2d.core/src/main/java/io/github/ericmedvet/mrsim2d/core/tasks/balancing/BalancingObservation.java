package io.github.ericmedvet.mrsim2d.core.tasks.balancing;

import io.github.ericmedvet.mrsim2d.core.tasks.AgentsObservation;

import java.util.List;

public class BalancingObservation extends AgentsObservation {
  private final double swingAngle;
  private final boolean swingTouchingGround;

  public BalancingObservation(List<Agent> agents, double swingAngle, boolean swingTouchingGround) {
    super(agents);
    this.swingAngle = swingAngle;
    this.swingTouchingGround = swingTouchingGround;
  }

  public double getSwingAngle() {
    return swingAngle;
  }

  public boolean isSwingTouchingGround() {
    return swingTouchingGround;
  }
}
