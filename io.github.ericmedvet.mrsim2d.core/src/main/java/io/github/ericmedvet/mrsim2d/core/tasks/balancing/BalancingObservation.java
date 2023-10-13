package io.github.ericmedvet.mrsim2d.core.tasks.balancing;

import io.github.ericmedvet.mrsim2d.core.geometry.BoundingBox;
import io.github.ericmedvet.mrsim2d.core.tasks.AgentsObservation;

import java.util.List;

public class BalancingObservation extends AgentsObservation {
  private final double swingAngle;
  private final boolean swingTouchingGround;
  private final BoundingBox swingBoundingBox;

  public BalancingObservation(
      List<Agent> agents,
      double swingAngle,
      boolean swingTouchingGround,
      BoundingBox swingBoundingBox
  ) {
    super(agents);
    this.swingAngle = swingAngle;
    this.swingTouchingGround = swingTouchingGround;
    this.swingBoundingBox = swingBoundingBox;
  }

  public double getSwingAngle() {
    return swingAngle;
  }

  public boolean isSwingTouchingGround() {
    return swingTouchingGround;
  }

  public boolean areAllAgentsOnSwing() {
    return getAllBoundingBox().xRange().contains(swingBoundingBox.xRange());
  }

  public BoundingBox getSwingBoundingBox() {
    return swingBoundingBox;
  }
}
