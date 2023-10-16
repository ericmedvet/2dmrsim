
package io.github.ericmedvet.mrsim2d.core.actions;

import io.github.ericmedvet.mrsim2d.core.ActionPerformer;
import io.github.ericmedvet.mrsim2d.core.Agent;
import io.github.ericmedvet.mrsim2d.core.SelfDescribedAction;
import io.github.ericmedvet.mrsim2d.core.bodies.Anchor;
import io.github.ericmedvet.mrsim2d.core.bodies.Anchorable;
import io.github.ericmedvet.mrsim2d.core.engine.ActionException;

import java.util.Collection;
import java.util.Comparator;
public record AttachClosestAnchors(
    int nOfAnchors,
    Anchorable sourceAnchorable,
    Anchorable targetAnchorable,
    Anchor.Link.Type type
) implements SelfDescribedAction<Collection<Anchor.Link>> {

  @Override
  public Collection<Anchor.Link> perform(ActionPerformer performer, Agent agent) throws ActionException {
    //check how many anchors are already attached
    Collection<Anchor> attached = sourceAnchorable().attachedTo(targetAnchorable());
    //possibly attach new anchors
    if (attached.size() < nOfAnchors) {
      record AnchorPair(Anchor src, Anchor dst) {}
      sourceAnchorable().anchors().stream()
          .filter(a -> !attached.contains(a)) // remove already attached anchors
          .map(a -> new AnchorPair(
              a,
              targetAnchorable().anchors().stream()
                  .min(Comparator.comparingDouble(oa -> oa.point().distance(a.point())))
                  .orElseThrow(() -> new IllegalArgumentException("Target anchorable has no anchors"))
          )) // find closest dst anchor
          .sorted(Comparator.comparingDouble(p -> p.src().point().distance(p.dst().point())))
          .limit(nOfAnchors - attached.size())
          .forEach(p -> performer.perform(
              new CreateLink(p.src(), p.dst(), type),
              agent
          ));
    }
    return attached.stream()
        .map(a -> a.links().stream()
            .filter(l -> l.destination().anchorable().equals(targetAnchorable()))
            .toList()
        )
        .flatMap(Collection::stream)
        .toList();
  }
}
