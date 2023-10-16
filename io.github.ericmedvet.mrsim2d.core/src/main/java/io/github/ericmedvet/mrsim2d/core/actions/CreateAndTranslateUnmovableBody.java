
package io.github.ericmedvet.mrsim2d.core.actions;

import io.github.ericmedvet.mrsim2d.core.ActionPerformer;
import io.github.ericmedvet.mrsim2d.core.Agent;
import io.github.ericmedvet.mrsim2d.core.SelfDescribedAction;
import io.github.ericmedvet.mrsim2d.core.bodies.UnmovableBody;
import io.github.ericmedvet.mrsim2d.core.engine.ActionException;
import io.github.ericmedvet.mrsim2d.core.geometry.Point;
import io.github.ericmedvet.mrsim2d.core.geometry.Poly;
public record CreateAndTranslateUnmovableBody(
    Poly poly,
    double anchorsDensity,
    Point translation
) implements SelfDescribedAction<UnmovableBody> {
  @Override
  public UnmovableBody perform(ActionPerformer performer, Agent agent) throws ActionException {
    UnmovableBody unmovableBody = performer.perform(
        new CreateUnmovableBody(poly, anchorsDensity),
        agent
    ).outcome().orElseThrow(() -> new ActionException(this, "Undoable creation"));
    performer.perform(new TranslateBody(unmovableBody, translation), agent);
    return unmovableBody;
  }
}
