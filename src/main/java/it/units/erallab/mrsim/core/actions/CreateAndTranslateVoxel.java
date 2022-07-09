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
import it.units.erallab.mrsim.core.geometry.Point;
import it.units.erallab.mrsim.util.DoubleRange;

/**
 * @author "Eric Medvet" on 2022/07/07 for 2dmrsim
 */
public record CreateAndTranslateVoxel(
    double sideLength,
    double mass,
    Voxel.Material material,
    Point translation
) implements Action<Voxel> {
  public CreateAndTranslateVoxel(
      double sideLength,
      double mass,
      Point translation
  ) {
    this(sideLength, mass, new Voxel.Material(), translation);
  }
}
