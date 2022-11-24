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

package it.units.erallab.mrsim2d.core;

import it.units.erallab.mrsim2d.core.agents.gridvsr.CentralizedNumGridVSR;
import it.units.erallab.mrsim2d.core.agents.gridvsr.GridBody;
import it.units.erallab.mrsim2d.core.agents.gridvsr.HeteroDistributedNumGridVSR;
import it.units.erallab.mrsim2d.core.agents.gridvsr.HomoDistributedNumGridVSR;
import it.units.erallab.mrsim2d.core.agents.independentvoxel.NumIndependentVoxel;
import it.units.erallab.mrsim2d.core.agents.legged.AbstractLeggedHybridModularRobot;
import it.units.erallab.mrsim2d.core.agents.legged.NumLeggedHybridModularRobot;
import it.units.erallab.mrsim2d.core.builders.*;
import it.units.erallab.mrsim2d.core.tasks.locomotion.Locomotion;
import it.units.erallab.mrsim2d.core.tasks.piling.FallPiling;
import it.units.erallab.mrsim2d.core.tasks.piling.StandPiling;
import it.units.erallab.mrsim2d.core.util.DoubleRange;
import it.units.malelab.jnb.core.NamedBuilder;

import java.util.List;

public class PreparedNamedBuilder {

  private final static NamedBuilder<Object> NB = NamedBuilder.empty()
      .and(List.of("sim", "s"), NamedBuilder.empty()
          .and(NamedBuilder.fromClass(DoubleRange.class))
          .and(List.of("terrain", "t"), NamedBuilder.fromUtilityClass(Terrains.class))
          .and(List.of("task"), NamedBuilder.empty()
              .and(NamedBuilder.fromClass(Locomotion.class))
              .and(NamedBuilder.fromClass(FallPiling.class))
              .and(NamedBuilder.fromClass(StandPiling.class))
          )
          .and(List.of("agent", "a"), NamedBuilder.empty()
              .and(NamedBuilder.fromClass(CentralizedNumGridVSR.class))
              .and(NamedBuilder.fromClass(HeteroDistributedNumGridVSR.class))
              .and(NamedBuilder.fromClass(HomoDistributedNumGridVSR.class))
              .and(NamedBuilder.fromClass(NumIndependentVoxel.class))
              .and(NamedBuilder.fromClass(NumLeggedHybridModularRobot.class))
              .and(List.of("legged", "l"), NamedBuilder.empty()
                  .and(NamedBuilder.fromClass(AbstractLeggedHybridModularRobot.Module.class))
                  .and(NamedBuilder.fromClass(AbstractLeggedHybridModularRobot.LegChunk.class))
              )
          )
          .and(List.of("sensor", "s"), NamedBuilder.fromUtilityClass(Sensors.class))
          .and(List.of("function", "f"), NamedBuilder.fromUtilityClass(TimedRealFunctions.class))
          .and(List.of("vsr"), NamedBuilder.empty()
              .and(NamedBuilder.fromClass(GridBody.class))
              .and(List.of("shape", "s"), NamedBuilder.fromUtilityClass(GridShapes.class))
              .and(
                  List.of("sensorizingFunction", "sf"),
                  NamedBuilder.fromUtilityClass(VSRSensorizingFunctions.class)
              )
          ));

  public static NamedBuilder<Object> get() {
    return NB;
  }

}
