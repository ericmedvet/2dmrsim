package it.units.erallab.mrsim2d.core.bodies;

import it.units.erallab.mrsim2d.core.geometry.Point;
import it.units.erallab.mrsim2d.core.util.DoubleRange;

public interface RotationalJoint extends RigidBody, Anchorable {

  record Motor(
      double speed,
      double maxTorque
  ) {
    private final static double SPEED = 0.5;
    private final static double MAX_TORQUE = 10;

    public Motor() {
      this(SPEED, MAX_TORQUE);
    }
  }

  double jointAngle();

  DoubleRange jointAngleRange();

  double jointLength();

  Point jointPoint();

  double jointTargetAngle();
}
