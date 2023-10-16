
package io.github.ericmedvet.mrsim2d.core.actions;

import io.github.ericmedvet.mrsim2d.core.ActionPerformer;
import io.github.ericmedvet.mrsim2d.core.Agent;
import io.github.ericmedvet.mrsim2d.core.SelfDescribedAction;
import io.github.ericmedvet.mrsim2d.core.bodies.Anchor;
import io.github.ericmedvet.mrsim2d.core.bodies.Anchorable;
import io.github.ericmedvet.mrsim2d.core.engine.ActionException;

import java.util.Collection;
import java.util.Comparator;
public record AttachAnchor(
    Anchor anchor,
    Anchorable anchorable,
    Anchor.Link.Type type
) implements SelfDescribedAction<Anchor.Link> {

  @Override
  public Anchor.Link perform(ActionPerformer performer, Agent agent) throws ActionException {
    // find already attached anchors
    Collection<Anchor> attachedAnchors = anchor.links().stream()
        .map(Anchor.Link::destination)
        .filter(a -> a.anchorable() == anchorable)
        .distinct()
        .toList();
    //find closest anchor on destination
    Anchor destination = anchorable.anchors().stream()
        .filter(a -> !attachedAnchors.contains(a))
        .min(Comparator.comparingDouble(a -> a.point().distance(anchor.point())))
        .orElse(null);
    //create link
    if (destination != null) {
      return performer.perform(new CreateLink(anchor, destination, type), agent).outcome().orElse(null);
    }
    return null;
  }
}
