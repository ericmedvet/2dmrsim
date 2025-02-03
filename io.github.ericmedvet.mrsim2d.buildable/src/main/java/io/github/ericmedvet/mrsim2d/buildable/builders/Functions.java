/*-
 * ========================LICENSE_START=================================
 * mrsim2d-buildable
 * %%
 * Copyright (C) 2020 - 2024 Eric Medvet
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
package io.github.ericmedvet.mrsim2d.buildable.builders;

import io.github.ericmedvet.jnb.core.Discoverable;
import io.github.ericmedvet.jnb.core.Param;
import io.github.ericmedvet.jnb.datastructure.FormattedNamedFunction;
import io.github.ericmedvet.jnb.datastructure.Grid;
import io.github.ericmedvet.mrsim2d.core.agents.gridvsr.AbstractGridVSR;
import io.github.ericmedvet.mrsim2d.core.agents.gridvsr.GridBody;
import java.util.function.Function;

@Discoverable(prefixTemplate = "sim|s.function|f")
public class Functions {

  private Functions() {
  }

  @SuppressWarnings("unused")
  public static <X> Function<X, Grid<GridBody.VoxelType>> vsrBody(
      @Param(value = "of", dNPM = "f.identity()") Function<X, AbstractGridVSR> beforeF,
      @Param(value = "nullify", dB = true) boolean nullifyNone,
      @Param(value = "format", dS = "%s") String format
  ) {
    Function<AbstractGridVSR, Grid<GridBody.VoxelType>> f = vsr -> vsr.getElementGrid()
        .map(GridBody.Element::type)
        .map(t -> nullifyNone ? (t.equals(GridBody.VoxelType.NONE) ? null : t) : t);
    return FormattedNamedFunction.from(f, format, "body").compose(beforeF);
  }
}
