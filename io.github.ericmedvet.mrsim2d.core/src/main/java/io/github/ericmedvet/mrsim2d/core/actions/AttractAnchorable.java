
package io.github.ericmedvet.mrsim2d.core.actions;

import io.github.ericmedvet.mrsim2d.core.ActionPerformer;
import io.github.ericmedvet.mrsim2d.core.Agent;
import io.github.ericmedvet.mrsim2d.core.SelfDescribedAction;
import io.github.ericmedvet.mrsim2d.core.bodies.Anchor;
import io.github.ericmedvet.mrsim2d.core.bodies.Anchorable;
import io.github.ericmedvet.mrsim2d.core.engine.ActionException;
import io.github.ericmedvet.mrsim2d.core.util.Pair;

import java.util.*;
public record AttractAnchorable(
    Collection<Anchor> anchors, Anchorable anchorable, double magnitude
) implements SelfDescribedAction<Collection<Pair<Anchor, Anchor>>> {
  @Override
  public Collection<Pair<Anchor, Anchor>> perform(ActionPerformer performer, Agent agent) throws ActionException {
    //discard already attached
    Collection<Anchor> srcAnchors = anchors.stream()
        .filter(a -> a.links().stream()
            .map(l -> l.destination().anchorable())
            .filter(dst -> dst == anchorable).toList().isEmpty())
        .toList();
    //match anchor pairs
    Collection<Anchor> dstAnchors = new LinkedHashSet<>(anchorable.anchors());
    Collection<Pair<Anchor, Anchor>> pairs = new ArrayList<>();
    srcAnchors.forEach(src -> {
      Optional<Anchor> closest = dstAnchors.stream()
          .min(Comparator.comparingDouble(a -> a.point().distance(src.point())));
      if (closest.isPresent()) {
        pairs.add(new Pair<>(src, closest.get()));
        dstAnchors.remove(closest.get());
      }
    });
    //attract
    pairs.forEach(p -> performer.perform(new AttractAnchor(p.first(), p.second(), magnitude)));
    return pairs;
  }
}
