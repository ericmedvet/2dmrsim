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

package it.units.erallab.mrsim.util;

import java.io.Serializable;
import java.util.Arrays;

/**
 * @author "Eric Medvet" on 2022/07/08 for 2dmrsim
 */
public record DoubleRange(double min, double max) implements Serializable {

  public static DoubleRange UNIT = new DoubleRange(0, 1);

  public DoubleRange {
    if (max < min) {
      throw new IllegalArgumentException(String.format(
          "Max has to be lower or equal than min; %f is not than %f.",
          max,
          min
      ));
    }
  }

  public static DoubleRange of(double min, double max) {
    return new DoubleRange(min, max);
  }

  public double clip(double value) {
    return Math.min(Math.max(value, min), max);
  }

  public double extent() {
    return max - min;
  }

  public double normalize(double value) {
    return (clip(value) - min) / (max - min);
  }

  public double denormalize(double value) {
    return clip(value * extent() + min());
  }

}
