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

package it.units.erallab.mrsim2d.core.agents.legged;

import it.units.erallab.mrsim2d.builder.BuilderMethod;
import it.units.erallab.mrsim2d.builder.NamedBuilder;
import it.units.erallab.mrsim2d.builder.Param;
import it.units.erallab.mrsim2d.core.Action;
import it.units.erallab.mrsim2d.core.ActionOutcome;
import it.units.erallab.mrsim2d.core.ActionPerformer;
import it.units.erallab.mrsim2d.core.EmbodiedAgent;
import it.units.erallab.mrsim2d.core.bodies.Body;
import it.units.erallab.mrsim2d.core.bodies.RotationalJoint;
import it.units.erallab.mrsim2d.core.engine.ActionException;

import java.util.List;

public class LeggedHybridModularRobot implements EmbodiedAgent {

  private final static double LEG_LENGTH = 2d;
  private final static double LEG_WIDTH = 1d;
  private final static double LEG_MASS = LEG_LENGTH * LEG_WIDTH;
  private final static double LEG_CONNECTOR_MASS = LEG_WIDTH * LEG_WIDTH;
  private final static double TRUNK_LENGTH = 3d;
  private final static double TRUNK_WIDTH = 2d;
  private final static double TRUNK_CONNECTOR_MASS = TRUNK_LENGTH * TRUNK_WIDTH;
  private final static double TRUNK_MASS = 6d;

  public enum Connector {NONE, SOFT, RIGID}

  public record Leg(
      @Param("legChunks") List<LegChunk> legChunks,
      @Param("downConnector") Connector downConnector
  ) {}

  public record LegChunk(
      double length,
      double width,
      double mass,
      RotationalJoint.Motor motor,
      Connector upConnector
  ) {
    @BuilderMethod
    public LegChunk(
        @Param(value = "length", dD = LEG_LENGTH) double length,
        @Param(value = "width", dD = LEG_WIDTH) double width,
        @Param(value = "mass", dD = LEG_MASS) double mass,
        @Param("upConnector") Connector upConnector
    ) {
      this(length, width, mass, new RotationalJoint.Motor(), upConnector);
    }
  }

  public record Module(
      @Param("length") double length,
      @Param("width") double width,
      @Param("mass") double mass,
      @Param("rightConnector") Connector rightConnector
  ) {}

  public static void main(String[] args) {
    NamedBuilder<Object> nb = NamedBuilder.empty()
        .and(NamedBuilder.fromClass(Module.class))
        .and(NamedBuilder.fromClass(Leg.class))
        .and(NamedBuilder.fromClass(LegChunk.class));
    System.out.println(NamedBuilder.prettyToString(nb, true));
    System.out.println(nb.build("legChunk(upConnector=none)"));
    //TODO add enums to autobuilder
  }

  @Override
  public List<Action<?>> act(double t, List<ActionOutcome<?, ?>> previousActionOutcomes) {
    return null;
  }

  @Override
  public void assemble(ActionPerformer actionPerformer) throws ActionException {

  }

  @Override
  public List<Body> bodyParts() {
    return null;
  }
}
