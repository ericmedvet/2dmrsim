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

package io.github.ericmedvet.mrsim2d.core.bodies;

import io.github.ericmedvet.mrsim2d.core.geometry.Point;

public interface UnmovableBody extends RigidBody {
  @Override
  default double angle() {
    return 0;
  }

  @Override
  default Point centerLinearVelocity() {
    return Point.ORIGIN;
  }

  @Override
  default double mass() {
    return Double.POSITIVE_INFINITY;
  }
}
