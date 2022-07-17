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

/**
 * @author "Eric Medvet" on 2022/07/17 for 2dmrsim
 */
public class SteppedTimedRealFunction implements TimedRealFunction {

  private final TimedRealFunction innerF;
  private final double step;
  private double lastT;
  private double[] lastOutputs;

  public SteppedTimedRealFunction(TimedRealFunction innerF, double step) {
    this.innerF = innerF;
    lastT = Double.NEGATIVE_INFINITY;
    this.step = step;
  }

  @Override
  public double[] apply(double t, double[] input) {
    if (t - lastT > step) {
      lastOutputs = innerF.apply(t, input);
      lastT = t;
    }
    return lastOutputs;
  }

  @Override
  public int nOfInputs() {
    return innerF.nOfInputs();
  }

  @Override
  public int nOfOutputs() {
    return innerF.nOfOutputs();
  }
}
