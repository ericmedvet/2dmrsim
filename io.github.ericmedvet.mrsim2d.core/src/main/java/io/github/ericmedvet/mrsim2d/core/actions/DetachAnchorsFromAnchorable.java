
package io.github.ericmedvet.mrsim2d.core.actions;

import io.github.ericmedvet.mrsim2d.core.ActionPerformer;
import io.github.ericmedvet.mrsim2d.core.Agent;
import io.github.ericmedvet.mrsim2d.core.SelfDescribedAction;
import io.github.ericmedvet.mrsim2d.core.bodies.Anchor;
import io.github.ericmedvet.mrsim2d.core.bodies.Anchorable;
import io.github.ericmedvet.mrsim2d.core.engine.ActionException;

import java.util.Collection;
import java.util.Optional;
public record DetachAnchorsFromAnchorable(
    Anchorable sourceAnchorable, Anchorable targetAnchorable
) implements SelfDescribedAction<Collection<Anchor.Link>> {

  @Override
  public Collection<Anchor.Link> perform(ActionPerformer performer, Agent agent) throws ActionException {
    return sourceAnchorable.anchors().stream()
        .map(a -> performer.perform(new DetachAnchorFromAnchorable(a, targetAnchorable), agent).outcome())
        .filter(Optional::isPresent)
        .map(Optional::get)
        .toList();
  }
}
