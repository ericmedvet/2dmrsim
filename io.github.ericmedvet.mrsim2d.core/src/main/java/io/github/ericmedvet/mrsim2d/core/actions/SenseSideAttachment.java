
package io.github.ericmedvet.mrsim2d.core.actions;

import io.github.ericmedvet.jsdynsym.core.DoubleRange;
import io.github.ericmedvet.mrsim2d.core.ActionPerformer;
import io.github.ericmedvet.mrsim2d.core.Agent;
import io.github.ericmedvet.mrsim2d.core.SelfDescribedAction;
import io.github.ericmedvet.mrsim2d.core.bodies.Anchor;
import io.github.ericmedvet.mrsim2d.core.bodies.Anchorable;
import io.github.ericmedvet.mrsim2d.core.bodies.Voxel;
import io.github.ericmedvet.mrsim2d.core.engine.ActionException;

import java.util.Collection;
import java.util.List;

public record SenseSideAttachment(Voxel.Side side, Voxel body) implements Sense<Voxel>, SelfDescribedAction<Double> {

  @Override
  public Double perform(ActionPerformer performer, Agent agent) throws ActionException {
    //consider side anchors
    Collection<Anchor> anchors = body.anchorsOn(side);
    if (anchors.isEmpty()) {
      return 0d;
    }
    //find "most attached" other body
    List<Anchorable> anchorables = anchors.stream()
        .map(Anchor::attachedAnchorables)
        .flatMap(Collection::stream)
        .toList();
    if (anchorables.isEmpty()) {
      return 0d;
    }
    long maxAttachedAnchorsOfSameBody = anchorables.stream()
        .mapToLong(b -> anchors.stream().filter(a -> a.isAnchoredTo(b)).count())
        .max()
        .orElse(0);
    //return
    return (double) maxAttachedAnchorsOfSameBody / (double) anchors.size();
  }

  @Override
  public DoubleRange range() {
    return DoubleRange.UNIT;
  }
}
