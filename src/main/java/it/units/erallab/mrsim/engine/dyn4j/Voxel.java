/*
 * Copyright 2022 Eric Medvet <eric.medvet@gmail.com> (as eric)
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

package it.units.erallab.mrsim.engine.dyn4j;

import it.units.erallab.mrsim.core.bodies.Anchor;
import it.units.erallab.mrsim.core.geometry.Point;
import it.units.erallab.mrsim.core.geometry.Poly;
import it.units.erallab.mrsim.util.DoubleRange;
import org.dyn4j.dynamics.Body;
import org.dyn4j.dynamics.joint.DistanceJoint;
import org.dyn4j.geometry.MassType;
import org.dyn4j.geometry.Rectangle;
import org.dyn4j.geometry.Transform;
import org.dyn4j.geometry.Vector2;

import java.util.*;

/**
 * @author "Eric Medvet" on 2022/07/08 for 2dmrsim
 */
public class Voxel implements it.units.erallab.mrsim.core.bodies.Voxel {

  protected final static double FRICTION = 1d;
  protected final static double RESTITUTION = 0.5d;
  protected final static double LINEAR_DAMPING = 0.1d;
  protected final static double ANGULAR_DAMPING = 0.1d;
  protected final static double VERTEX_MASS_SIDE_LENGTH_RATIO = 0.35d;
  protected final static EnumSet<SpringScaffolding> SPRING_SCAFFOLDINGS = EnumSet.allOf(SpringScaffolding.class);
  protected static final DoubleRange SPRING_F_RANGE = new DoubleRange(2d, 10d);
  protected static final double SPRING_D = 0.3d;

  private record SpringRange(double min, double rest, double max) {

    public SpringRange {
      if ((min > rest) || (max < rest) || (min < 0)) {
        throw new IllegalArgumentException(String.format("Wrong spring range [%f, %f, %f]", min, rest, max));
      }
    }

  }

  protected enum SpringScaffolding {
    SIDE_EXTERNAL, SIDE_INTERNAL, SIDE_CROSS, CENTRAL_CROSS
  }


  private final double sideLength;
  private final double mass;
  private final double friction;
  private final double restitution;
  private final double softness;
  private final double linearDamping;
  private final double angularDamping;
  private final double vertexMassSideLengthRatio;
  private final DoubleRange areaRatioActiveRange;
  private final EnumSet<SpringScaffolding> springScaffoldings;

  protected final Body[] vertexBodies;
  protected final List<DistanceJoint<Body>> springJoints;
  protected final List<BodyAnchor> anchors;
  private final Vector2 initialSidesAverageDirection;


  public Voxel(
      double sideLength,
      double mass,
      double friction,
      double restitution,
      double softness,
      double linearDamping,
      double angularDamping,
      double vertexMassSideLengthRatio,
      DoubleRange areaRatioActiveRange,
      EnumSet<SpringScaffolding> springScaffoldings
  ) {
    this.sideLength = sideLength;
    this.mass = mass;
    this.friction = friction;
    this.restitution = restitution;
    this.softness = softness;
    this.linearDamping = linearDamping;
    this.angularDamping = angularDamping;
    this.vertexMassSideLengthRatio = vertexMassSideLengthRatio;
    this.areaRatioActiveRange = areaRatioActiveRange;
    this.springScaffoldings = springScaffoldings;
    vertexBodies = new Body[4];
    springJoints = new ArrayList<>();
    assemble();
    anchors = Arrays.stream(vertexBodies).map(BodyAnchor::new).toList();
    initialSidesAverageDirection = getSidesAverageDirection();
  }

  private Vector2 getSidesAverageDirection() {
    Poly poly = poly();
    return new Vector2(
        poly.vertexes()[0].x() - poly.vertexes()[1].x() + poly.vertexes()[3].x() - poly.vertexes()[2].x(),
        poly.vertexes()[0].y() - poly.vertexes()[1].y() + poly.vertexes()[3].y() - poly.vertexes()[2].y()
    );
  }

  protected void assemble() {
    //compute densities
    double massSideLength = sideLength * vertexMassSideLengthRatio;
    double density = (mass / 4) / (massSideLength * massSideLength);
    //build bodies
    vertexBodies[0] = new Body(); //NW
    vertexBodies[1] = new Body(); //NE
    vertexBodies[2] = new Body(); //SE
    vertexBodies[3] = new Body(); //SW
    vertexBodies[0].addFixture(new Rectangle(massSideLength, massSideLength), density, friction, restitution);
    vertexBodies[1].addFixture(new Rectangle(massSideLength, massSideLength), density, friction, restitution);
    vertexBodies[2].addFixture(new Rectangle(massSideLength, massSideLength), density, friction, restitution);
    vertexBodies[3].addFixture(new Rectangle(massSideLength, massSideLength), density, friction, restitution);
    vertexBodies[0].translate(-(sideLength / 2d - massSideLength / 2d), (sideLength / 2d - massSideLength / 2d));
    vertexBodies[1].translate((sideLength / 2d - massSideLength / 2d), (sideLength / 2d - massSideLength / 2d));
    vertexBodies[2].translate((sideLength / 2d - massSideLength / 2d), -(sideLength / 2d - massSideLength / 2d));
    vertexBodies[3].translate(-(sideLength / 2d - massSideLength / 2d), -(sideLength / 2d - massSideLength / 2d));
    for (Body body : vertexBodies) {
      body.setMass(MassType.NORMAL);
      body.setLinearDamping(linearDamping);
      body.setAngularDamping(angularDamping);
    }
    //build distance joints constraints
    List<DistanceJoint<Body>> allSpringJoints = new ArrayList<>();
    DoubleRange activeSideRange = DoubleRange.of(
        Math.sqrt(sideLength * sideLength * areaRatioActiveRange.min()),
        Math.sqrt(sideLength * sideLength * areaRatioActiveRange.max())
    );
    SpringRange sideParallelActiveRange = new SpringRange(
        activeSideRange.min() - 2d * massSideLength,
        sideLength - 2d * massSideLength,
        activeSideRange.max() - 2d * massSideLength
    );
    SpringRange sideCrossActiveRange = new SpringRange(
        Math.sqrt(massSideLength * massSideLength + sideParallelActiveRange.min * sideParallelActiveRange.min),
        Math.sqrt(massSideLength * massSideLength + sideParallelActiveRange.rest * sideParallelActiveRange.rest),
        Math.sqrt(massSideLength * massSideLength + sideParallelActiveRange.max * sideParallelActiveRange.max)
    );
    SpringRange centralCrossActiveRange = new SpringRange(
        (activeSideRange.min() - massSideLength) * Math.sqrt(2d),
        (sideLength - massSideLength) * Math.sqrt(2d),
        (activeSideRange.max() - massSideLength) * Math.sqrt(2d)
    );
    //build distance joints
    if (springScaffoldings.contains(SpringScaffolding.SIDE_INTERNAL)) {
      List<DistanceJoint<Body>> localSpringJoints = new ArrayList<>();
      localSpringJoints.add(new DistanceJoint<>(
          vertexBodies[0],
          vertexBodies[1],
          vertexBodies[0].getWorldCenter().copy().add(massSideLength / 2d, -massSideLength / 2d),
          vertexBodies[1].getWorldCenter().copy().add(-massSideLength / 2d, -massSideLength / 2d)
      ));
      localSpringJoints.add(new DistanceJoint<>(
          vertexBodies[1],
          vertexBodies[2],
          vertexBodies[1].getWorldCenter().copy().add(-massSideLength / 2d, -massSideLength / 2d),
          vertexBodies[2].getWorldCenter().copy().add(-massSideLength / 2d, massSideLength / 2d)
      ));
      localSpringJoints.add(new DistanceJoint<>(
          vertexBodies[2],
          vertexBodies[3],
          vertexBodies[2].getWorldCenter().copy().add(-massSideLength / 2d, massSideLength / 2d),
          vertexBodies[3].getWorldCenter().copy().add(massSideLength / 2d, massSideLength / 2d)
      ));
      localSpringJoints.add(new DistanceJoint<>(
          vertexBodies[3],
          vertexBodies[0],
          vertexBodies[3].getWorldCenter().copy().add(massSideLength / 2d, massSideLength / 2d),
          vertexBodies[0].getWorldCenter().copy().add(massSideLength / 2d, -massSideLength / 2d)
      ));
      for (DistanceJoint<Body> joint : localSpringJoints) {
        joint.setUserData(sideParallelActiveRange);
      }
      allSpringJoints.addAll(localSpringJoints);
    }
    if (springScaffoldings.contains(SpringScaffolding.SIDE_EXTERNAL)) {
      List<DistanceJoint<Body>> localSpringJoints = new ArrayList<>();
      localSpringJoints.add(new DistanceJoint<>(
          vertexBodies[0],
          vertexBodies[1],
          vertexBodies[0].getWorldCenter().copy().add(massSideLength / 2d, massSideLength / 2d),
          vertexBodies[1].getWorldCenter().copy().add(-massSideLength / 2d, massSideLength / 2d)
      ));
      localSpringJoints.add(new DistanceJoint<>(
          vertexBodies[1],
          vertexBodies[2],
          vertexBodies[1].getWorldCenter().copy().add(massSideLength / 2d, -massSideLength / 2d),
          vertexBodies[2].getWorldCenter().copy().add(massSideLength / 2d, massSideLength / 2d)
      ));
      localSpringJoints.add(new DistanceJoint<>(
          vertexBodies[2],
          vertexBodies[3],
          vertexBodies[2].getWorldCenter().copy().add(-massSideLength / 2d, -massSideLength / 2d),
          vertexBodies[3].getWorldCenter().copy().add(massSideLength / 2d, -massSideLength / 2d)
      ));
      localSpringJoints.add(new DistanceJoint<>(
          vertexBodies[3],
          vertexBodies[0],
          vertexBodies[3].getWorldCenter().copy().add(-massSideLength / 2d, massSideLength / 2d),
          vertexBodies[0].getWorldCenter().copy().add(-massSideLength / 2d, -massSideLength / 2d)
      ));
      for (DistanceJoint<Body> joint : localSpringJoints) {
        joint.setUserData(sideParallelActiveRange);
      }
      allSpringJoints.addAll(localSpringJoints);
    }
    if (springScaffoldings.contains(SpringScaffolding.SIDE_CROSS)) {
      List<DistanceJoint<Body>> localSpringJoints = new ArrayList<>();
      localSpringJoints.add(new DistanceJoint<>(
          vertexBodies[0],
          vertexBodies[1],
          vertexBodies[0].getWorldCenter().copy().add(massSideLength / 2d, massSideLength / 2d),
          vertexBodies[1].getWorldCenter().copy().add(-massSideLength / 2d, -massSideLength / 2d)
      ));
      localSpringJoints.add(new DistanceJoint<>(
          vertexBodies[0],
          vertexBodies[1],
          vertexBodies[0].getWorldCenter().copy().add(massSideLength / 2d, -massSideLength / 2d),
          vertexBodies[1].getWorldCenter().copy().add(-massSideLength / 2d, massSideLength / 2d)
      ));
      localSpringJoints.add(new DistanceJoint<>(
          vertexBodies[1],
          vertexBodies[2],
          vertexBodies[1].getWorldCenter().copy().add(massSideLength / 2d, -massSideLength / 2d),
          vertexBodies[2].getWorldCenter().copy().add(-massSideLength / 2d, massSideLength / 2d)
      ));
      localSpringJoints.add(new DistanceJoint<>(
          vertexBodies[1],
          vertexBodies[2],
          vertexBodies[1].getWorldCenter().copy().add(-massSideLength / 2d, -massSideLength / 2d),
          vertexBodies[2].getWorldCenter().copy().add(massSideLength / 2d, massSideLength / 2d)
      ));
      localSpringJoints.add(new DistanceJoint<>(
          vertexBodies[2],
          vertexBodies[3],
          vertexBodies[2].getWorldCenter().copy().add(-massSideLength / 2d, massSideLength / 2d),
          vertexBodies[3].getWorldCenter().copy().add(massSideLength / 2d, -massSideLength / 2d)
      ));
      localSpringJoints.add(new DistanceJoint<>(
          vertexBodies[2],
          vertexBodies[3],
          vertexBodies[2].getWorldCenter().copy().add(-massSideLength / 2d, -massSideLength / 2d),
          vertexBodies[3].getWorldCenter().copy().add(massSideLength / 2d, massSideLength / 2d)
      ));
      localSpringJoints.add(new DistanceJoint<>(
          vertexBodies[3],
          vertexBodies[0],
          vertexBodies[3].getWorldCenter().copy().add(-massSideLength / 2d, massSideLength / 2d),
          vertexBodies[0].getWorldCenter().copy().add(massSideLength / 2d, -massSideLength / 2d)
      ));
      localSpringJoints.add(new DistanceJoint<>(
          vertexBodies[3],
          vertexBodies[0],
          vertexBodies[3].getWorldCenter().copy().add(massSideLength / 2d, massSideLength / 2d),
          vertexBodies[0].getWorldCenter().copy().add(-massSideLength / 2d, -massSideLength / 2d)
      ));
      for (DistanceJoint<Body> joint : localSpringJoints) {
        joint.setUserData(sideCrossActiveRange);
      }
      allSpringJoints.addAll(localSpringJoints);
    }
    if (springScaffoldings.contains(SpringScaffolding.CENTRAL_CROSS)) {
      List<DistanceJoint<Body>> localSpringJoints = new ArrayList<>();
      localSpringJoints.add(new DistanceJoint<>(
          vertexBodies[0],
          vertexBodies[2],
          vertexBodies[0].getWorldCenter(),
          vertexBodies[2].getWorldCenter()
      ));
      localSpringJoints.add(new DistanceJoint<>(
          vertexBodies[1],
          vertexBodies[3],
          vertexBodies[1].getWorldCenter(),
          vertexBodies[3].getWorldCenter()
      ));
      for (DistanceJoint<Body> joint : localSpringJoints) {
        joint.setUserData(centralCrossActiveRange);
      }
      allSpringJoints.addAll(localSpringJoints);
    }
    //setup spring joints
    for (DistanceJoint<Body> joint : allSpringJoints) {
      joint.setRestDistance(((SpringRange) joint.getUserData()).rest);
      joint.setCollisionAllowed(true);
      joint.setFrequency(SPRING_F_RANGE.denormalize(softness));
      joint.setDampingRatio(SPRING_D);
    }
    springJoints.addAll(allSpringJoints);
  }

  protected Body[] getVertexBodies() {
    return vertexBodies;
  }

  protected List<DistanceJoint<Body>> getSpringJoints() {
    return springJoints;
  }

  private Point getIndexedVertex(int i, int j) {
    Transform t = vertexBodies[i].getTransform();
    Rectangle rectangle = (Rectangle) vertexBodies[i].getFixture(0).getShape();
    Vector2 tV = rectangle.getVertices()[j].copy();
    t.transform(tV);
    return new Point(tV.x, tV.y);
  }

  public void translate(Point t) {
    for (Body body : vertexBodies) {
      body.translate(new Vector2(t.x(), t.y()));
    }
  }

  @SuppressWarnings({"unchecked", "rawtypes"})
  @Override
  public List<Anchor> anchors() {
    return (List)anchors;
  }

  @Override
  public Poly poly() {
    return new Poly(
        getIndexedVertex(0, 3),
        getIndexedVertex(1, 2),
        getIndexedVertex(2, 1),
        getIndexedVertex(3, 0)
    );
  }

  @Override
  public double mass() {
    return mass;
  }

  @Override
  public Point centerLinearVelocity() {
    double x = 0d;
    double y = 0d;
    for (Body vertex : vertexBodies) {
      x = x + vertex.getLinearVelocity().x;
      y = y + vertex.getLinearVelocity().y;
    }
    return new Point(x / 4d, y / 4d);
  }

  @Override
  public double restArea() {
    return sideLength * sideLength;
  }

  @Override
  public double angle() {
    Vector2 currentSidesAverageDirection = getSidesAverageDirection();
    return currentSidesAverageDirection.getAngleBetween(initialSidesAverageDirection);
  }
}
