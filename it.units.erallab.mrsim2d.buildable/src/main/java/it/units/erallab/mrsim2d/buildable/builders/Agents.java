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

import it.units.erallab.mrsim2d.core.Sensor;
import it.units.erallab.mrsim2d.core.agents.gridvsr.CentralizedNumGridVSR;
import it.units.erallab.mrsim2d.core.agents.gridvsr.DistributedNumGridVSR;
import it.units.erallab.mrsim2d.core.agents.gridvsr.GridBody;
import it.units.erallab.mrsim2d.core.agents.independentvoxel.NumIndependentVoxel;
import it.units.erallab.mrsim2d.core.agents.legged.AbstractLeggedHybridModularRobot;
import it.units.erallab.mrsim2d.core.agents.legged.NumLeggedHybridModularRobot;
import it.units.erallab.mrsim2d.core.bodies.Voxel;
import it.units.malelab.jnb.core.Param;

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
        body.sensorsGrid().map(
            v -> v != null ?
            timedRealFunctionBuilder.apply(4 * nSignals + v.size(), 1 + (directional ? 4 * nSignals : nSignals))
            : null
        ),
        nSignals,
        directional
    );
  }

  @SuppressWarnings("unused")
  public static NumIndependentVoxel numIndependentVoxel(
      @Param("sensors") List<Sensor<? super Voxel>> sensors,
      @Param("function") TimedRealFunctions.Builder<?> timedRealFunctionBuilder
  ) {
    return new NumIndependentVoxel(
        sensors,
        timedRealFunctionBuilder.apply(
            NumIndependentVoxel.nOfInputs(sensors),
            NumIndependentVoxel.nOfOutputs()
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


}
