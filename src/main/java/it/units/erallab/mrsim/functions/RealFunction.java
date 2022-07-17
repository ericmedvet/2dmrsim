/*
 * Copyright 2022 Eric Medvet <eric.medvet@gmail.com> (as eric)
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

package it.units.erallab.mrsim.functions;

import java.util.function.Function;

/**
 * @author eric on 2021/03/09 for 2dhmsr
 */
public interface RealFunction extends TimedRealFunction {
  double[] apply(double[] input);

  static RealFunction build(
      Function<double[], double[]> function,
      int nOfInputs,
      int nOfOutputs
  ) {
    return new RealFunction() {
      @Override
      public double[] apply(double[] input) {
        return function.apply(input);
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

  @Override
  default double[] apply(double t, double[] input) {
    return apply(input);
  }

}
