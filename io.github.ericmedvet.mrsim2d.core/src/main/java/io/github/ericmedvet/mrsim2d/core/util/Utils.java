/*-
 * ========================LICENSE_START=================================
 * mrsim2d-core
 * %%
 * Copyright (C) 2020 - 2023 Eric Medvet
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * =========================LICENSE_END==================================
 */

package io.github.ericmedvet.mrsim2d.core.util;

import java.util.List;

public class Utils {

  private Utils() {}

  public static double[] concat(double[] a1, double[] a2) {
    double[] a = new double[a1.length + a2.length];
    System.arraycopy(a1, 0, a, 0, a1.length);
    System.arraycopy(a2, 0, a, a1.length, a2.length);
    return a;
  }

  public static double[] concat(List<double[]> as) {
    int s = as.stream().mapToInt(a -> a.length).sum();
    double[] a = new double[s];
    int c = 0;
    for (double[] doubles : as) {
      System.arraycopy(doubles, 0, a, c, doubles.length);
      c = c + doubles.length;
    }
    return a;
  }
}
