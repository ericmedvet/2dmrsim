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
import it.units.erallab.mrsim2d.core.actions.CreateRigidBody;
import it.units.erallab.mrsim2d.core.actions.CreateVoxel;
import it.units.erallab.mrsim2d.core.actions.TranslateBodyAt;
import it.units.erallab.mrsim2d.core.bodies.Body;
import it.units.erallab.mrsim2d.core.bodies.RigidBody;
import it.units.erallab.mrsim2d.core.bodies.RotationalJoint;
import it.units.erallab.mrsim2d.core.bodies.Voxel;
import it.units.erallab.mrsim2d.core.engine.ActionException;
import it.units.erallab.mrsim2d.core.geometry.Point;
import it.units.erallab.mrsim2d.core.geometry.Poly;

import java.util.ArrayList;
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
  private final List<Module> modules;
  private final List<Body> bodies;
  private final List<RotationalJoint> rotationalJoints;

  public LeggedHybridModularRobot(
      @Param("modules") List<Module> modules
  ) {
    this.modules = modules;
    bodies = new ArrayList<>();
    rotationalJoints = new ArrayList<>();
  }

  public enum Connector {NONE, SOFT, RIGID}

  public record LegChunk(
      double length,
      double width,
      double mass,
      RotationalJoint.Motor motor,
      Connector upConnector
  ) {
    @BuilderMethod
    public LegChunk(
        @Param(value = "trunkLength", dD = LEG_LENGTH) double length,
        @Param(value = "width", dD = LEG_WIDTH) double width,
        @Param(value = "mass", dD = LEG_MASS) double mass,
        @Param(value = "upConnector", dS = "rigid") Connector upConnector
    ) {
      this(length, width, mass, new RotationalJoint.Motor(), upConnector);
    }
  }

  public record Module(
      @Param(value = "trunkLength", dD = TRUNK_LENGTH) double trunkLength,
      @Param(value = "trunkWidth", dD = TRUNK_WIDTH) double trunkWidth,
      @Param(value = "trunkMass", dD = TRUNK_MASS) double trunkMass,
      @Param("legChunks") List<LegChunk> legChunks,
      @Param(value = "downConnector", dS = "rigid") Connector downConnector,
      @Param(value = "rightConnector", dS = "rigid") Connector rightConnector
  ) {}

  public static void main(String[] args) {
    NamedBuilder<Object> nb = NamedBuilder.empty()
        .and(NamedBuilder.fromClass(Module.class))
        .and(NamedBuilder.fromClass(LegChunk.class));
    System.out.println(NamedBuilder.prettyToString(nb, true));
    System.out.println(nb.build("module(legChunks=[legChunk(upConnector=soft);legChunk(upConnector=rigid)])"));
  }

  @Override
  public List<Action<?>> act(double t, List<ActionOutcome<?, ?>> previousActionOutcomes) {
    return null;
  }

  @Override
  public void assemble(ActionPerformer performer) throws ActionException {
    for (Module module : modules) {
      //create trunk
      double rigidTrunkMass = module.rightConnector()
          .equals(Connector.NONE) ? module.trunkMass() :
          (module.trunkMass() * module.trunkLength() / (module.trunkLength() + module.trunkWidth()));
      RigidBody trunk = performer.perform(new CreateRigidBody(
          Poly.rectangle(module.trunkLength(), module.trunkWidth()),
          rigidTrunkMass,
          true
      ), this).outcome().orElseThrow();
      bodies.add(trunk);
      double cX = trunk.poly().boundingBox().center().x();
      //create leg
      Body upperBody = trunk;
      for (LegChunk legChunk : module.legChunks()) {
        double rotationalJointMass = legChunk.upConnector()
            .equals(Connector.NONE) ? legChunk.mass() :
            (legChunk.mass() * legChunk.length() / (legChunk.length()) + legChunk.width());
        //create up connector
        if (legChunk.upConnector().equals(Connector.SOFT)) {
          Voxel voxel = performer.perform(new CreateVoxel(legChunk.width(), legChunk.mass() - rotationalJointMass))
              .outcome()
              .orElseThrow();
          bodies.add(voxel);
          performer.perform(new TranslateBodyAt(voxel,new Point(cX- legChunk.width()/2d,upperBody.poly().boundingBox().min().y())));
          upperBody = voxel;
        }
      }
      //create
    }
  }

  @Override
  public List<Body> bodyParts() {
    return bodies;
  }
}
