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
import io.github.ericmedvet.mrsim2d.core.functions.*;
import io.github.ericmedvet.mrsim2d.core.util.DoubleRange;

import java.util.List;
import java.util.function.BiFunction;
import java.util.random.RandomGenerator;
import java.util.stream.IntStream;

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
      @Param("innerFunction") Builder<? extends TimedRealFunction> innerFunction,
      @Param(value = "types", dSs = {"current", "trend", "avg"}) List<DiffInputTRF.Type> types
  ) {
    return (nOfInputs, nOfOutputs) -> new DiffInputTRF(
        innerFunction.apply(nOfInputs * types.size(), nOfOutputs),
        windowT,
        types
    );
  }

  @SuppressWarnings("unused")
  public static Builder<DelayedRecurrentNetwork> drn(
      @Param(value = "timeRange", dNPM = "sim.range(min=0;max=1)") DoubleRange timeRange,
      @Param(value = "innerNeuronsRatio", dD = 1d) double innerNeuronsRatio,
      @Param(value = "activationFunction", dS = "tanh") MultiLayerPerceptron.ActivationFunction activationFunction,
      @Param(value = "threshold", dD = 0.1d) double threshold,
      @Param(value = "timeResolution", dD = 0.16666d) double timeResolution
  ) {
    return (nOfInputs, nOfOutputs) -> new DelayedRecurrentNetwork(
        activationFunction,
        nOfInputs,
        nOfOutputs,
        (int) Math.round(innerNeuronsRatio * (nOfInputs + nOfOutputs)),
        timeRange,
        threshold,
        timeResolution
    );
  }

  @SuppressWarnings("unused")
  public static Builder<GroupedSinusoidal> groupedSin(
      @Param("size") int size,
      @Param(value = "p", dNPM = "sim.range(min=-1.57;max=1.57)") DoubleRange phaseRange,
      @Param(value = "f", dNPM = "sim.range(min=0;max=1)") DoubleRange frequencyRange,
      @Param(value = "a", dNPM = "sim.range(min=0;max=0.5)") DoubleRange amplitudeRange,
      @Param(value = "b", dNPM = "sim.range(min=-0.5;max=0.5)") DoubleRange biasRange,
      @Param(value = "s", dNPM = "sim.range(min=-0.5;max=0.5)") DoubleRange sumRange
  ) {
    return (nOfInputs, nOfOutputs) -> new GroupedSinusoidal(
        nOfInputs,
        IntStream.range(0, nOfOutputs / size)
            .mapToObj(i -> new GroupedSinusoidal.Group(
                size,
                amplitudeRange,
                frequencyRange,
                phaseRange,
                biasRange,
                sumRange
            ))
            .toList()
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
  public static Builder<NoisedTRF> noised(
      @Param(value = "inputSigma", dD = 0) double inputSigma,
      @Param(value = "outputSigma", dD = 0) double outputSigma,
      @Param(value = "randomGenerator", dNPM = "sim.defaultRG()") RandomGenerator randomGenerator,
      @Param("innerFunction") Builder<? extends TimedRealFunction> innerFunction
  ) {
    return (nOfInputs, nOfOutputs) -> new NoisedTRF(
        innerFunction.apply(nOfInputs, nOfOutputs),
        inputSigma,
        outputSigma,
        randomGenerator
    );
  }

  @SuppressWarnings("unused")
  public static Builder<Sinusoidal> sin(
      @Param(value = "p", dNPM = "sim.range(min=-1.57;max=1.57)") DoubleRange phaseRange,
      @Param(value = "f", dNPM = "sim.range(min=0;max=1)") DoubleRange frequencyRange,
      @Param(value = "a", dNPM = "sim.range(min=0;max=1)") DoubleRange amplitudeRange,
      @Param(value = "b", dNPM = "sim.range(min=-0.5;max=0.5)") DoubleRange biasRange
  ) {
    return (nOfInputs, nOfOutputs) -> new Sinusoidal(
        nOfInputs,
        nOfOutputs,
        phaseRange,
        frequencyRange,
        amplitudeRange,
        biasRange
    );
  }

  @SuppressWarnings("unused")
  public static Builder<SteppedOutputTRF> stepOut(
      @Param("stepT") double stepT,
      @Param("innerFunction") Builder<? extends TimedRealFunction> innerFunction
  ) {
    return (nOfInputs, nOfOutputs) -> new SteppedOutputTRF(
        innerFunction.apply(nOfInputs, nOfOutputs),
        stepT
    );
  }
}
