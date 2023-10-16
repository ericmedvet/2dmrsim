
package io.github.ericmedvet.mrsim2d.core.actions;

import io.github.ericmedvet.mrsim2d.core.ActionPerformer;
import io.github.ericmedvet.mrsim2d.core.Agent;
import io.github.ericmedvet.mrsim2d.core.EmbodiedAgent;
import io.github.ericmedvet.mrsim2d.core.SelfDescribedAction;
import io.github.ericmedvet.mrsim2d.core.engine.ActionException;
import io.github.ericmedvet.mrsim2d.core.geometry.BoundingBox;
import io.github.ericmedvet.mrsim2d.core.geometry.Point;
public record TranslateAgentAt(
    EmbodiedAgent agent,
    BoundingBox.Anchor anchor,
    Point destination
) implements SelfDescribedAction<EmbodiedAgent> {
  @Override
  public EmbodiedAgent perform(ActionPerformer performer, Agent agent) throws ActionException {
    Point anchorPoint = agent().boundingBox().anchor(anchor);
    agent().bodyParts().forEach(b -> performer.perform(new TranslateBody(b, destination.diff(anchorPoint)), agent));
    return agent();
  }
}
