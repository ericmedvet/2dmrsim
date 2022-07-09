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
import org.dyn4j.dynamics.joint.Joint;
import org.dyn4j.geometry.MassType;
import org.dyn4j.geometry.Rectangle;
import org.dyn4j.geometry.Transform;
import org.dyn4j.geometry.Vector2;

import java.util.*;
import java.util.stream.Stream;

/**
 * @author "Eric Medvet" on 2022/07/08 for 2dmrsim
 */
public class Voxel implements it.units.erallab.mrsim.core.bodies.Voxel, MultipartBody {

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

  protected final Map<Vertex, Body> vertexes;
  protected final Map<Side, List<DistanceJoint<Body>>> sideJoints;
  protected final List<DistanceJoint<Body>> centralJoints;
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
    vertexes = new EnumMap<>(Vertex.class);
    sideJoints = new EnumMap<>(Side.class);
    Arrays.stream(Side.values()).forEach(s -> sideJoints.put(s, new ArrayList<>()));
    centralJoints = new ArrayList<>();
    assemble();
    anchors = vertexes.values().stream().map(v -> new BodyAnchor(v, this)).toList();
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
    vertexes.put(Vertex.NW, new Body()); // 0
    vertexes.put(Vertex.NE, new Body()); // 1
    vertexes.put(Vertex.SE, new Body()); // 2
    vertexes.put(Vertex.SW, new Body()); // 3
    vertexes.values().forEach(v -> v.addFixture(
        new Rectangle(massSideLength, massSideLength),
        density,
        friction,
        restitution
    ));
    vertexes.get(Vertex.NW).translate(
        -(sideLength / 2d - massSideLength / 2d),
        (sideLength / 2d - massSideLength / 2d)
    );
    vertexes.get(Vertex.NE).translate((sideLength / 2d - massSideLength / 2d), (sideLength / 2d - massSideLength / 2d));
    vertexes.get(Vertex.SE).translate(
        (sideLength / 2d - massSideLength / 2d),
        -(sideLength / 2d - massSideLength / 2d)
    );
    vertexes.get(Vertex.SW).translate(
        -(sideLength / 2d - massSideLength / 2d),
        -(sideLength / 2d - massSideLength / 2d)
    );
    for (Body body : vertexes.values()) {
      body.setMass(MassType.NORMAL);
      body.setLinearDamping(linearDamping);
      body.setAngularDamping(angularDamping);
    }
    //build distance joints constraints
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
      DistanceJoint<Body> nj = new DistanceJoint<>(
          vertexes.get(Vertex.NW),
          vertexes.get(Vertex.NE),
          vertexes.get(Vertex.NW).getWorldCenter().copy().add(massSideLength / 2d, -massSideLength / 2d),
          vertexes.get(Vertex.NE).getWorldCenter().copy().add(-massSideLength / 2d, -massSideLength / 2d)
      );
      DistanceJoint<Body> ej = new DistanceJoint<>(
          vertexes.get(Vertex.NE),
          vertexes.get(Vertex.SE),
          vertexes.get(Vertex.NE).getWorldCenter().copy().add(-massSideLength / 2d, -massSideLength / 2d),
          vertexes.get(Vertex.SE).getWorldCenter().copy().add(-massSideLength / 2d, massSideLength / 2d)
      );
      DistanceJoint<Body> sj = new DistanceJoint<>(
          vertexes.get(Vertex.SE),
          vertexes.get(Vertex.SW),
          vertexes.get(Vertex.SE).getWorldCenter().copy().add(-massSideLength / 2d, massSideLength / 2d),
          vertexes.get(Vertex.SW).getWorldCenter().copy().add(massSideLength / 2d, massSideLength / 2d)
      );
      DistanceJoint<Body> wj = new DistanceJoint<>(
          vertexes.get(Vertex.SW),
          vertexes.get(Vertex.NW),
          vertexes.get(Vertex.SW).getWorldCenter().copy().add(massSideLength / 2d, massSideLength / 2d),
          vertexes.get(Vertex.NW).getWorldCenter().copy().add(massSideLength / 2d, -massSideLength / 2d)
      );
      sideJoints.get(Side.N).add(nj);
      sideJoints.get(Side.E).add(ej);
      sideJoints.get(Side.S).add(sj);
      sideJoints.get(Side.W).add(wj);
      for (DistanceJoint<Body> joint : List.of(nj, ej, sj, wj)) {
        joint.setUserData(sideParallelActiveRange);
      }
    }
    if (springScaffoldings.contains(SpringScaffolding.SIDE_EXTERNAL)) {
      DistanceJoint<Body> nj = new DistanceJoint<>(
          vertexes.get(Vertex.NW),
          vertexes.get(Vertex.NE),
          vertexes.get(Vertex.NW).getWorldCenter().copy().add(massSideLength / 2d, massSideLength / 2d),
          vertexes.get(Vertex.NE).getWorldCenter().copy().add(-massSideLength / 2d, massSideLength / 2d)
      );
      DistanceJoint<Body> ej = new DistanceJoint<>(
          vertexes.get(Vertex.NE),
          vertexes.get(Vertex.SE),
          vertexes.get(Vertex.NE).getWorldCenter().copy().add(massSideLength / 2d, -massSideLength / 2d),
          vertexes.get(Vertex.SE).getWorldCenter().copy().add(massSideLength / 2d, massSideLength / 2d)
      );
      DistanceJoint<Body> sj = new DistanceJoint<>(
          vertexes.get(Vertex.SE),
          vertexes.get(Vertex.SW),
          vertexes.get(Vertex.SE).getWorldCenter().copy().add(-massSideLength / 2d, -massSideLength / 2d),
          vertexes.get(Vertex.SW).getWorldCenter().copy().add(massSideLength / 2d, -massSideLength / 2d)
      );
      DistanceJoint<Body> wj = new DistanceJoint<>(
          vertexes.get(Vertex.SW),
          vertexes.get(Vertex.NW),
          vertexes.get(Vertex.SW).getWorldCenter().copy().add(-massSideLength / 2d, massSideLength / 2d),
          vertexes.get(Vertex.NW).getWorldCenter().copy().add(-massSideLength / 2d, -massSideLength / 2d)
      );
      sideJoints.get(Side.N).add(nj);
      sideJoints.get(Side.E).add(ej);
      sideJoints.get(Side.S).add(sj);
      sideJoints.get(Side.W).add(wj);
      for (DistanceJoint<Body> joint : List.of(nj, ej, sj, wj)) {
        joint.setUserData(sideParallelActiveRange);
      }
    }
    if (springScaffoldings.contains(SpringScaffolding.SIDE_CROSS)) {
      DistanceJoint<Body> nj1 = new DistanceJoint<>(
          vertexes.get(Vertex.NW),
          vertexes.get(Vertex.NE),
          vertexes.get(Vertex.NW).getWorldCenter().copy().add(massSideLength / 2d, massSideLength / 2d),
          vertexes.get(Vertex.NE).getWorldCenter().copy().add(-massSideLength / 2d, -massSideLength / 2d)
      );
      DistanceJoint<Body> nj2 = new DistanceJoint<>(
          vertexes.get(Vertex.NW),
          vertexes.get(Vertex.NE),
          vertexes.get(Vertex.NW).getWorldCenter().copy().add(massSideLength / 2d, -massSideLength / 2d),
          vertexes.get(Vertex.NE).getWorldCenter().copy().add(-massSideLength / 2d, massSideLength / 2d)
      );
      DistanceJoint<Body> ej1 = new DistanceJoint<>(
          vertexes.get(Vertex.NE),
          vertexes.get(Vertex.SE),
          vertexes.get(Vertex.NE).getWorldCenter().copy().add(massSideLength / 2d, -massSideLength / 2d),
          vertexes.get(Vertex.SE).getWorldCenter().copy().add(-massSideLength / 2d, massSideLength / 2d)
      );
      DistanceJoint<Body> ej2 = new DistanceJoint<>(
          vertexes.get(Vertex.NE),
          vertexes.get(Vertex.SE),
          vertexes.get(Vertex.NE).getWorldCenter().copy().add(-massSideLength / 2d, -massSideLength / 2d),
          vertexes.get(Vertex.SE).getWorldCenter().copy().add(massSideLength / 2d, massSideLength / 2d)
      );
      DistanceJoint<Body> sj1 = new DistanceJoint<>(
          vertexes.get(Vertex.SE),
          vertexes.get(Vertex.SW),
          vertexes.get(Vertex.SE).getWorldCenter().copy().add(-massSideLength / 2d, massSideLength / 2d),
          vertexes.get(Vertex.SW).getWorldCenter().copy().add(massSideLength / 2d, -massSideLength / 2d)
      );
      DistanceJoint<Body> sj2 = new DistanceJoint<>(
          vertexes.get(Vertex.SE),
          vertexes.get(Vertex.SW),
          vertexes.get(Vertex.SE).getWorldCenter().copy().add(-massSideLength / 2d, -massSideLength / 2d),
          vertexes.get(Vertex.SW).getWorldCenter().copy().add(massSideLength / 2d, massSideLength / 2d)
      );
      DistanceJoint<Body> wj1 = new DistanceJoint<>(
          vertexes.get(Vertex.SW),
          vertexes.get(Vertex.NW),
          vertexes.get(Vertex.SW).getWorldCenter().copy().add(-massSideLength / 2d, massSideLength / 2d),
          vertexes.get(Vertex.NW).getWorldCenter().copy().add(massSideLength / 2d, -massSideLength / 2d)
      );
      DistanceJoint<Body> wj2 = new DistanceJoint<>(
          vertexes.get(Vertex.SW),
          vertexes.get(Vertex.NW),
          vertexes.get(Vertex.SW).getWorldCenter().copy().add(massSideLength / 2d, massSideLength / 2d),
          vertexes.get(Vertex.NW).getWorldCenter().copy().add(-massSideLength / 2d, -massSideLength / 2d)
      );
      sideJoints.get(Side.N).add(nj1);
      sideJoints.get(Side.N).add(nj2);
      sideJoints.get(Side.E).add(ej1);
      sideJoints.get(Side.E).add(ej2);
      sideJoints.get(Side.S).add(sj1);
      sideJoints.get(Side.S).add(sj2);
      sideJoints.get(Side.W).add(wj1);
      sideJoints.get(Side.W).add(wj2);
      for (DistanceJoint<Body> joint : List.of(nj1, nj2, ej1, ej2, sj1, sj2, wj1, wj2)) {
        joint.setUserData(sideCrossActiveRange);
      }
    }
    if (springScaffoldings.contains(SpringScaffolding.CENTRAL_CROSS)) {
      centralJoints.add(new DistanceJoint<>(
          vertexes.get(Vertex.NW),
          vertexes.get(Vertex.SE),
          vertexes.get(Vertex.NW).getWorldCenter(),
          vertexes.get(Vertex.SE).getWorldCenter()
      ));
      centralJoints.add(new DistanceJoint<>(
          vertexes.get(Vertex.NE),
          vertexes.get(Vertex.SW),
          vertexes.get(Vertex.NE).getWorldCenter(),
          vertexes.get(Vertex.SW).getWorldCenter()
      ));
      for (DistanceJoint<Body> joint : centralJoints) {
        joint.setUserData(centralCrossActiveRange);
      }
    }
    //setup spring joints
    getJoints().forEach(j -> {
      if (j instanceof DistanceJoint<Body> joint) {
        joint.setRestDistance(((SpringRange) joint.getUserData()).rest);
        joint.setCollisionAllowed(true);
        joint.setFrequency(SPRING_F_RANGE.denormalize(softness));
        joint.setDampingRatio(SPRING_D);
      }
    });
  }

  protected void actuate(EnumMap<Side, Double> sideValues) {
    //apply on sides
    for (Map.Entry<Side, Double> sideEntry : sideValues.entrySet()) {
      double v = sideEntry.getValue();
      for (DistanceJoint<Body> joint : sideJoints.get(sideEntry.getKey())) {
        Voxel.SpringRange range = (SpringRange) joint.getUserData();
        if (v >= 0) { // shrink
          joint.setRestDistance(range.rest - (range.rest - range.min) * v);
        } else if (v < 0) { // expand
          joint.setRestDistance(range.rest + (range.max - range.rest) * -v);
        }
      }
    }
    //apply on central
    double v = sideValues.values().stream().mapToDouble(Double::doubleValue).average().orElse(0d);
    for (DistanceJoint<Body> joint : centralJoints) {
      Voxel.SpringRange range = (SpringRange) joint.getUserData();
      if (v >= 0) { // shrink
        joint.setRestDistance(range.rest - (range.rest - range.min) * v);
      } else if (v < 0) { // expand
        joint.setRestDistance(range.rest + (range.max - range.rest) * -v);
      }
    }
  }

  protected void actuate(double value) {
    EnumMap<Side, Double> sideValues = new EnumMap<>(Side.class);
    for (Side side : Side.values()) {
      sideValues.put(side, value);
    }
    actuate(sideValues);
  }

  @Override
  public Collection<Body> getBodies() {
    return vertexes.values();
  }

  @SuppressWarnings({"unchecked", "rawtypes"})
  @Override
  public Collection<Joint<Body>> getJoints() {
    List<DistanceJoint<Body>> allJoints = Stream.of(
        sideJoints.get(Side.N),
        sideJoints.get(Side.E),
        sideJoints.get(Side.S),
        sideJoints.get(Side.W),
        centralJoints
    ).flatMap(Collection::stream).toList();
    return (List) allJoints;
  }

  private Point getIndexedVertex(Vertex vertex, int j) {
    Transform t = vertexes.get(vertex).getTransform();
    Rectangle rectangle = (Rectangle) vertexes.get(vertex).getFixture(0).getShape();
    Vector2 tV = rectangle.getVertices()[j].copy();
    t.transform(tV);
    return new Point(tV.x, tV.y);
  }

  @SuppressWarnings({"unchecked", "rawtypes"})
  @Override
  public List<Anchor> anchors() {
    return (List) anchors;
  }

  @Override
  public Poly poly() {
    return new Poly(
        getIndexedVertex(Vertex.NW, 3),
        getIndexedVertex(Vertex.NE, 2),
        getIndexedVertex(Vertex.SE, 1),
        getIndexedVertex(Vertex.SW, 0)
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
    for (Body vertex : vertexes.values()) {
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
