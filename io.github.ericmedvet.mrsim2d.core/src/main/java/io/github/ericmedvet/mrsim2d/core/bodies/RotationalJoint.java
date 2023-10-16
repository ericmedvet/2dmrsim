
package io.github.ericmedvet.mrsim2d.core.bodies;

import io.github.ericmedvet.jsdynsym.core.DoubleRange;
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
    public final static double MAX_SPEED = 20;
    public final static double MAX_TORQUE = 1000;
    public final static double CONTROL_P = 10;
    public final static double CONTROL_I = 2;
    public final static double CONTROL_D = 2;
    public final static double ANGLE_TOLERANCE = 0.000;

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
