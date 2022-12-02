/*
 * Copyright 2022 eric
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package it.units.erallab.mrsim2d.buildable.builders;

import it.units.erallab.mrsim2d.core.Sensor;
import it.units.erallab.mrsim2d.core.agents.legged.AbstractLeggedHybridModularRobot;
import it.units.erallab.mrsim2d.core.bodies.RotationalJoint;
import it.units.malelab.jnb.core.Param;

import java.util.List;

public class LeggedMisc {
  private final static double RIGID_DENSITY = 0.25d;
  private final static double LEG_CHUNK_LENGTH = 1.5d;
  private final static double LEG_CHUNK_WIDTH = 1d;
  private final static double LEG_CHUNK_MASS = LEG_CHUNK_LENGTH * LEG_CHUNK_WIDTH * RIGID_DENSITY;
  private final static double TRUNK_LENGTH = 4d;
  private final static double TRUNK_WIDTH = 1d;
  private final static double TRUNK_MASS = TRUNK_LENGTH * TRUNK_WIDTH * RIGID_DENSITY;

  private LeggedMisc() {
  }

  @SuppressWarnings("unused")
  public static AbstractLeggedHybridModularRobot.LegChunk legChunk(
      @Param(value = "trunkLength", dD = LEG_CHUNK_LENGTH) double length,
      @Param(value = "width", dD = LEG_CHUNK_WIDTH) double width,
      @Param(value = "mass", dD = LEG_CHUNK_MASS) double mass,
      @Param(value = "upConnector", dS = "rigid") AbstractLeggedHybridModularRobot.Connector upConnector,
      @Param("jointSensors") List<Sensor<?>> jointSensors
  ) {
    return new AbstractLeggedHybridModularRobot.LegChunk(
        length,
        width,
        mass,
        new RotationalJoint.Motor(),
        upConnector,
        jointSensors
    );
  }


  @SuppressWarnings("unused")
  public static AbstractLeggedHybridModularRobot.Module module(
      @Param(value = "trunkLength", dD = TRUNK_LENGTH) double trunkLength,
      @Param(value = "trunkWidth", dD = TRUNK_WIDTH) double trunkWidth,
      @Param(value = "trunkMass", dD = TRUNK_MASS) double trunkMass,
      @Param("legChunks") List<AbstractLeggedHybridModularRobot.LegChunk> legChunks,
      @Param(value = "downConnector", dS = "rigid") AbstractLeggedHybridModularRobot.Connector downConnector,
      @Param(value = "rightConnector", dS = "rigid") AbstractLeggedHybridModularRobot.Connector rightConnector,
      @Param("trunkSensors") List<Sensor<?>> trunkSensors,
      @Param("rightConnectorSensors") List<Sensor<?>> rightConnectorSensors,
      @Param("downConnectorSensors") List<Sensor<?>> downConnectorSensors
  ) {
    return new AbstractLeggedHybridModularRobot.Module(
        trunkLength,
        trunkWidth,
        trunkMass,
        legChunks,
        downConnector,
        rightConnector,
        trunkSensors,
        rightConnectorSensors,
        downConnectorSensors
    );
  }

}
