package io.github.ericmedvet.mrsim2d.core;

import io.github.ericmedvet.mrsim2d.core.geometry.Point;
public record NFCMessage(
    Point source,
    double direction,
    short channel,
    double value
) {
}
