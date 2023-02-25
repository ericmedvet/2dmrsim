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

import io.github.ericmedvet.mrsim2d.core.ActionPerformer;
import io.github.ericmedvet.mrsim2d.core.Agent;
import io.github.ericmedvet.mrsim2d.core.SelfDescribedAction;
import io.github.ericmedvet.mrsim2d.core.bodies.Body;
import io.github.ericmedvet.mrsim2d.core.engine.ActionException;
import io.github.ericmedvet.mrsim2d.core.util.DoubleRange;

public record SenseAngle(Body body) implements Sense<Body>, SelfDescribedAction<Double> {

  private final static DoubleRange RANGE = new DoubleRange(-Math.PI, Math.PI);

  @Override
  public Double perform(ActionPerformer performer, Agent agent) throws ActionException {
    double a = body.angle();
    if (a > Math.PI) {
      a = a - 2d * Math.PI;
    }
    if (a < -Math.PI) {
      a = a + 2d * Math.PI;
    }
    return a;
  }

  @Override
  public DoubleRange range() {
    return RANGE;
  }
}
