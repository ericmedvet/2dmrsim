
package io.github.ericmedvet.mrsim2d.core.actions;

import io.github.ericmedvet.mrsim2d.core.Action;
import io.github.ericmedvet.mrsim2d.core.bodies.Body;

import java.util.Collection;

public record FindInContactBodies(Body body) implements Action<Collection<Body>> {
}
