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
import io.github.ericmedvet.mrsim2d.core.Sensor;
import io.github.ericmedvet.mrsim2d.core.agents.gridvsr.CentralizedNumGridVSR;
import io.github.ericmedvet.mrsim2d.core.agents.gridvsr.DistributedNumGridVSR;
import io.github.ericmedvet.mrsim2d.core.agents.gridvsr.GridBody;
import io.github.ericmedvet.mrsim2d.core.agents.independentvoxel.NumIndependentVoxel;
import io.github.ericmedvet.mrsim2d.core.agents.legged.AbstractLeggedHybridModularRobot;
import io.github.ericmedvet.mrsim2d.core.agents.legged.AbstractLeggedHybridRobot;
import io.github.ericmedvet.mrsim2d.core.agents.legged.NumLeggedHybridModularRobot;
import io.github.ericmedvet.mrsim2d.core.agents.legged.NumLeggedHybridRobot;
import io.github.ericmedvet.mrsim2d.core.bodies.Voxel;
import io.github.ericmedvet.mrsim2d.core.util.Grid;

import java.util.List;

public class Agents {

  private Agents() {
  }

  @SuppressWarnings("unused")
  public static CentralizedNumGridVSR centralizedNumGridVSR(
      @Param("body") GridBody body,
      @Param("function") TimedRealFunctions.Builder<?> timedRealFunctionBuilder
  ) {
    return new CentralizedNumGridVSR(body, timedRealFunctionBuilder.apply(
        CentralizedNumGridVSR.nOfInputs(body),
        CentralizedNumGridVSR.nOfOutputs(body)
    ));
  }

  @SuppressWarnings("unused")
  public static DistributedNumGridVSR distributedNumGridVSR(
      @Param("body") GridBody body,
      @Param("function") TimedRealFunctions.Builder<?> timedRealFunctionBuilder,
      @Param("signals") int nSignals,
      @Param("directional") boolean directional
  ) {
    return new DistributedNumGridVSR(
        body,
        /*body.sensorsGrid().map(
            v -> v != null ?
                timedRealFunctionBuilder.apply(4 * nSignals + v.size(), 1 + (directional ? 4 * nSignals : nSignals))
                : null
        ),*/
        Grid.create(
            body.grid().w(),
            body.grid().h(),
            k -> body.grid().get(k).element().type().equals(GridBody.VoxelType.NONE) ?
                null :
                timedRealFunctionBuilder.apply(
                    DistributedNumGridVSR.nOfInputs(body, k, nSignals, directional),
                    DistributedNumGridVSR.nOfOutputs(body, k, nSignals, directional)
                )
        ),
        nSignals,
        directional
    );
  }

  @SuppressWarnings("unused")
  public static NumIndependentVoxel numIndependentVoxel(
      @Param("sensors") List<Sensor<? super Voxel>> sensors,
      @Param(value = "areaActuation", dS = "sides") NumIndependentVoxel.AreaActuation areaActuation,
      @Param(value = "attachActuation", dB = true) boolean attachActuation,
      @Param(value = "nOfNFCChannels", dI = 1) int nOfNFCChannels,
      @Param("function") TimedRealFunctions.Builder<?> timedRealFunctionBuilder
  ) {
    return new NumIndependentVoxel(
        sensors,
        areaActuation,
        attachActuation,
        nOfNFCChannels,
        timedRealFunctionBuilder.apply(
            NumIndependentVoxel.nOfInputs(sensors, nOfNFCChannels),
            NumIndependentVoxel.nOfOutputs(areaActuation, attachActuation, nOfNFCChannels)
        )
    );
  }

  @SuppressWarnings("unused")
  public static NumLeggedHybridModularRobot numLeggedHybridModularRobot(
      @Param("modules") List<AbstractLeggedHybridModularRobot.Module> modules,
      @Param("function") TimedRealFunctions.Builder<?> timedRealFunctionBuilder
  ) {
    return new NumLeggedHybridModularRobot(
        modules,
        timedRealFunctionBuilder.apply(
            NumLeggedHybridModularRobot.nOfInputs(modules),
            NumLeggedHybridModularRobot.nOfOutputs(modules)
        )
    );
  }

  @SuppressWarnings("unused")
  public static NumLeggedHybridRobot numLeggedHybridRobot(
      @Param("legs") List<AbstractLeggedHybridRobot.Leg> legs,
      @Param(value = "trunkLength", dD = 4 * LeggedMisc.TRUNK_LENGTH) double trunkLength,
      @Param(value = "trunkWidth", dD = LeggedMisc.TRUNK_WIDTH) double trunkWidth,
      @Param(value = "trunkMass", dD = 4 * LeggedMisc.TRUNK_MASS) double trunkMass,
      @Param(value = "headMass", dD = LeggedMisc.TRUNK_WIDTH * LeggedMisc.TRUNK_WIDTH * LeggedMisc.RIGID_DENSITY) double headMass,
      @Param("headSensors") List<Sensor<?>> headSensors,
      @Param("function") TimedRealFunctions.Builder<?> timedRealFunctionBuilder
  ) {
    return new NumLeggedHybridRobot(
        legs,
        trunkLength,
        trunkWidth,
        trunkMass,
        headMass,
        headSensors,
        timedRealFunctionBuilder.apply(
            NumLeggedHybridRobot.nOfInputs(legs, headSensors),
            NumLeggedHybridRobot.nOfOutputs(legs)
        )
    );
  }


}
