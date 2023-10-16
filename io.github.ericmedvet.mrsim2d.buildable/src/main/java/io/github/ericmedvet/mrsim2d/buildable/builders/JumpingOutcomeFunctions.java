
package io.github.ericmedvet.mrsim2d.buildable.builders;

import io.github.ericmedvet.jnb.core.Param;
import io.github.ericmedvet.jsdynsym.core.DoubleRange;
import io.github.ericmedvet.mrsim2d.core.tasks.AgentsObservation;
import io.github.ericmedvet.mrsim2d.core.tasks.Outcome;

import java.util.function.Function;

public class JumpingOutcomeFunctions {

  private JumpingOutcomeFunctions() {
  }

  @SuppressWarnings("unused")
  public static Function<Outcome<?>, Double> avgBBMinY(
      @Param(value = "transientTime", dD = 5.0) double transientTime
  ) {
    return o -> o.subOutcome(new DoubleRange(transientTime, o.duration())).firstAgentAverageBBMinY();
  }

  @SuppressWarnings("unused")
  public static Function<Outcome<?>, Double> avgY(
      @Param(value = "transientTime", dD = 5.0) double transientTime
  ) {
    return o -> o.subOutcome(new DoubleRange(transientTime, o.duration())).firstAgentAverageY();
  }

  @SuppressWarnings("unused")
  public static Function<Outcome<?>, Double> maxBBMinY(
      @Param(value = "transientTime", dD = 5.0) double transientTime
  ) {
    return o -> o.subOutcome(new DoubleRange(transientTime, o.duration())).firstAgentMaxBBMinY();
  }

  @SuppressWarnings("unused")
  public static Function<Outcome<?>, Double> maxMaxRelJumpH(
      @Param(value = "transientTime", dD = 5.0) double transientTime
  ) {
    return o -> o.subOutcome(new DoubleRange(transientTime, o.duration())).firstAgentMaxRelativeJumpHeight();
  }

  @SuppressWarnings("unused")
  public static Function<Outcome<?>, Double> maxY(
      @Param(value = "transientTime", dD = 5.0) double transientTime
  ) {
    return o -> o.subOutcome(new DoubleRange(transientTime, o.duration())).firstAgentMaxY();
  }
}
