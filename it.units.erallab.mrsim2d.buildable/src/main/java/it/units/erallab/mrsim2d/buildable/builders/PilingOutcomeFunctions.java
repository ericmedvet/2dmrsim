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

package it.units.erallab.mrsim2d.buildable.builders;

import it.units.erallab.mrsim2d.core.tasks.piling.Outcome;
import it.units.erallab.mrsim2d.core.util.DoubleRange;
import it.units.malelab.jnb.core.Param;

import java.util.function.Function;

public class PilingOutcomeFunctions {

  private PilingOutcomeFunctions() {
  }

  @SuppressWarnings("unused")
  public static Function<Outcome, Double> avgH(
      @Param(value = "transientTime", dD = 0) double transientTime
  ) {
    return o -> o.subOutcome(new DoubleRange(transientTime, o.duration())).averageAverageHeight();
  }

  @SuppressWarnings("unused")
  public static Function<Outcome, Double> maxH(
      @Param(value = "transientTime", dD = 0) double transientTime
  ) {
    return o -> o.subOutcome(new DoubleRange(transientTime, o.duration())).averageMaxHeight();
  }


}
