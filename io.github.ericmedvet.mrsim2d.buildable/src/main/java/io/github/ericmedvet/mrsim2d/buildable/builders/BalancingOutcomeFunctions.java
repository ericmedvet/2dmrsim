package io.github.ericmedvet.mrsim2d.buildable.builders;

import io.github.ericmedvet.jnb.core.Param;
import io.github.ericmedvet.jsdynsym.core.DoubleRange;
import io.github.ericmedvet.mrsim2d.core.tasks.balancing.BalancingOutcome;

import java.util.function.Function;

public class BalancingOutcomeFunctions {

  private BalancingOutcomeFunctions() {
  }

  @SuppressWarnings("unused")
  public Function<BalancingOutcome, Double> avgSwingAngle(
      @Param(value = "transientTime", dD = 5.0) double transientTime
  ) {
    return o -> o.subOutcome(new DoubleRange(transientTime, o.duration()))
        .avgSwingAngle();
  }

  @SuppressWarnings("unused")
  public Function<BalancingOutcome, Double> avgSwingAngleWithMalus(
      @Param(value = "transientTime", dD = 5.0) double transientTime,
      @Param(value = "malus", dD = Math.PI / 2d) double malus
  ) {
    return o -> o.subOutcome(new DoubleRange(transientTime, o.duration()))
        .avgSwingAngleWithMalus(malus);
  }
}
