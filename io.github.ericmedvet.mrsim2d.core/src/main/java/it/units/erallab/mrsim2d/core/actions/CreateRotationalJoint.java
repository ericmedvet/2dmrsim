package it.units.erallab.mrsim2d.core.actions;

import it.units.erallab.mrsim2d.core.Action;
import it.units.erallab.mrsim2d.core.bodies.RotationalJoint;

public record CreateRotationalJoint(
    double length,
    double width,
    double mass,
    RotationalJoint.Motor motor
) implements Action<RotationalJoint> {
}
