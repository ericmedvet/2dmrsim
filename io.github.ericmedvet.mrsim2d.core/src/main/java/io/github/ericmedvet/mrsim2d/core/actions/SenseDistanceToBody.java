/*-
 * ========================LICENSE_START=================================
 * mrsim2d-core
 * %%
 * Copyright (C) 2020 - 2025 Eric Medvet
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
import io.github.ericmedvet.mrsim2d.core.bodies.Body;
import java.util.function.DoubleUnaryOperator;

public record SenseDistanceToBody(double direction, double distanceRange, Body body) implements XMirrorableSense<Body> {

  @Override
  public DoubleRange range() {
    return new DoubleRange(0, distanceRange);
  }

  @Override
  public Sense<Body> mirrored() {
    return new SenseDistanceToBody(SenseAngle.mirrorAngle(direction), distanceRange, body);
  }

  @Override
  public DoubleUnaryOperator outcomeMirrorer() {
    return DoubleUnaryOperator.identity();
  }
}
