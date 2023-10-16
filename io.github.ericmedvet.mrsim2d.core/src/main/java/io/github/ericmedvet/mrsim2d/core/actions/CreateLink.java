
package io.github.ericmedvet.mrsim2d.core.actions;

import io.github.ericmedvet.mrsim2d.core.Action;
import io.github.ericmedvet.mrsim2d.core.bodies.Anchor;
public record CreateLink(Anchor source, Anchor destination, Anchor.Link.Type type) implements Action<Anchor.Link> {
}
