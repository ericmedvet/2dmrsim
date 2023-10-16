
package io.github.ericmedvet.mrsim2d.core.bodies;

import java.util.Collection;
import java.util.List;
public interface Anchorable extends Body {
  List<Anchor> anchors();

  default Collection<Anchorable> attachedAnchorables() {
    return anchors().stream().map(Anchor::attachedAnchorables).flatMap(Collection::stream).distinct().toList();
  }

  default Collection<Anchor> attachedAnchors() {
    return anchors().stream().map(Anchor::attachedAnchors).flatMap(Collection::stream).distinct().toList();
  }

  default Collection<Anchor> attachedTo(Anchor otherAnchor) {
    return anchors().stream().filter(a -> a.isAnchoredTo(otherAnchor)).toList();
  }


  default Collection<Anchor> attachedTo(Anchorable otherAnchorable) {
    return anchors().stream().filter(a -> a.isAnchoredTo(otherAnchorable)).toList();
  }

  default boolean isAnchoredTo(Anchor otherAnchor) {
    return anchors().stream().anyMatch(a -> a.isAnchoredTo(otherAnchor));
  }

  default boolean isAnchoredTo(Anchorable otherAnchorable) {
    return anchors().stream().anyMatch(a -> a.isAnchoredTo(otherAnchorable));
  }
}
