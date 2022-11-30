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

package it.units.erallab.mrsim2d.buildable;

import it.units.erallab.mrsim2d.buildable.builders.*;
import it.units.malelab.jnb.core.NamedBuilder;

import java.util.List;

public class PreparedNamedBuilder {
  private final static NamedBuilder<Object> NB = NamedBuilder.empty()
      .and(List.of("sim", "s"), NamedBuilder.empty()
          .and(NamedBuilder.fromUtilityClass(Misc.class))
          .and(List.of("sensor", "s"), NamedBuilder.fromUtilityClass(Sensors.class))
          .and(List.of("function", "f"), NamedBuilder.fromUtilityClass(TimedRealFunctions.class))
          .and(List.of("terrain", "t"), NamedBuilder.fromUtilityClass(Terrains.class))
          .and(List.of("agent", "a"), NamedBuilder.fromUtilityClass(Agents.class))
          .and(List.of("task"), NamedBuilder.fromUtilityClass(Tasks.class)
              .and(List.of("locomotion", "l"), NamedBuilder.fromUtilityClass(LocomotionOutcomeFunctions.class))
              .and(List.of("piling", "p"), NamedBuilder.fromUtilityClass(PilingOutcomeFunctions.class))
          )
          .and(List.of("vsr"), NamedBuilder.empty()
              .and(NamedBuilder.fromUtilityClass(VSRMisc.class))
              .and(List.of("shape", "s"), NamedBuilder.fromUtilityClass(GridShapes.class))
              .and(List.of("sensorizingFunction", "sf"), NamedBuilder.fromUtilityClass(VSRSensorizingFunctions.class))
          )
          .and(List.of("legged"), NamedBuilder.empty()
              .and(NamedBuilder.fromUtilityClass(LeggedMisc.class))
          )
      );

  private PreparedNamedBuilder() {
  }

  public static NamedBuilder<Object> get() {
    return NB;
  }

}
