/*-
 * ========================LICENSE_START=================================
 * mrsim2d-buildable
 * %%
 * Copyright (C) 2020 - 2024 Eric Medvet
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * =========================LICENSE_END==================================
 */

package io.github.ericmedvet.mrsim2d.buildable.builders;

import io.github.ericmedvet.jnb.core.Discoverable;
import io.github.ericmedvet.jnb.core.Param;
import io.github.ericmedvet.jnb.datastructure.DoubleRange;
import io.github.ericmedvet.mrsim2d.core.Sensor;
import io.github.ericmedvet.mrsim2d.core.agents.legged.AbstractLeggedHybridModularRobot;
import io.github.ericmedvet.mrsim2d.core.agents.legged.AbstractLeggedHybridRobot;
import io.github.ericmedvet.mrsim2d.core.agents.legged.ConnectorType;
import io.github.ericmedvet.mrsim2d.core.agents.legged.LegChunk;
import io.github.ericmedvet.mrsim2d.core.bodies.RotationalJoint;
import java.util.List;

@Discoverable(prefixTemplate = "sim|s.agent|a.legged|l")
public class LeggedMisc {
    protected static final double RIGID_DENSITY = 1d;
    protected static final double LEG_CHUNK_LENGTH = 1.05d;
    protected static final double LEG_CHUNK_WIDTH = 1d;
    protected static final double LEG_CHUNK_MASS = LEG_CHUNK_LENGTH * LEG_CHUNK_WIDTH * RIGID_DENSITY;
    protected static final double TRUNK_LENGTH = 6d;
    protected static final double TRUNK_WIDTH = 1d;
    protected static final double TRUNK_MASS = TRUNK_LENGTH * TRUNK_WIDTH * RIGID_DENSITY;

    private LeggedMisc() {}

    @SuppressWarnings("unused")
    public static AbstractLeggedHybridRobot.Leg leg(
            @Param("legChunks") List<LegChunk> legChunks,
            @Param(value = "downConnectorMass", dD = LEG_CHUNK_LENGTH * LEG_CHUNK_LENGTH * RIGID_DENSITY)
                    double downConnectorMass,
            @Param(value = "downConnector", dS = "rigid") ConnectorType downConnector,
            @Param("downConnectorSensors") List<Sensor<?>> downConnectorSensors) {
        return new AbstractLeggedHybridRobot.Leg(legChunks, downConnector, downConnectorMass, downConnectorSensors);
    }

    @SuppressWarnings("unused")
    public static LegChunk legChunk(
            @Param(value = "length", dD = LEG_CHUNK_LENGTH) double length,
            @Param(value = "width", dD = LEG_CHUNK_WIDTH) double width,
            @Param(value = "mass", dD = LEG_CHUNK_MASS) double mass,
            @Param(value = "upConnector", dS = "rigid") ConnectorType upConnector,
            @Param("jointSensors") List<Sensor<?>> jointSensors,
            @Param(value = "motorMaxSpeed", dD = RotationalJoint.Motor.MAX_SPEED) double motorMaxSpeed,
            @Param(value = "motorMaxTorque", dD = RotationalJoint.Motor.MAX_TORQUE) double motorMaxTorque,
            @Param(value = "motorControlP", dD = RotationalJoint.Motor.CONTROL_P) double motorControlP,
            @Param(value = "motorControlI", dD = RotationalJoint.Motor.CONTROL_I) double motorControlI,
            @Param(value = "motorControlD", dD = RotationalJoint.Motor.CONTROL_D) double motorControlD,
            @Param(value = "motorAngleTolerance", dD = RotationalJoint.Motor.ANGLE_TOLERANCE)
                    double motorAngleTolerance,
            @Param(value = "activeAngleRange", dNPM = "m.range(min=-1.047;max=1.047)") DoubleRange activeAngleRange) {
        return new LegChunk(
                length,
                width,
                mass,
                new RotationalJoint.Motor(
                        motorMaxSpeed,
                        motorMaxTorque,
                        motorControlP,
                        motorControlI,
                        motorControlD,
                        motorAngleTolerance),
                activeAngleRange,
                upConnector,
                jointSensors);
    }

    @SuppressWarnings("unused")
    public static AbstractLeggedHybridModularRobot.Module module(
            @Param(value = "trunkLength", dD = TRUNK_LENGTH) double trunkLength,
            @Param(value = "trunkWidth", dD = TRUNK_WIDTH) double trunkWidth,
            @Param(value = "trunkMass", dD = TRUNK_MASS) double trunkMass,
            @Param("legChunks") List<LegChunk> legChunks,
            @Param(value = "downConnector", dS = "rigid") ConnectorType downConnector,
            @Param(value = "rightConnector", dS = "rigid") ConnectorType rightConnector,
            @Param("trunkSensors") List<Sensor<?>> trunkSensors,
            @Param("rightConnectorSensors") List<Sensor<?>> rightConnectorSensors,
            @Param("downConnectorSensors") List<Sensor<?>> downConnectorSensors) {
        return new AbstractLeggedHybridModularRobot.Module(
                trunkLength,
                trunkWidth,
                trunkMass,
                legChunks,
                downConnector,
                rightConnector,
                trunkSensors,
                rightConnectorSensors,
                downConnectorSensors);
    }
}
