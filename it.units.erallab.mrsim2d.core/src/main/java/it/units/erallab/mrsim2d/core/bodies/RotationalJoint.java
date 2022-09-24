/*
 * Copyright 2022 eric
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

package it.units.erallab.mrsim2d.core.bodies;

import it.units.erallab.mrsim2d.core.geometry.Point;
import it.units.erallab.mrsim2d.core.util.DoubleRange;

public interface RotationalJoint extends RigidBody, Anchorable {

  record Motor(
      double maxSpeed,
      double maxTorque,
      double controlP,
      double controlI,
      double controlD,
      double angleTolerance
  ) {
    private final static double MAX_SPEED = 100;
    private final static double MAX_TORQUE = 1000;
    private final static double CONTROL_P = 20;
    private final static double CONTROL_I = 1;
    private final static double CONTROL_D = 1;
    private final static double ANGLE_TOLERANCE = 0.01;

    public Motor() {
      this(MAX_SPEED, MAX_TORQUE, CONTROL_P, CONTROL_I, CONTROL_D, ANGLE_TOLERANCE);
    }
  }

  double jointAngle();

  DoubleRange jointAngleRange();

  double jointLength();

  Point jointPoint();

  double jointTargetAngle();
}
