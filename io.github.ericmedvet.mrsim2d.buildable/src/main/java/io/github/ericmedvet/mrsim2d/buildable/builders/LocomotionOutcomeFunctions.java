
package io.github.ericmedvet.mrsim2d.buildable.builders;

import io.github.ericmedvet.jnb.core.Param;
import io.github.ericmedvet.jsdynsym.core.DoubleRange;
import io.github.ericmedvet.mrsim2d.core.tasks.Outcome;

import java.util.function.Function;

public class LocomotionOutcomeFunctions {

  private LocomotionOutcomeFunctions() {
  }

  @SuppressWarnings("unused")
  public static Function<Outcome<?>, Double> avgArea(
      @Param(value = "transientTime", dD = 0) double transientTime
  ) {
    return o -> o.subOutcome(new DoubleRange(transientTime, o.duration())).firstAgentAverageArea();
  }

  @SuppressWarnings("unused")
  public static Function<Outcome<?>, Double> avgTerrainHeight(
      @Param(value = "transientTime", dD = 0) double transientTime
  ) {
    return o -> o.subOutcome(new DoubleRange(transientTime, o.duration())).firstAgentAverageTerrainHeight();
  }

  @SuppressWarnings("unused")
  public static Function<Outcome<?>, Double> xDistance(
      @Param(value = "transientTime", dD = 0) double transientTime
  ) {
    return o -> o.subOutcome(new DoubleRange(transientTime, o.duration())).firstAgentXDistance();
  }

  @SuppressWarnings("unused")
  public static Function<Outcome<?>, Double> xVelocity(
      @Param(value = "transientTime", dD = 0) double transientTime
  ) {
    return o -> o.subOutcome(new DoubleRange(transientTime, o.duration())).firstAgentXVelocity();
  }

}
