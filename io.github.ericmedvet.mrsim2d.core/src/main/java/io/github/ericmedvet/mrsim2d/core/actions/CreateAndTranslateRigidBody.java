
package io.github.ericmedvet.mrsim2d.core.actions;

import io.github.ericmedvet.mrsim2d.core.ActionPerformer;
import io.github.ericmedvet.mrsim2d.core.Agent;
import io.github.ericmedvet.mrsim2d.core.SelfDescribedAction;
import io.github.ericmedvet.mrsim2d.core.bodies.RigidBody;
import io.github.ericmedvet.mrsim2d.core.engine.ActionException;
import io.github.ericmedvet.mrsim2d.core.geometry.Point;
import io.github.ericmedvet.mrsim2d.core.geometry.Poly;
public record CreateAndTranslateRigidBody(
    Poly poly,
    double mass,
    double anchorsDensity,
    Point translation
) implements SelfDescribedAction<RigidBody> {

  @Override
  public RigidBody perform(ActionPerformer performer, Agent agent) throws ActionException {
    RigidBody rigidBody = performer.perform(
        new CreateRigidBody(poly, mass, anchorsDensity),
        agent
    ).outcome().orElseThrow(() -> new ActionException(this, "Undoable creation"));
    performer.perform(new TranslateBody(rigidBody, translation), agent);
    return rigidBody;
  }
}
