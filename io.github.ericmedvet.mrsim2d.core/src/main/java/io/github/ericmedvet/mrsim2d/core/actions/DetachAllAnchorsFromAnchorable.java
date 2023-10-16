
package io.github.ericmedvet.mrsim2d.core.actions;

import io.github.ericmedvet.mrsim2d.core.ActionPerformer;
import io.github.ericmedvet.mrsim2d.core.Agent;
import io.github.ericmedvet.mrsim2d.core.SelfDescribedAction;
import io.github.ericmedvet.mrsim2d.core.bodies.Anchor;
import io.github.ericmedvet.mrsim2d.core.bodies.Anchorable;
import io.github.ericmedvet.mrsim2d.core.engine.ActionException;

import java.util.Collection;
import java.util.List;
public record DetachAllAnchorsFromAnchorable(
    Anchorable anchorable
) implements SelfDescribedAction<Collection<Anchor.Link>> {
  @Override
  public Collection<Anchor.Link> perform(ActionPerformer performer, Agent agent) throws ActionException {
    List<Anchorable> anchorables = anchorable.anchors().stream()
        .map(a -> a.links().stream().map(l -> l.destination().anchorable()).toList())
        .flatMap(Collection::stream)
        .distinct()
        .toList();
    return anchorables.stream()
        .map(target -> performer.perform(new DetachAnchorsFromAnchorable(anchorable, target), agent).outcome()
            .orElseThrow())
        .flatMap(Collection::stream)
        .toList();
  }
}
