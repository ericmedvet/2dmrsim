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
    private final static double MAX_TORQUE = 10;
    private final static double CONTROL_P = 20;
    private final static double CONTROL_I = 10;
    private final static double CONTROL_D = 1;
    private final static double ANGLE_TOLERANCE = Math.toRadians(2d);

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
