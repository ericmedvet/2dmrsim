/*-
 * ========================LICENSE_START=================================
 * mrsim2d-engine-dyn4j
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

package io.github.ericmedvet.mrsim2d.engine.dyn4j;

import io.github.ericmedvet.jnb.datastructure.DoubleRange;
import io.github.ericmedvet.mrsim2d.core.bodies.Anchor;
import io.github.ericmedvet.mrsim2d.core.geometry.Point;
import io.github.ericmedvet.mrsim2d.core.geometry.Poly;
import java.util.*;
import java.util.function.DoubleFunction;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.dyn4j.collision.Filter;
import org.dyn4j.dynamics.Body;
import org.dyn4j.dynamics.joint.DistanceJoint;
import org.dyn4j.dynamics.joint.Joint;
import org.dyn4j.geometry.Circle;
import org.dyn4j.geometry.Convex;
import org.dyn4j.geometry.MassType;
import org.dyn4j.geometry.Vector2;

public class Voxel implements io.github.ericmedvet.mrsim2d.core.bodies.Voxel, MultipartBody {

  protected static final DoubleRange SPRING_F_RANGE = new DoubleRange(2d, 10d);
  protected static final double SPRING_D = 0.3d;
  private static final double CENTRAL_MASS_RATIO = 0.5d;
  private static final DoubleFunction<Convex> MASS_SHAPE_PROVIDER = l -> new Circle(l / 2d);
  protected final Map<Vertex, Body> vertexes;
  protected final List<Body> otherBodies;
  protected final Map<Side, List<DistanceJoint<Body>>> sideJoints;
  protected final List<DistanceJoint<Body>> centralJoints;
  protected final Map<Vertex, BodyAnchor> anchors;
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
  private final Vector2 initialSidesAverageDirection;

  private Collection<Body> bodies;
  private Collection<Joint<Body>> joints;

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
    otherBodies = new ArrayList<>();
    Arrays.stream(Side.values()).forEach(s -> sideJoints.put(s, new ArrayList<>()));
    centralJoints = new ArrayList<>();
    assemble();
    anchors = vertexes.entrySet()
        .stream()
        .collect(Collectors.toMap(Map.Entry::getKey, e -> new BodyAnchor(e.getValue(), this)));
    initialSidesAverageDirection = getSidesAverageDirection();
  }

  private enum BodyType {
    VERTEX, CENTRAL
  }

  public enum SpringScaffolding {
    SIDE_EXTERNAL, SIDE_INTERNAL, SIDE_CROSS, CENTRAL_CROSS
  }

  private record SpringRange(double min, double rest, double max) {

    public SpringRange {
      if ((min > rest) || (max < rest) || (min < 0)) {
        throw new IllegalArgumentException(String.format("Wrong spring range [%f, %f, %f]", min, rest, max));
      }
    }
  }

  private static class VoxelFilter extends BodyOwnerFilter {

    private final BodyType bodyType;

    public VoxelFilter(io.github.ericmedvet.mrsim2d.core.bodies.Body owner, BodyType bodyType) {
      super(owner);
      this.bodyType = bodyType;
    }

    @Override
    public boolean isAllowed(Filter otherFilter) {
      if (otherFilter instanceof VoxelFilter otherVoxelFilter) {
        return getOwner() != otherVoxelFilter.getOwner() || bodyType.equals(otherVoxelFilter.bodyType);
      }
      return true;
    }
  }

  protected void actuate(EnumMap<Side, Double> sideValues) {
    // apply on sides
    for (Map.Entry<Side, Double> sideEntry : sideValues.entrySet()) {
      double v = DoubleRange.SYMMETRIC_UNIT.clip(sideEntry.getValue());
      for (DistanceJoint<Body> joint : sideJoints.get(sideEntry.getKey())) {
        Voxel.SpringRange range = (SpringRange) joint.getUserData();
        if (v > 0) { // shrink
          joint.setRestDistance(range.rest - (range.rest - range.min) * v);
        } else if (v < 0) { // expand
          joint.setRestDistance(range.rest + (range.max - range.rest) * -v);
        }
      }
    }
    // apply on central
    double v = sideValues.values()
        .stream()
        .mapToDouble(DoubleRange.SYMMETRIC_UNIT::clip)
        .average()
        .orElse(0d);
    for (DistanceJoint<Body> joint : centralJoints) {
      Voxel.SpringRange range = (SpringRange) joint.getUserData();
      if (v >= 0) { // shrink
        joint.setRestDistance(range.rest - (range.rest - range.min) * v);
      } else if (v < 0) { // expand
        joint.setRestDistance(range.rest + (range.max - range.rest) * -v);
      }
    }
  }

  @Override
  public Anchor anchorOn(Vertex vertex) {
    return anchors.get(vertex);
  }

  @Override
  public Collection<Anchor> anchorsOn(Side side) {
    return switch (side) {
      case N -> List.of(anchors.get(Vertex.NW), anchors.get(Vertex.NE));
      case E -> List.of(anchors.get(Vertex.NE), anchors.get(Vertex.SE));
      case S -> List.of(anchors.get(Vertex.SE), anchors.get(Vertex.SW));
      case W -> List.of(anchors.get(Vertex.SW), anchors.get(Vertex.NW));
    };
  }

  @Override
  public Point vertex(Vertex vertex) {
    List<Point> centers = vertexes.values()
        .stream()
        .map(b -> Utils.point(b.getWorldCenter()))
        .toList();
    Point c = Point.average(centers.toArray(Point[]::new));
    double d = sideLength * vertexMassSideLengthRatio / 2d * Math.sqrt(2d);
    return enlongForm(c, Utils.point(vertexes.get(vertex).getWorldCenter()), d);
  }

  @SuppressWarnings({"unchecked", "rawtypes"})
  @Override
  public List<Anchor> anchors() {
    return (List) anchors.values().stream().toList();
  }

  @Override
  public double angle() {
    Vector2 currentSidesAverageDirection = getSidesAverageDirection();
    return -currentSidesAverageDirection.getAngleBetween(initialSidesAverageDirection);
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
  public double mass() {
    return mass;
  }

  @Override
  public Poly poly() {
    List<Point> centers = vertexes.values()
        .stream()
        .map(b -> Utils.point(b.getWorldCenter()))
        .toList();
    Point c = Point.average(centers.toArray(Point[]::new));
    double d = sideLength * vertexMassSideLengthRatio / 2d * Math.sqrt(2d);
    return new Poly(centers.stream().map(vc -> enlongForm(c, vc, d)).toArray(Point[]::new));
  }

  protected void assemble() {
    // compute densities
    double massSideLength = sideLength * vertexMassSideLengthRatio;
    double density = (mass * (1d - CENTRAL_MASS_RATIO) / 4d) / (massSideLength * massSideLength);
    // build bodies
    vertexes.put(Vertex.NW, new Body()); // 0
    vertexes.put(Vertex.NE, new Body()); // 1
    vertexes.put(Vertex.SE, new Body()); // 2
    vertexes.put(Vertex.SW, new Body()); // 3
    vertexes.values()
        .forEach(v -> v.addFixture(MASS_SHAPE_PROVIDER.apply(massSideLength), density, friction, restitution));
    vertexes.get(Vertex.NW)
        .translate(-(sideLength / 2d - massSideLength / 2d), (sideLength / 2d - massSideLength / 2d));
    vertexes.get(Vertex.NE)
        .translate((sideLength / 2d - massSideLength / 2d), (sideLength / 2d - massSideLength / 2d));
    vertexes.get(Vertex.SE)
        .translate((sideLength / 2d - massSideLength / 2d), -(sideLength / 2d - massSideLength / 2d));
    vertexes.get(Vertex.SW)
        .translate(-(sideLength / 2d - massSideLength / 2d), -(sideLength / 2d - massSideLength / 2d));
    for (Body body : vertexes.values()) {
      body.setMass(MassType.NORMAL);
      body.setLinearDamping(linearDamping);
      body.setAngularDamping(angularDamping);
    }
    // build distance joints constraints
    DoubleRange activeSideRange = new DoubleRange(
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
        Math.sqrt(
            massSideLength * massSideLength + sideParallelActiveRange.rest * sideParallelActiveRange.rest
        ),
        Math.sqrt(massSideLength * massSideLength + sideParallelActiveRange.max * sideParallelActiveRange.max)
    );
    SpringRange centralCrossActiveRange = new SpringRange(
        (activeSideRange.min() - massSideLength) * Math.sqrt(2d),
        (sideLength - massSideLength) * Math.sqrt(2d),
        (activeSideRange.max() - massSideLength) * Math.sqrt(2d)
    );
    // build distance joints
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
      centralJoints.add(
          new DistanceJoint<>(
              vertexes.get(Vertex.NW),
              vertexes.get(Vertex.SE),
              vertexes.get(Vertex.NW).getWorldCenter(),
              vertexes.get(Vertex.SE).getWorldCenter()
          )
      );
      centralJoints.add(
          new DistanceJoint<>(
              vertexes.get(Vertex.NE),
              vertexes.get(Vertex.SW),
              vertexes.get(Vertex.NE).getWorldCenter(),
              vertexes.get(Vertex.SW).getWorldCenter()
          )
      );
      for (DistanceJoint<Body> joint : centralJoints) {
        joint.setUserData(centralCrossActiveRange);
      }
    }
    // set collision filter
    vertexes.values()
        .forEach(b -> b.getFixtures().forEach(f -> f.setFilter(new VoxelFilter(this, BodyType.VERTEX))));
    // add central mass
    //noinspection ConstantConditions
    if (CENTRAL_MASS_RATIO > 0) {
      Body centralMass = new Body();
      centralMass.addFixture(
          new Circle(activeSideRange.min() / 2d),
          mass * CENTRAL_MASS_RATIO,
          friction,
          restitution
      );
      centralMass.setMass(MassType.NORMAL);
      centralMass.setLinearDamping(linearDamping);
      centralMass.setAngularDamping(angularDamping);
      centralMass.getFixtures().forEach(f -> f.setFilter(new VoxelFilter(this, BodyType.CENTRAL)));
      otherBodies.add(centralMass);
      for (Body vertex : vertexes.values()) {
        DistanceJoint<Body> joint = new DistanceJoint<>(
            centralMass,
            vertex,
            centralMass.getWorldCenter(),
            vertex.getWorldCenter()
        );
        joint.setUserData(
            new SpringRange(
                centralCrossActiveRange.min / 2d,
                centralCrossActiveRange.rest / 2d,
                centralCrossActiveRange.max / 2
            )
        );
        joint.setCollisionAllowed(false);
        centralJoints.add(joint);
      }
    }
    // setup spring joints
    Stream.of(
        sideJoints.get(Side.N),
        sideJoints.get(Side.E),
        sideJoints.get(Side.S),
        sideJoints.get(Side.W),
        centralJoints
    )
        .flatMap(Collection::stream)
        .forEach(j -> {
          if (j instanceof DistanceJoint<Body> joint) {
            joint.setRestDistance(((SpringRange) joint.getUserData()).rest);
            joint.setCollisionAllowed(true);
            joint.setFrequency(SPRING_F_RANGE.denormalize(softness));
            joint.setDampingRatio(SPRING_D);
          }
        });
    // set user data
    vertexes.values().forEach(b -> b.setUserData(this));
    otherBodies.forEach(b -> b.setUserData(this));
    // prepare lists
    bodies = Stream.of(vertexes.values(), otherBodies)
        .flatMap(Collection::stream)
        .toList();
    List<DistanceJoint<Body>> allJoints = Stream.of(
        sideJoints.get(Side.N),
        sideJoints.get(Side.E),
        sideJoints.get(Side.S),
        sideJoints.get(Side.W),
        centralJoints
    )
        .flatMap(Collection::stream)
        .toList();
    //noinspection unchecked,rawtypes
    joints = (List) allJoints;
  }

  private Point enlongForm(Point src, Point dst, double d) {
    return dst.sum(new Point(dst.diff(src).direction()).scale(d));
  }

  @Override
  public Collection<Body> getBodies() {
    return bodies;
  }

  @Override
  public Collection<Joint<Body>> getJoints() {
    return joints;
  }

  private Vector2 getSidesAverageDirection() {
    return new Vector2(
        vertexes.get(Vertex.NW).getWorldCenter().x - vertexes.get(Vertex.NE).getWorldCenter().x + vertexes.get(
            Vertex.SW
        ).getWorldCenter().x - vertexes.get(Vertex.SE).getWorldCenter().x,
        vertexes.get(Vertex.NW).getWorldCenter().y - vertexes.get(Vertex.NE).getWorldCenter().y + vertexes.get(
            Vertex.SW
        ).getWorldCenter().y - vertexes.get(Vertex.SE).getWorldCenter().y
    );
  }

  @Override
  public double restArea() {
    return sideLength * sideLength;
  }

  @Override
  public String toString() {
    return String.format("%s at %s", this.getClass().getSimpleName(), poly().center());
  }
}
