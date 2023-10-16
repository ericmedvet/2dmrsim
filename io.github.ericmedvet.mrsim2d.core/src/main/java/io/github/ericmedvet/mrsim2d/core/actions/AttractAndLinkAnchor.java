
package io.github.ericmedvet.mrsim2d.core.actions;

import io.github.ericmedvet.mrsim2d.core.Action;
import io.github.ericmedvet.mrsim2d.core.bodies.Anchor;

import java.util.Optional;
public record AttractAndLinkAnchor(Anchor source, Anchor destination, double magnitude, Anchor.Link.Type type) implements Action<AttractAndLinkAnchor.Outcome> {

  public record Outcome(Optional<Double> magnitude, Optional<Anchor.Link> link) {}
}
