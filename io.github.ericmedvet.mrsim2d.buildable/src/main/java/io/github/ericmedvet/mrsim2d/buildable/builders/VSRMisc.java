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
import io.github.ericmedvet.jnb.datastructure.Grid;
import io.github.ericmedvet.mrsim2d.core.Sensor;
import io.github.ericmedvet.mrsim2d.core.agents.gridvsr.GridBody;
import io.github.ericmedvet.mrsim2d.core.agents.gridvsr.GridBody.VoxelType;
import io.github.ericmedvet.mrsim2d.core.bodies.Body;
import java.util.List;
import java.util.function.Function;

@Discoverable(prefixTemplate = "sim|s.agent|a.vsr")
public class VSRMisc {
  private VSRMisc() {}

  @SuppressWarnings("unused")
  public static GridBody gridBody(
      @Param("shape") Grid<VoxelType> shape,
      @Param("sensorizingFunction")
          Function<Grid<Boolean>, Grid<List<Sensor<? super Body>>>> sensorizingFunction) {
    return new GridBody(shape, sensorizingFunction);
  }
}
