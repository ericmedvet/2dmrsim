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

package it.units.erallab.mrsim.core.actions;

import it.units.erallab.mrsim.core.Action;
import it.units.erallab.mrsim.core.bodies.Voxel;

import java.util.EnumMap;
import java.util.Map;

/**
 * @author "Eric Medvet" on 2022/07/09 for 2dmrsim
 */
public record ActuateVoxel(Voxel voxel, EnumMap<Voxel.Side, Double> values) implements Action<Voxel> {
  public ActuateVoxel(Voxel voxel, double value) {
    this(voxel, value, value, value, value);
  }

  public ActuateVoxel(Voxel voxel, double nValue, double eValue, double sValue, double wValue) {
    this(voxel, new EnumMap<>(Map.of(
        Voxel.Side.N, nValue,
        Voxel.Side.E, eValue,
        Voxel.Side.S, sValue,
        Voxel.Side.W, wValue
    )));
  }
}
