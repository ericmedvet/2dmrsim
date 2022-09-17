package it.units.erallab.mrsim2d.core.actions;

import it.units.erallab.mrsim2d.core.ActionPerformer;
import it.units.erallab.mrsim2d.core.Agent;
import it.units.erallab.mrsim2d.core.SelfDescribedAction;
import it.units.erallab.mrsim2d.core.bodies.RotationalJoint;
import it.units.erallab.mrsim2d.core.engine.ActionException;
import it.units.erallab.mrsim2d.core.geometry.Point;

public record CreateAndTranslateRotationalJoint(
    double length,
    double width,
    double mass,
    Point translation
) implements SelfDescribedAction<RotationalJoint> {
  @Override
  public RotationalJoint perform(ActionPerformer performer, Agent agent) throws ActionException {
    RotationalJoint rotationalJoint = performer.perform(
        new CreateRotationalJoint(length, width, mass),
        agent
    ).outcome().orElseThrow(() -> new ActionException(this, "Undoable creation"));
    performer.perform(new TranslateBody(rotationalJoint, translation), agent);
    return rotationalJoint;
  }
}
