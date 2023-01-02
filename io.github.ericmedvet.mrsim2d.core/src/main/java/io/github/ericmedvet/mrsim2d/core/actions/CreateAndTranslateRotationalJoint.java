package io.github.ericmedvet.mrsim2d.core.actions;

import io.github.ericmedvet.mrsim2d.core.ActionPerformer;
import io.github.ericmedvet.mrsim2d.core.Agent;
import io.github.ericmedvet.mrsim2d.core.SelfDescribedAction;
import io.github.ericmedvet.mrsim2d.core.bodies.RotationalJoint;
import io.github.ericmedvet.mrsim2d.core.engine.ActionException;
import io.github.ericmedvet.mrsim2d.core.geometry.Point;
import io.github.ericmedvet.mrsim2d.core.util.DoubleRange;

public record CreateAndTranslateRotationalJoint(
    double length,
    double width,
    double mass,
    RotationalJoint.Motor motor,
    DoubleRange activeAngleRange,
    Point translation
) implements SelfDescribedAction<RotationalJoint> {
  @Override
  public RotationalJoint perform(ActionPerformer performer, Agent agent) throws ActionException {
    RotationalJoint rotationalJoint = performer.perform(
        new CreateRotationalJoint(length, width, mass, motor, activeAngleRange),
        agent
    ).outcome().orElseThrow(() -> new ActionException(this, "Undoable creation"));
    performer.perform(new TranslateBody(rotationalJoint, translation), agent);
    return rotationalJoint;
  }
}
