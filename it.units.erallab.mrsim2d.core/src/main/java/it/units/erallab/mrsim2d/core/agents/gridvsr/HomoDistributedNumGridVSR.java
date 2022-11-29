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

package it.units.erallab.mrsim2d.core.agents.gridvsr;

import it.units.erallab.mrsim2d.core.functions.TimedRealFunction;
import it.units.erallab.mrsim2d.core.util.Grid;
import it.units.erallab.mrsim2d.core.util.Parametrized;

import java.util.function.Supplier;

public class HomoDistributedNumGridVSR extends HeteroDistributedNumGridVSR {

  public HomoDistributedNumGridVSR(GridBody body, Supplier<TimedRealFunction> timedRealFunctionsSupplier, int nSignals, boolean directional) {
    super(body, body.grid().map(i -> timedRealFunctionsSupplier.get()), nSignals, directional);
  }

  @Override
  public double[] getParams() {
    for (Grid.Entry<TimedRealFunction> functionEntry : timedRealFunctionsGrid) {
      if (functionEntry.value() instanceof Parametrized parametrized) {
        return parametrized.getParams();
      }
    }
    return new double[0];
  }

  @Override
  public void setParams(double[] params) {
    for (Grid.Entry<TimedRealFunction> functionEntry : timedRealFunctionsGrid) {
      if (functionEntry.value() instanceof Parametrized parametrized) {
        parametrized.setParams(params);
      }
    }
  }

}
