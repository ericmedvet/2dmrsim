
package io.github.ericmedvet.mrsim2d.core.agents.legged;

import io.github.ericmedvet.jsdynsym.core.DoubleRange;
import io.github.ericmedvet.mrsim2d.core.Sensor;
import io.github.ericmedvet.mrsim2d.core.bodies.RotationalJoint;

import java.util.List;

public record LegChunk(
    double length,
    double width,
    double mass,
    RotationalJoint.Motor motor,
    DoubleRange activeAngleRange,
    ConnectorType upConnector,
    List<Sensor<?>> jointSensors
) {}
