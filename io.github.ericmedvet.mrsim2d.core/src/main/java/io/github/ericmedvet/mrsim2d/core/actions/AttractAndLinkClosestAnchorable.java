
package io.github.ericmedvet.mrsim2d.core.actions;

import io.github.ericmedvet.mrsim2d.core.Action;
import io.github.ericmedvet.mrsim2d.core.bodies.Anchor;
import io.github.ericmedvet.mrsim2d.core.util.Pair;

import java.util.Collection;
import java.util.Map;
public record AttractAndLinkClosestAnchorable(
    Collection<Anchor> anchors, double magnitude, Anchor.Link.Type type
) implements Action<Map<Pair<Anchor, Anchor>, AttractAndLinkAnchor.Outcome>> {
}
