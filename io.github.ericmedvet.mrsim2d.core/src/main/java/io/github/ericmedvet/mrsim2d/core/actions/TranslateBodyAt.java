
package io.github.ericmedvet.mrsim2d.core.actions;

import io.github.ericmedvet.mrsim2d.core.ActionPerformer;
import io.github.ericmedvet.mrsim2d.core.Agent;
import io.github.ericmedvet.mrsim2d.core.SelfDescribedAction;
import io.github.ericmedvet.mrsim2d.core.bodies.Body;
import io.github.ericmedvet.mrsim2d.core.engine.ActionException;
import io.github.ericmedvet.mrsim2d.core.geometry.BoundingBox;
import io.github.ericmedvet.mrsim2d.core.geometry.Point;
public record TranslateBodyAt(
    Body body,
    BoundingBox.Anchor anchor,
    Point destination
) implements SelfDescribedAction<Body> {
  @Override
  public Body perform(ActionPerformer performer, Agent agent) throws ActionException {
    Point anchorPoint = body.poly().boundingBox().anchor(anchor);
    return performer.perform(new TranslateBody(body, destination.diff(anchorPoint)), agent).outcome().orElseThrow(
        () -> new ActionException(this, "Cannot translate body")
    );
  }
}
