/*
 * Copyright 2022 eric
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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
