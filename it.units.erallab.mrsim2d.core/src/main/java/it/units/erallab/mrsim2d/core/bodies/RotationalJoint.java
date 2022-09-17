package it.units.erallab.mrsim2d.core.bodies;

import it.units.erallab.mrsim2d.core.geometry.Point;
import it.units.erallab.mrsim2d.core.util.DoubleRange;

public interface RotationalJoint extends RigidBody, Anchorable {
  double jointAngle();
  double jointTargetAngle();

  DoubleRange jointAngleRange();

  double jointLength();

  Point jointPoint();
}
