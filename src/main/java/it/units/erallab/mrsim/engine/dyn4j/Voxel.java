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

import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumSet;
import java.util.List;

/**
 * @author "Eric Medvet" on 2022/07/08 for 2dmrsim
 */
public class Voxel implements it.units.erallab.mrsim.core.bodies.Voxel {
  protected final static double FRICTION = 1d;
  protected final static double RESTITUTION = 0.5d;
  protected final static double LINEAR_DAMPING = 0.1d;
  protected final static double ANGULAR_DAMPING = 0.1d;
  protected final static double VERTEX_MASS_SIDE_LENGTH_RATIO = 0.35d;
  private final EnumSet<SpringScaffolding> SPRING_SCAFFOLDING = EnumSet.allOf(SpringScaffolding.class);

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

  protected Body[] vertexBodies;
  protected List<DistanceJoint<Body>> springJoints;


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
    assemble();
  }

  protected void assemble() {
    //compute densities
    double massSideLength = sideLength * vertexMassSideLengthRatio;
    double density = (mass / 4) / (massSideLength * massSideLength);
    //build bodies
    vertexBodies = new Body[4];
    vertexBodies[0] = new Body(); //NW
    vertexBodies[1] = new Body(); //NE
    vertexBodies[2] = new Body(); //SE
    vertexBodies[3] = new Body(); //SW
    vertexBodies[0].addFixture(new Rectangle(massSideLength, massSideLength), density, friction, restitution);
    vertexBodies[1].addFixture(new Rectangle(massSideLength, massSideLength), density, friction, restitution);
    vertexBodies[2].addFixture(new Rectangle(massSideLength, massSideLength), density, friction, restitution);
    vertexBodies[3].addFixture(new Rectangle(massSideLength, massSideLength), density, friction, restitution);
    vertexBodies[0].translate(-(sideLength / 2d - massSideLength / 2d), +(sideLength / 2d - massSideLength / 2d));
    vertexBodies[1].translate(+(sideLength / 2d - massSideLength / 2d), +(sideLength / 2d - massSideLength / 2d));
    vertexBodies[2].translate(+(sideLength / 2d - massSideLength / 2d), -(sideLength / 2d - massSideLength / 2d));
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

  }

  @Override
  public Collection<Anchor> anchors() {
    return null;
  }

  @Override
  public Poly poly() {
    return null;
  }

  @Override
  public double mass() {
    return 0;
  }

  @Override
  public Point centerLinearVelocity() {
    return null;
  }

  @Override
  public double restArea() {
    return 0;
  }
}
