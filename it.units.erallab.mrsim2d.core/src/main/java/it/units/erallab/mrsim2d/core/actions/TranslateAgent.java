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
import it.units.erallab.mrsim2d.core.EmbodiedAgent;
import it.units.erallab.mrsim2d.core.SelfDescribedAction;
import it.units.erallab.mrsim2d.core.engine.ActionException;
import it.units.erallab.mrsim2d.core.geometry.Point;

/**
 * @author "Eric Medvet" on 2022/07/09 for 2dmrsim
 */
public record TranslateAgent(EmbodiedAgent agent, Point translation) implements SelfDescribedAction<EmbodiedAgent> {
  @Override
  public EmbodiedAgent perform(ActionPerformer performer, Agent agent) throws ActionException {
    agent().bodyParts().forEach(b -> performer.perform(new TranslateBody(b, translation), agent));
    return agent();
  }
}
