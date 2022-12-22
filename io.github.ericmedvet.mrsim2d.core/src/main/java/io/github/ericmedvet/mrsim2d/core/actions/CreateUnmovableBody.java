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

package io.github.ericmedvet.mrsim2d.core.actions;

import io.github.ericmedvet.mrsim2d.core.Action;
import io.github.ericmedvet.mrsim2d.core.bodies.UnmovableBody;
import io.github.ericmedvet.mrsim2d.core.geometry.Poly;

/**
 * @author "Eric Medvet" on 2022/07/06 for 2dmrsim
 */
public record CreateUnmovableBody(
    Poly poly,
    double anchorsDensity
) implements Action<UnmovableBody> {
  public CreateUnmovableBody(Poly poly) {
    this(poly, Double.POSITIVE_INFINITY);
  }
}