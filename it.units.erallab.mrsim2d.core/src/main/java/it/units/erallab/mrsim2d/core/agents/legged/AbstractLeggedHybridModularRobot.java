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
import it.units.erallab.mrsim2d.builder.Param;
import it.units.erallab.mrsim2d.core.ActionPerformer;
import it.units.erallab.mrsim2d.core.EmbodiedAgent;
import it.units.erallab.mrsim2d.core.actions.*;
import it.units.erallab.mrsim2d.core.bodies.*;
import it.units.erallab.mrsim2d.core.engine.ActionException;
import it.units.erallab.mrsim2d.core.geometry.Point;
import it.units.erallab.mrsim2d.core.geometry.Poly;

import java.util.ArrayList;
import java.util.List;

public abstract class AbstractLeggedHybridModularRobot implements EmbodiedAgent {

  private final static double RIGID_DENSITY = 0.25d;
  private final static double LEG_CHUNK_LENGTH = 1.5d;
  private final static double LEG_CHUNK_WIDTH = 1d;
  private final static double LEG_CHUNK_MASS = LEG_CHUNK_LENGTH * LEG_CHUNK_WIDTH * RIGID_DENSITY;
  private final static double TRUNK_LENGTH = 4d;
  private final static double TRUNK_WIDTH = 1d;
  private final static double TRUNK_MASS = TRUNK_LENGTH * TRUNK_WIDTH * RIGID_DENSITY;
  protected final List<RotationalJoint> rotationalJoints;
  protected final List<Module> modules;
  private final List<Body> bodies;

  public AbstractLeggedHybridModularRobot(List<Module> modules) {
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
        @Param(value = "trunkLength", dD = LEG_CHUNK_LENGTH) double length,
        @Param(value = "width", dD = LEG_CHUNK_WIDTH) double width,
        @Param(value = "mass", dD = LEG_CHUNK_MASS) double mass,
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

  @Override
  public void assemble(ActionPerformer performer) throws ActionException {
    Anchorable rightBody = null;
    for (Module module : modules) {
      //create trunk
      double rigidTrunkMass = module.rightConnector()
          .equals(Connector.NONE) ? module.trunkMass() :
          (module.trunkMass() * module.trunkLength() / (module.trunkLength() + module.trunkWidth()));
      RigidBody trunk = performer.perform(new CreateRigidBody(
              Poly.rectangle(module.trunkLength(), module.trunkWidth()),
              rigidTrunkMass,
              1d / module.trunkWidth
          ), this)
          .outcome()
          .orElseThrow(() -> new ActionException("Cannot create trunk"));
      bodies.add(trunk);
      if (rightBody != null) {
        performer.perform(new TranslateBodyAt(trunk, rightBody.poly().boundingBox().max()), this);
        performer.perform(new AttachClosestAnchors(2, trunk, rightBody, Anchor.Link.Type.RIGID), this);
      }
      rightBody = trunk;
      double cX = trunk.poly().boundingBox().center().x();
      //create leg
      Anchorable upperBody = trunk;
      for (LegChunk legChunk : module.legChunks()) {
        double rotationalJointMass = legChunk.upConnector()
            .equals(Connector.NONE) ? legChunk.mass() :
            (legChunk.mass() * legChunk.length() / (legChunk.length() + legChunk.width()));
        //create up connector
        if (legChunk.upConnector().equals(Connector.SOFT)) {
          Voxel voxel = performer.perform(new CreateVoxel(
                  legChunk.width(), legChunk.mass() - rotationalJointMass
              ), this)
              .outcome()
              .orElseThrow(() -> new ActionException("Cannot leg chunk soft connector"));
          bodies.add(voxel);
          performer.perform(new TranslateBodyAt(
              voxel,
              new Point(cX - legChunk.width() / 2d, upperBody.poly().boundingBox().min().y())
          ), this);
          performer.perform(new AttachClosestAnchors(2, voxel, upperBody, Anchor.Link.Type.RIGID));
          upperBody = voxel;
        } else if (legChunk.upConnector().equals(Connector.RIGID)) {
          RigidBody connector = performer.perform(new CreateRigidBody(
                  Poly.square(legChunk.width()),
                  legChunk.mass() - rotationalJointMass,
                  1d / legChunk.width
              ), this)
              .outcome()
              .orElseThrow(() -> new ActionException("Cannot leg chunk rigid connector"));
          bodies.add(connector);
          performer.perform(new TranslateBodyAt(
              connector,
              new Point(cX - legChunk.width() / 2d, upperBody.poly().boundingBox().min().y())
          ), this);
          performer.perform(new AttachClosestAnchors(2, connector, upperBody, Anchor.Link.Type.RIGID));
          upperBody = connector;
        }
        //create joint
        RotationalJoint joint = performer.perform(new CreateRotationalJoint(
                legChunk.length(), legChunk.width(), rotationalJointMass, legChunk.motor()
            ), this)
            .outcome()
            .orElseThrow(() -> new ActionException("Cannot leg chunk rotational joint"));
        bodies.add(joint);
        rotationalJoints.add(joint);
        performer.perform(new RotateBody(joint, Math.toRadians(90)), this);
        performer.perform(new TranslateBodyAt(
            joint,
            new Point(cX - legChunk.width() / 2d, upperBody.poly().boundingBox().min().y())
        ), this);
        performer.perform(new AttachClosestAnchors(2, joint, upperBody, Anchor.Link.Type.RIGID));
        upperBody = joint;
      }
      //create down connector (foot)
      if (module.downConnector().equals(Connector.SOFT)) {
        Voxel voxel = performer.perform(new CreateVoxel(
                upperBody.poly().boundingBox().width(), module.trunkMass() - rigidTrunkMass
            ), this)
            .outcome()
            .orElseThrow(() -> new ActionException("Cannot leg chunk soft connector"));
        bodies.add(voxel);
        performer.perform(new TranslateBodyAt(
            voxel,
            upperBody.poly().boundingBox().min()
        ), this);
        performer.perform(new AttachClosestAnchors(2, voxel, upperBody, Anchor.Link.Type.RIGID));
      } else if (module.downConnector().equals(Connector.RIGID)) {
        RigidBody connector = performer.perform(new CreateRigidBody(
                Poly.square(upperBody.poly().boundingBox().width()),
                module.trunkMass() - rigidTrunkMass,
                1d / upperBody.poly().boundingBox().width()
            ), this)
            .outcome()
            .orElseThrow(() -> new ActionException("Cannot leg chunk rigid connector"));
        bodies.add(connector);
        performer.perform(new TranslateBodyAt(
            connector,
            upperBody.poly().boundingBox().min()
        ), this);
        performer.perform(new AttachClosestAnchors(2, connector, upperBody, Anchor.Link.Type.RIGID));
      }
      //create right connector
      if (module.rightConnector().equals(Connector.SOFT)) {
        Voxel voxel = performer.perform(new CreateVoxel(
                module.trunkWidth(), module.trunkMass() - rigidTrunkMass
            ), this)
            .outcome()
            .orElseThrow(() -> new ActionException("Cannot leg chunk soft connector"));
        bodies.add(voxel);
        performer.perform(new TranslateBodyAt(
            voxel,
            trunk.poly().boundingBox().max()
        ), this);
        performer.perform(new AttachClosestAnchors(2, voxel, trunk, Anchor.Link.Type.RIGID));
        rightBody = voxel;
      } else if (module.rightConnector().equals(Connector.RIGID)) {
        RigidBody connector = performer.perform(new CreateRigidBody(
                Poly.square(module.trunkWidth()),
                module.trunkMass() - rigidTrunkMass,
                1d / module.trunkWidth
            ), this)
            .outcome()
            .orElseThrow(() -> new ActionException("Cannot leg chunk rigid connector"));
        bodies.add(connector);
        performer.perform(new TranslateBodyAt(
            connector,
            trunk.poly().boundingBox().max()
        ), this);
        performer.perform(new AttachClosestAnchors(2, connector, trunk, Anchor.Link.Type.RIGID));
        rightBody = connector;
      }
    }
  }

  @Override
  public List<Body> bodyParts() {
    return bodies;
  }
}
