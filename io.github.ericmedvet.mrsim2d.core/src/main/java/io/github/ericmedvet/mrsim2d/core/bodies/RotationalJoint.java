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

package io.github.ericmedvet.mrsim2d.core.bodies;

import io.github.ericmedvet.jnb.datastructure.DoubleRange;
import io.github.ericmedvet.mrsim2d.core.geometry.Point;

public interface RotationalJoint extends RigidBody, Anchorable {

  record Motor(
      double maxSpeed,
      double maxTorque,
      double controlP,
      double controlI,
      double controlD,
      double angleTolerance
  ) {

    public static final double MAX_SPEED = 20;
    public static final double MAX_TORQUE = 1000;
    public static final double CONTROL_P = 10;
    public static final double CONTROL_I = 2;
    public static final double CONTROL_D = 2;
    public static final double ANGLE_TOLERANCE = 0.000;

    public Motor() {
      this(MAX_SPEED, MAX_TORQUE, CONTROL_P, CONTROL_I, CONTROL_D, ANGLE_TOLERANCE);
    }
  }

  DoubleRange jointActiveAngleRange();

  double jointAngle();

  double jointLength();

  DoubleRange jointPassiveAngleRange();

  Point jointPoint();

  double jointTargetAngle();
}
