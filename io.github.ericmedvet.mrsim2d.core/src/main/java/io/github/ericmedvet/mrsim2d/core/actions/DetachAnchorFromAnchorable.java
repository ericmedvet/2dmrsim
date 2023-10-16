
package io.github.ericmedvet.mrsim2d.core.actions;

import io.github.ericmedvet.mrsim2d.core.ActionPerformer;
import io.github.ericmedvet.mrsim2d.core.Agent;
import io.github.ericmedvet.mrsim2d.core.SelfDescribedAction;
import io.github.ericmedvet.mrsim2d.core.bodies.Anchor;
import io.github.ericmedvet.mrsim2d.core.bodies.Anchorable;
import io.github.ericmedvet.mrsim2d.core.engine.ActionException;

import java.util.Optional;
public record DetachAnchorFromAnchorable(
    Anchor anchor,
    Anchorable anchorable
) implements SelfDescribedAction<Anchor.Link> {
  @Override
  public Anchor.Link perform(ActionPerformer performer, Agent agent) throws ActionException {
    //find anchor
    Optional<Anchor.Link> optionalLink = anchor
        .links()
        .stream()
        .filter(l -> l.destination().anchorable() == anchorable)
        .findFirst();
    return optionalLink
        .flatMap(l -> performer.perform(new RemoveLink(optionalLink.get()), agent).outcome())
        .orElse(null);
  }
}
