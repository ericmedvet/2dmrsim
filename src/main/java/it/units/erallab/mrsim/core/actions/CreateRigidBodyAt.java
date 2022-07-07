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
import it.units.erallab.mrsim.core.bodies.RigidBody;
import it.units.erallab.mrsim.core.geometry.Point;
import it.units.erallab.mrsim.core.geometry.Poly;

/**
 * @author "Eric Medvet" on 2022/07/06 for 2dmrsim
 */
public record CreateRigidBodyAt(
    Poly poly,
    double mass,
    Point translation,
    double rotation,
    double scale
) implements Action<RigidBody> {
  public CreateRigidBodyAt(Poly poly, double mass, Point translation) {
    this(poly, mass, translation, 0d, 1d);
  }
}