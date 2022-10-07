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

package it.units.erallab.mrsim2d.core.functions;

import java.util.Collection;
import java.util.function.BiFunction;

/**
 * @author eric on 2021/03/09 for 2dhmsr
 */
public interface TimedRealFunction {
  double[] apply(double t, double[] input);

  int nOfInputs();

  int nOfOutputs();

  static TimedRealFunction from(BiFunction<Double, double[], double[]> f, int nOfInputs, int nOfOutputs) {
    return new TimedRealFunction() {
      @Override
      public double[] apply(double t, double[] input) {
        if (input.length != nOfInputs) {
          throw new IllegalArgumentException(String.format(
              "Unsupported input size: %d instead of %d",
              input.length,
              nOfInputs
          ));
        }
        double[] output = f.apply(t, input);
        if (output.length != nOfOutputs) {
          throw new IllegalArgumentException(String.format(
              "Unsupported output size: %d instead of %d",
              output.length,
              nOfOutputs
          ));
        }
        return output;
      }

      @Override
      public int nOfInputs() {
        return nOfInputs;
      }

      @Override
      public int nOfOutputs() {
        return nOfOutputs;
      }
    };
  }

  static TimedRealFunction zeros(int nOfInputs, int nOfOutputs) {
    return from((t, in) -> new double[nOfOutputs], nOfInputs, nOfOutputs);
  }

  default TimedRealFunction inputDiffed(double windowT, Collection<InputDiffTRF.Type> types) {
    return new InputDiffTRF(this, windowT, types);
  }

  default TimedRealFunction outputStepped(double stepT) {
    return new OutputSteppedTRF(this, stepT);
  }

}
