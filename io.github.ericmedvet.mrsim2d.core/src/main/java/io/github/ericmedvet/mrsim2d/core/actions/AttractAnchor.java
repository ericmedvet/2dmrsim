
package io.github.ericmedvet.mrsim2d.core.actions;

import io.github.ericmedvet.mrsim2d.core.Action;
import io.github.ericmedvet.mrsim2d.core.bodies.Anchor;
public record AttractAnchor(Anchor source, Anchor destination, double magnitude) implements Action<Double> {
}
