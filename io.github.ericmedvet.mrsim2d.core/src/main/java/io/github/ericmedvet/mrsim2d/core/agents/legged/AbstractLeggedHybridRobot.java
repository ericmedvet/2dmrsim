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

package io.github.ericmedvet.mrsim2d.core.agents.legged;

import io.github.ericmedvet.mrsim2d.core.ActionPerformer;
import io.github.ericmedvet.mrsim2d.core.EmbodiedAgent;
import io.github.ericmedvet.mrsim2d.core.Sensor;
import io.github.ericmedvet.mrsim2d.core.actions.*;
import io.github.ericmedvet.mrsim2d.core.bodies.*;
import io.github.ericmedvet.mrsim2d.core.engine.ActionException;
import io.github.ericmedvet.mrsim2d.core.geometry.Point;
import io.github.ericmedvet.mrsim2d.core.geometry.Poly;

import java.util.ArrayList;
import java.util.List;

public abstract class AbstractLeggedHybridRobot implements EmbodiedAgent {

  protected final List<Leg> legs;
  protected final double trunkLength;
  protected final double trunkWidth;
  protected final double trunkMass;
  protected final double headMass;


  protected final List<RotationalJoint> rotationalJoints;
  protected final List<LegBody> legBodies;
  private final List<Body> bodies;
  protected Body head;

  public AbstractLeggedHybridRobot(
      List<Leg> legs,
      double trunkLength,
      double trunkWidth,
      double trunkMass,
      double headMass
  ) {
    this.legs = legs;
    this.trunkLength = trunkLength;
    this.trunkWidth = trunkWidth;
    this.trunkMass = trunkMass;
    this.headMass = headMass;
    rotationalJoints = new ArrayList<>();
    legBodies = new ArrayList<>();
    bodies = new ArrayList<>();
  }

  public record Leg(
      List<LegChunk> legChunks,
      ConnectorType downConnector,
      double downConnectorMass,
      List<Sensor<?>> downConnectorSensors
  ) {}

  protected record LegBody(List<LegChunkBody> legChunks, Body downConnector) {}

  protected record LegChunkBody(Body upConnector, RotationalJoint joint) {}

  @Override
  public void assemble(ActionPerformer performer) throws ActionException {
    //create trunk
    RigidBody trunk = performer.perform(new CreateRigidBody(
            Poly.rectangle(trunkLength, trunkWidth),
            trunkMass,
            1d / trunkWidth
        ), this)
        .outcome()
        .orElseThrow(() -> new ActionException("Cannot create trunk"));
    bodies.add(trunk);
    //create and attach head
    head = performer.perform(new CreateRigidBody(
            Poly.square(trunkWidth),
            trunkMass,
            1d / trunkWidth
        ), this)
        .outcome()
        .orElseThrow(() -> new ActionException("Cannot create trunk"));
    bodies.add(head);
    performer.perform(new TranslateBodyAt(head, trunk.poly().boundingBox().max()), this);
    performer.perform(new AttachClosestAnchors(2, (Anchorable) head, trunk, Anchor.Link.Type.RIGID), this);
    //iterate over legs
    double maxLegWidth = legs.stream()
        .mapToDouble(l -> l.legChunks().stream()
            .mapToDouble(LegChunk::width)
            .max()
            .orElse(0d)
        )
        .max()
        .orElse(0d);
    double dCX = (trunk.poly().boundingBox().width() - 2 * maxLegWidth) / ((double) legs.size() - 1);
    double cX = trunk.poly().boundingBox().min().x() + maxLegWidth;
    for (Leg leg : legs) {
      Anchorable upperBody = trunk;
      List<LegChunkBody> chunkBodies = new ArrayList<>(leg.legChunks().size());
      for (LegChunk legChunk : leg.legChunks()) {
        double rotationalJointMass = legChunk.upConnector()
            .equals(ConnectorType.NONE) ? legChunk.mass() :
            (legChunk.mass() * legChunk.length() / (legChunk.length() + legChunk.width()));
        //create up connector
        Body upConnector = null;
        if (legChunk.upConnector().equals(ConnectorType.SOFT)) {
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
          upConnector = voxel;
        } else if (legChunk.upConnector().equals(ConnectorType.RIGID)) {
          RigidBody connector = performer.perform(new CreateRigidBody(
                  Poly.square(legChunk.width()),
                  legChunk.mass() - rotationalJointMass,
                  1d / legChunk.width()
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
          upConnector = connector;
        }
        //create joint
        RotationalJoint joint = performer.perform(new CreateRotationalJoint(
                legChunk.length(), legChunk.width(), rotationalJointMass, legChunk.motor(), legChunk.activeAngleRange()
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
        chunkBodies.add(new LegChunkBody(upConnector, joint));
      }
      cX = cX + dCX;
      //create down connector (foot)
      Body downConnector = null;
      if (leg.downConnector().equals(ConnectorType.SOFT)) {
        Voxel voxel = performer.perform(new CreateVoxel(
                upperBody.poly().boundingBox().width(), leg.downConnectorMass()
            ), this)
            .outcome()
            .orElseThrow(() -> new ActionException("Cannot leg chunk soft connector"));
        bodies.add(voxel);
        performer.perform(new TranslateBodyAt(
            voxel,
            upperBody.poly().boundingBox().min()
        ), this);
        performer.perform(new AttachClosestAnchors(2, voxel, upperBody, Anchor.Link.Type.RIGID));
        downConnector = voxel;
      } else if (leg.downConnector().equals(ConnectorType.RIGID)) {
        RigidBody connector = performer.perform(new CreateRigidBody(
                Poly.square(upperBody.poly().boundingBox().width()),
                leg.downConnectorMass(),
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
        downConnector = connector;
      }
      legBodies.add(new LegBody(chunkBodies, downConnector));
    }
  }

  @Override
  public List<Body> bodyParts() {
    return bodies;
  }
}
