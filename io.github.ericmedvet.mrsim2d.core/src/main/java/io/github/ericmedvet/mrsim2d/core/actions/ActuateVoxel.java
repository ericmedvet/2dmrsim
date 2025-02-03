/*-
 * ========================LICENSE_START=================================
 * mrsim2d-core
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

package io.github.ericmedvet.mrsim2d.core.actions;

import io.github.ericmedvet.jnb.datastructure.DoubleRange;
import io.github.ericmedvet.mrsim2d.core.bodies.Voxel;
import java.util.EnumMap;
import java.util.Map;
import java.util.stream.Collectors;

public record ActuateVoxel(Voxel body, EnumMap<Voxel.Side, Double> values) implements Actuate<Voxel, Voxel> {
  public ActuateVoxel(Voxel voxel, double value) {
    this(voxel, value, value, value, value);
  }

  public ActuateVoxel(Voxel voxel, double nValue, double eValue, double sValue, double wValue) {
    this(
        voxel,
        new EnumMap<>(
            Map.of(
                Voxel.Side.N,
                nValue,
                Voxel.Side.E,
                eValue,
                Voxel.Side.S,
                sValue,
                Voxel.Side.W,
                wValue
            )
        )
    );
  }

  @Override
  public DoubleRange range() {
    return DoubleRange.SYMMETRIC_UNIT;
  }

  @Override
  public String toString() {
    return "ActuateVoxel[" + "body="
        + body + ", values={"
        + values.entrySet().stream()
            .map(e -> "%s=%.3f".formatted(e.getKey(), e.getValue()))
            .collect(Collectors.joining(";"))
        + "}]";
  }
}
