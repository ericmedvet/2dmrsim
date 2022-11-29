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

package it.units.erallab.mrsim2d.core.agents.legged;

import it.units.erallab.mrsim2d.core.Action;
import it.units.erallab.mrsim2d.core.ActionOutcome;
import it.units.erallab.mrsim2d.core.actions.ActuateRotationalJoint;
import it.units.erallab.mrsim2d.core.functions.TimedRealFunction;
import it.units.erallab.mrsim2d.core.util.DoubleRange;
import it.units.erallab.mrsim2d.core.util.Parametrized;

import java.util.List;
import java.util.stream.IntStream;

/**
 * @author "Eric Medvet" on 2022/09/24 for 2dmrsim
 */
public class NumLeggedHybridModularRobot extends AbstractLeggedHybridModularRobot implements Parametrized {

  private final static DoubleRange ANGLE_RANGE = new DoubleRange(Math.toRadians(-90), Math.toRadians(90));

  private final TimedRealFunction timedRealFunction;

  public NumLeggedHybridModularRobot(List<Module> modules, TimedRealFunction timedRealFunction) {
    super(modules);
    this.timedRealFunction = timedRealFunction;
  }

  public static int nOfInputs(List<Module> modules) {
    return 0;
  }

  public static int nOfOutputs(List<Module> modules) {
    return modules.stream().mapToInt(m -> m.legChunks().size()).sum();
  }

  @Override
  public List<? extends Action<?>> act(double t, List<ActionOutcome<?, ?>> previousActionOutcomes) {
    double[] values = timedRealFunction.apply(t, new double[0]);
    if (values.length != rotationalJoints.size()) {
      throw new IllegalArgumentException("Unexpected function ouptut size: %d found vs. %d expected".formatted(
          values.length,
          rotationalJoints.size()
      ));
    }
    return IntStream.range(0, values.length)
        .mapToObj(i -> (Action<?>) new ActuateRotationalJoint(
            rotationalJoints.get(i),
            ANGLE_RANGE.clip(values[i])
        ))
        .toList();
  }

  public double[] getParams() {
    if (timedRealFunction instanceof Parametrized parametrized) {
      return parametrized.getParams();
    }
    return new double[0];
  }

  @Override
  public void setParams(double[] params) {
    if (timedRealFunction instanceof Parametrized parametrized) {
      parametrized.setParams(params);
    } else {
      if (params.length > 0) {
        throw new IllegalArgumentException("Cannot set params because the function %s has no params".formatted(
            timedRealFunction));
      }
    }
  }

}
