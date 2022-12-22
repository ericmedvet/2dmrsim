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

package it.units.erallab.mrsim2d.core.actions;

import it.units.erallab.mrsim2d.core.ActionPerformer;
import it.units.erallab.mrsim2d.core.Agent;
import it.units.erallab.mrsim2d.core.SelfDescribedAction;
import it.units.erallab.mrsim2d.core.bodies.Body;
import it.units.erallab.mrsim2d.core.engine.ActionException;
import it.units.erallab.mrsim2d.core.util.DoubleRange;

import java.util.Collection;
import java.util.List;

public record SenseContact(Body body) implements Sense<Body>, SelfDescribedAction<Double> {

  @Override
  public Double perform(ActionPerformer performer, Agent agent) throws ActionException {
    Collection<Body> bodies = performer.perform(new FindInContactBodies(body), agent).outcome().orElse(List.of());
    return bodies.isEmpty() ? 0d : 1d;
  }

  @Override
  public DoubleRange range() {
    return DoubleRange.UNIT;
  }
}
