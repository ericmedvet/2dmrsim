package io.github.ericmedvet.mrsim2d.core;

import io.github.ericmedvet.mrsim2d.core.geometry.Point;

/**
 * @author "Eric Medvet" on 2023/01/21 for 2dmrsim
 */
public record NFCMessage(
    Point source,
    double direction,
    short channel,
    double value
) {
}
