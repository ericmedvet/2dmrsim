
package io.github.ericmedvet.mrsim2d.core.bodies;

import io.github.ericmedvet.mrsim2d.core.geometry.Point;

import java.util.Collection;
public interface Anchor {

  record Link(Anchor source, Anchor destination, Type type) {
    public enum Type {RIGID, SOFT}

    public Link reversed() {
      return new Link(destination, source, type);
    }
  }

  Anchorable anchorable();

  Collection<Anchor.Link> links();

  Point point();

  default Collection<Anchorable> attachedAnchorables() {
    return links().stream().map(l -> l.destination.anchorable()).distinct().toList();
  }

  default Collection<Anchor> attachedAnchors() {
    return links().stream().map(l -> l.destination).toList();
  }

  default boolean isAnchoredTo(Anchor otherAnchor) {
    return links().stream().anyMatch(l -> l.destination().equals(otherAnchor));
  }

  default boolean isAnchoredTo(Anchorable otherAnchorable) {
    return links().stream().anyMatch(l -> l.destination().anchorable().equals(otherAnchorable));
  }

}
