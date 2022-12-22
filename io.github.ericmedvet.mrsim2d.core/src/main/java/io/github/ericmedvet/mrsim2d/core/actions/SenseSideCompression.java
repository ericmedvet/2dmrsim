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
import io.github.ericmedvet.mrsim2d.core.bodies.Voxel;
import io.github.ericmedvet.mrsim2d.core.engine.ActionException;
import io.github.ericmedvet.mrsim2d.core.util.DoubleRange;

public record SenseSideCompression(Voxel.Side side, Voxel body) implements Sense<Voxel>, SelfDescribedAction<Double> {
  private final static DoubleRange RANGE = new DoubleRange(0.5, 1.5);

  @Override
  public Double perform(ActionPerformer performer, Agent agent) throws ActionException {
    double avgL = Math.sqrt(body.areaRatio() * body.restArea());
    return RANGE.clip(body.vertex(side.vertexes()[0]).distance(body.vertex(side.vertexes()[1])) / avgL);
  }

  @Override
  public DoubleRange range() {
    return RANGE;
  }
}
