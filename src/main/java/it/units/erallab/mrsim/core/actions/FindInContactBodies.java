package it.units.erallab.mrsim.core.actions;

import it.units.erallab.mrsim.core.Action;
import it.units.erallab.mrsim.core.bodies.Body;

import java.util.Collection;

public record FindInContactBodies(Body body) implements Action<Collection<Body>> {
}
