
package io.github.ericmedvet.mrsim2d.core;

import io.github.ericmedvet.mrsim2d.core.actions.Sense;
import io.github.ericmedvet.mrsim2d.core.bodies.Body;

import java.util.function.Function;

public interface Sensor<B extends Body> extends Function<B, Sense<B>> {
}
