
package io.github.ericmedvet.mrsim2d.buildable.builders;

import io.github.ericmedvet.jnb.core.Param;
import io.github.ericmedvet.jsdynsym.core.DoubleRange;
import io.github.ericmedvet.mrsim2d.core.tasks.Outcome;

import java.util.function.Function;

public class PilingOutcomeFunctions {

  private PilingOutcomeFunctions() {
  }

  @SuppressWarnings("unused")
  public static Function<Outcome<?>, Double> avgH(
      @Param(value = "transientTime", dD = 0) double transientTime
  ) {
    return o -> o.subOutcome(new DoubleRange(transientTime, o.duration())).allAgentsAverageHeight();
  }

  @SuppressWarnings("unused")
  public static Function<Outcome<?>, Double> avgW(
      @Param(value = "transientTime", dD = 0) double transientTime
  ) {
    return o -> o.subOutcome(new DoubleRange(transientTime, o.duration())).allAgentsAverageWidth();
  }

  @SuppressWarnings("unused")
  public static Function<Outcome<?>, Double> maxH(
      @Param(value = "transientTime", dD = 0) double transientTime
  ) {
    return o -> o.subOutcome(new DoubleRange(transientTime, o.duration())).allAgentsMaxHeight();
  }

  @SuppressWarnings("unused")
  public static Function<Outcome<?>, Double> maxW(
      @Param(value = "transientTime", dD = 0) double transientTime
  ) {
    return o -> o.subOutcome(new DoubleRange(transientTime, o.duration())).allAgentsMaxWidth();
  }


}
