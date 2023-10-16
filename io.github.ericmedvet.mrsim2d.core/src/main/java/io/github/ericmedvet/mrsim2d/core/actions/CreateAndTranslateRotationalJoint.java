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
package io.github.ericmedvet.mrsim2d.core.actions;

import io.github.ericmedvet.jsdynsym.core.DoubleRange;
import io.github.ericmedvet.mrsim2d.core.ActionPerformer;
import io.github.ericmedvet.mrsim2d.core.Agent;
import io.github.ericmedvet.mrsim2d.core.SelfDescribedAction;
import io.github.ericmedvet.mrsim2d.core.bodies.RotationalJoint;
import io.github.ericmedvet.mrsim2d.core.engine.ActionException;
import io.github.ericmedvet.mrsim2d.core.geometry.Point;

public record CreateAndTranslateRotationalJoint(
    double length,
    double width,
    double mass,
    RotationalJoint.Motor motor,
    DoubleRange activeAngleRange,
    Point translation)
    implements SelfDescribedAction<RotationalJoint> {
  @Override
  public RotationalJoint perform(ActionPerformer performer, Agent agent) throws ActionException {
    RotationalJoint rotationalJoint =
        performer
            .perform(new CreateRotationalJoint(length, width, mass, motor, activeAngleRange), agent)
            .outcome()
            .orElseThrow(() -> new ActionException(this, "Undoable creation"));
    performer.perform(new TranslateBody(rotationalJoint, translation), agent);
    return rotationalJoint;
  }
}
