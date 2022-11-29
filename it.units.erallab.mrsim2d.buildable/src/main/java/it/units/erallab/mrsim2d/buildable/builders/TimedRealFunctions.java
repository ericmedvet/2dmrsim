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

import it.units.erallab.mrsim2d.core.functions.*;
import it.units.erallab.mrsim2d.core.util.DoubleRange;
import it.units.malelab.jnb.core.Param;

import java.util.EnumSet;
import java.util.List;
import java.util.function.BiFunction;

/**
 * @author "Eric Medvet" on 2022/10/03 for 2dmrsim
 */
public class TimedRealFunctions {

  private TimedRealFunctions() {
  }

  public interface Builder<F extends TimedRealFunction> extends BiFunction<Integer, Integer, F> {}

  @SuppressWarnings("unused")
  public static Builder<DiffInputTRF> diffIn(
      @Param("windowT") double windowT,
      @Param("innerFunction") BiFunction<Integer, Integer, ? extends TimedRealFunction> innerFunction,
      @Param(value = "types", dSs = {"current", "trend", "avg"}) List<DiffInputTRF.Type> types
  ) {
    return (nOfInputs, nOfOutputs) -> new DiffInputTRF(
        innerFunction.apply(nOfInputs * types.size(), nOfOutputs),
        windowT,
        types
    );
  }

  @SuppressWarnings("unused")
  public static Builder<MultiLayerPerceptron> mlp(
      @Param(value = "innerLayerRatio", dD = 0.65) double innerLayerRatio,
      @Param(value = "nOfInnerLayers", dI = 1) int nOfInnerLayers,
      @Param(value = "activationFunction", dS = "tanh") MultiLayerPerceptron.ActivationFunction activationFunction
  ) {
    return (nOfInputs, nOfOutputs) -> {
      int[] innerNeurons = new int[nOfInnerLayers];
      int centerSize = (int) Math.max(2, Math.round(nOfInputs * innerLayerRatio));
      if (nOfInnerLayers > 1) {
        for (int i = 0; i < nOfInnerLayers / 2; i++) {
          innerNeurons[i] = nOfInputs + (centerSize - nOfInputs) / (nOfInnerLayers / 2 + 1) * (i + 1);
        }
        for (int i = nOfInnerLayers / 2; i < nOfInnerLayers; i++) {
          innerNeurons[i] =
              centerSize + (nOfOutputs - centerSize) / (nOfInnerLayers / 2 + 1) * (i - nOfInnerLayers / 2);
        }
      } else if (nOfInnerLayers > 0) {
        innerNeurons[0] = centerSize;
      }
      return new MultiLayerPerceptron(
          activationFunction,
          nOfInputs,
          innerNeurons,
          nOfOutputs
      );
    };
  }

  @SuppressWarnings("unused")
  public static Builder<Sinusoidal> sinP(
      @Param(value = "p", dNPM = "sim.doubleRange(min=-1.57;max=1.57)") DoubleRange phaseRange,
      @Param(value = "f", dNPM = "sim.doubleRange(min=0;max=1)") DoubleRange frequencyRange,
      @Param(value = "a", dNPM = "sim.doubleRange(min=0;max=1)") DoubleRange amplitudeRange
  ) {
    return (nOfInputs, nOfOutputs) -> new Sinusoidal(
        nOfInputs,
        nOfOutputs,
        EnumSet.of(Sinusoidal.Type.PHASE),
        phaseRange,
        frequencyRange,
        amplitudeRange
    );
  }

  @SuppressWarnings("unused")
  public static Builder<Sinusoidal> sinPA(
      @Param(value = "p", dNPM = "sim.doubleRange(min=-1.57;max=1.57)") DoubleRange phaseRange,
      @Param(value = "f", dNPM = "sim.doubleRange(min=0;max=1)") DoubleRange frequencyRange,
      @Param(value = "a", dNPM = "sim.doubleRange(min=0;max=1)") DoubleRange amplitudeRange
  ) {
    return (nOfInputs, nOfOutputs) -> new Sinusoidal(
        nOfInputs,
        nOfOutputs,
        EnumSet.of(Sinusoidal.Type.PHASE, Sinusoidal.Type.AMPLITUDE),
        phaseRange,
        frequencyRange,
        amplitudeRange
    );
  }

  @SuppressWarnings("unused")
  public static Builder<Sinusoidal> sinPF(
      @Param(value = "p", dNPM = "sim.doubleRange(min=-1.57;max=1.57)") DoubleRange phaseRange,
      @Param(value = "f", dNPM = "sim.doubleRange(min=0;max=1)") DoubleRange frequencyRange,
      @Param(value = "a", dNPM = "sim.doubleRange(min=0;max=1)") DoubleRange amplitudeRange
  ) {
    return (nOfInputs, nOfOutputs) -> new Sinusoidal(
        nOfInputs,
        nOfOutputs,
        EnumSet.of(Sinusoidal.Type.PHASE, Sinusoidal.Type.FREQUENCY),
        phaseRange,
        frequencyRange,
        amplitudeRange
    );
  }

  @SuppressWarnings("unused")
  public static Builder<Sinusoidal> sinPFA(
      @Param(value = "p", dNPM = "sim.doubleRange(min=-1.57;max=1.57)") DoubleRange phaseRange,
      @Param(value = "f", dNPM = "sim.doubleRange(min=0;max=1)") DoubleRange frequencyRange,
      @Param(value = "a", dNPM = "sim.doubleRange(min=0;max=1)") DoubleRange amplitudeRange
  ) {
    return (nOfInputs, nOfOutputs) -> new Sinusoidal(
        nOfInputs,
        nOfOutputs,
        EnumSet.of(Sinusoidal.Type.PHASE, Sinusoidal.Type.FREQUENCY, Sinusoidal.Type.AMPLITUDE),
        phaseRange,
        frequencyRange,
        amplitudeRange
    );
  }

  @SuppressWarnings("unused")
  public static Builder<SteppedOutputTRF> stepOut(
      @Param("stepT") double stepT,
      @Param("innerFunction") BiFunction<Integer, Integer, ? extends TimedRealFunction> innerFunction
  ) {
    return (nOfInputs, nOfOutputs) -> new SteppedOutputTRF(
        innerFunction.apply(nOfInputs, nOfOutputs),
        stepT
    );
  }
}
