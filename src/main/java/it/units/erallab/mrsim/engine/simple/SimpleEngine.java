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

package it.units.erallab.mrsim.engine.simple;

import it.units.erallab.mrsim.core.Action;
import it.units.erallab.mrsim.core.Agent;
import it.units.erallab.mrsim.core.action.CreateRigidBodyAt;
import it.units.erallab.mrsim.core.body.Body;
import it.units.erallab.mrsim.engine.AbstractEngine;
import it.units.erallab.mrsim.engine.UnsupportedActionException;

import java.util.Collection;

/**
 * @author "Eric Medvet" on 2022/07/06 for 2dmrsim
 */
public class SimpleEngine extends AbstractEngine {

  @Override
  protected <O> O innerPerform(Action<O> action, Agent agent) throws UnsupportedActionException {
    if (action instanceof CreateRigidBodyAt a) {
      // TODO do something
    }
    return super.innerPerform(action, agent);
  }

  @Override
  protected double innerTick() {
    return 0;
  }

  @Override
  protected Collection<Body<?>> getBodies() {
    return null;
  }
}
