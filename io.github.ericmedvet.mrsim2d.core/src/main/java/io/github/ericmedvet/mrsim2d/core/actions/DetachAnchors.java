
package io.github.ericmedvet.mrsim2d.core.actions;

import io.github.ericmedvet.mrsim2d.core.ActionPerformer;
import io.github.ericmedvet.mrsim2d.core.Agent;
import io.github.ericmedvet.mrsim2d.core.SelfDescribedAction;
import io.github.ericmedvet.mrsim2d.core.bodies.Anchor;
import io.github.ericmedvet.mrsim2d.core.engine.ActionException;

import java.util.Collection;
import java.util.Optional;
public record DetachAnchors(Collection<Anchor> anchors) implements SelfDescribedAction<Collection<Anchor.Link>> {
  @Override
  public Collection<Anchor.Link> perform(ActionPerformer performer, Agent agent) throws ActionException {
    Collection<Anchor.Link> toRemoveLinks = anchors.stream()
        .map(Anchor::links)
        .flatMap(Collection::stream)
        .toList();
    return toRemoveLinks.stream()
        .map(l -> performer.perform(new RemoveLink(l), agent).outcome())
        .filter(Optional::isPresent)
        .map(Optional::get)
        .toList();
  }
}
