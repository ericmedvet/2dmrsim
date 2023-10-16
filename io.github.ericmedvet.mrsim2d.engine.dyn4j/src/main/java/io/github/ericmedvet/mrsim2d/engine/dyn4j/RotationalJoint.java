/*-
 * ========================LICENSE_START=================================
 * mrsim2d-engine-dyn4j
 * %%
 * Copyright (C) 2020 - 2023 Eric Medvet
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

import io.github.ericmedvet.jsdynsym.core.DoubleRange;
import io.github.ericmedvet.mrsim2d.core.bodies.Anchor;
import io.github.ericmedvet.mrsim2d.core.geometry.Path;
import io.github.ericmedvet.mrsim2d.core.geometry.Point;
import io.github.ericmedvet.mrsim2d.core.geometry.Poly;
import io.github.ericmedvet.mrsim2d.core.geometry.Segment;
import java.util.*;
import java.util.stream.IntStream;
import org.dyn4j.dynamics.Body;
import org.dyn4j.dynamics.joint.Joint;
import org.dyn4j.dynamics.joint.RevoluteJoint;
import org.dyn4j.geometry.MassType;
import org.dyn4j.geometry.Polygon;
import org.dyn4j.geometry.Transform;
import org.dyn4j.geometry.Vector2;

public class RotationalJoint
    implements io.github.ericmedvet.mrsim2d.core.bodies.RotationalJoint, MultipartBody, Actuable {
  private static final DoubleRange JOINT_PASSIVE_ANGLE_RANGE =
      new DoubleRange(Math.toRadians(-90), Math.toRadians(90));
  private static final boolean SET_LIMITS = false;

  private static final double ANCHOR_REL_GAP = 0.1;
  private final double mass;
  private final Motor motor;

  private final Body body1;
  private final Body body2;
  private final RevoluteJoint<Body> joint;
  private final double jointLength;
  private final List<List<Integer>> polyIndexes;

  private final List<Anchor> anchors;
  private final Vector2 initialRefDirection;
  private final DoubleRange jointActiveAngleRange;

  private double jointTargetAngle;
  private double angleErrorSummation;
  private double lastAngleError;

  public RotationalJoint(
      double length,
      double width,
      double mass,
      Motor motor,
      DoubleRange jointActiveAngleRange,
      double friction,
      double restitution,
      double linearDamping,
      double angularDamping,
      double anchorSideDistance) {
    // check length and with consistency
    if (length < width) {
      throw new IllegalArgumentException(
          "Length must be at least %f%% w.r.t. width: found l=%f and w=%f"
              .formatted(1d + ANCHOR_REL_GAP, length, width));
    }
    this.mass = mass;
    this.motor = motor;
    this.jointActiveAngleRange = jointActiveAngleRange;
    jointLength = Math.sqrt(2d) * width / 2d;
    // create bodies
    Poly poly1 =
        new Path(Point.ORIGIN)
            .moveBy(length / 2d - width / 2d, 0)
            .moveBy(width / 2d, width / 2d)
            .moveBy(-width / 2d, width / 2d)
            .moveBy(-(length / 2d - width / 2d), 0)
            .toPoly();
    @SuppressWarnings("SuspiciousNameCombination")
    Poly poly2 =
        new Path(new Point(length / 2d, width / 2d))
            .moveBy(width / 2d, -width / 2d)
            .moveBy(length / 2d - width / 2d, 0)
            .moveBy(0, width)
            .moveBy(-(length / 2d - width / 2d), 0)
            .toPoly();
    body1 =
        createBody(mass / 2d, friction, restitution, linearDamping, angularDamping, this, poly1);
    body2 =
        createBody(mass / 2d, friction, restitution, linearDamping, angularDamping, this, poly2);
    int jIndex1 =
        List.of(polyFromBody(body1).vertexes()).indexOf(new Point(length / 2d, width / 2d));
    int jIndex2 =
        List.of(polyFromBody(body2).vertexes()).indexOf(new Point(length / 2d, width / 2d));
    polyIndexes =
        List.of(
            IntStream.range(0, 5).map(i -> (i + jIndex1) % 5).boxed().toList(),
            IntStream.range(0, 5).map(i -> (i + jIndex2) % 5).boxed().toList());
    // create joint
    joint = new RevoluteJoint<>(body1, body2, new Vector2(length / 2d, width / 2d));
    // joint.setReferenceAngle(0);
    if (SET_LIMITS) {
      joint.setLimits(JOINT_PASSIVE_ANGLE_RANGE.min(), JOINT_PASSIVE_ANGLE_RANGE.max());
      joint.setLimitEnabled(true);
    }
    joint.setMotorEnabled(true);
    joint.setMaximumMotorTorque(motor.maxTorque());
    // create anchors
    List<Anchor> localAnchors = new ArrayList<>();
    List.of(0, 1, 3, 4)
        .forEach(
            i ->
                localAnchors.add(
                    new BodyAnchor(
                        body1,
                        new Segment(poly1.vertexes()[i], Utils.point(body1.getLocalCenter()))
                            .pointAtDistance(anchorSideDistance),
                        this)));
    List.of(1, 2, 3, 4)
        .forEach(
            i ->
                localAnchors.add(
                    new BodyAnchor(
                        body2,
                        new Segment(poly2.vertexes()[i], Utils.point(body2.getLocalCenter()))
                            .pointAtDistance(anchorSideDistance),
                        this)));
    anchors = Collections.unmodifiableList(localAnchors);
    // set initial first direction
    initialRefDirection = getRefDirection();
    // set control vars
    jointTargetAngle = 0;
    angleErrorSummation = 0;
    lastAngleError = 0;
  }

  private static Body createBody(
      double mass,
      double friction,
      double restitution,
      double linearDamping,
      double angularDamping,
      Object userData,
      Poly poly) {
    Body body = new Body();
    body.addFixture(Utils.poly(poly), mass / poly.area(), friction, restitution);
    body.setMass(MassType.NORMAL);
    body.setLinearDamping(linearDamping);
    body.setAngularDamping(angularDamping);
    body.setUserData(userData);
    return body;
  }

  private static Poly polyFromBody(Body body) {
    Transform t = body.getTransform();
    return new Poly(
        Arrays.stream(((Polygon) body.getFixture(0).getShape()).getVertices())
            .map(
                v -> {
                  Vector2 cv = v.copy();
                  t.transform(cv);
                  return Utils.point(cv);
                })
            .toArray(Point[]::new));
  }

  @Override
  public void actuate(double t, double lastT) {
    // compute things
    double dT = t - lastT;
    double angleError = jointTargetAngle - jointAngle();
    angleErrorSummation = angleErrorSummation + angleError * dT;
    double angleDerivate = (angleError - lastAngleError) / dT;
    lastAngleError = angleError;
    // check if need to control
    if (Math.abs(angleError) < motor.angleTolerance()) {
      joint.setMotorSpeed(0d);
      return;
    }
    // control
    double motorSpeed =
        motor.controlP() * angleError
            + motor.controlI() * angleErrorSummation
            + motor.controlD() * angleDerivate;
    if (motorSpeed > motor.maxSpeed()) {
      motorSpeed = motor.maxSpeed();
    } else if (motorSpeed < -motor.maxSpeed()) {
      motorSpeed = -motor.maxSpeed();
    }
    joint.setMotorSpeed(motorSpeed);
  }

  @Override
  public List<Anchor> anchors() {
    return anchors;
  }

  @Override
  public double angle() {
    Vector2 currentRefDirection = getRefDirection();
    return -currentRefDirection.getAngleBetween(initialRefDirection);
  }

  @Override
  public Point centerLinearVelocity() {
    return Point.average(
        Utils.point(body1.getLinearVelocity()), Utils.point(body2.getLinearVelocity()));
  }

  @Override
  public double mass() {
    return mass;
  }

  @Override
  public Poly poly() {
    Point[] ps1 = polyFromBody(body1).vertexes();
    Point[] ps2 = polyFromBody(body2).vertexes();
    Point[] ps = new Point[10];
    for (int i = 0; i < 5; i++) {
      ps[i] = ps1[polyIndexes.get(0).get(i)];
    }
    for (int i = 0; i < 5; i++) {
      ps[i + 5] = ps2[polyIndexes.get(1).get(i)];
    }
    return new Poly(ps);
  }

  @Override
  public Collection<Body> getBodies() {
    return List.of(body1, body2);
  }

  @Override
  public Collection<Joint<Body>> getJoints() {
    return List.of(joint);
  }

  private Vector2 getRefDirection() {
    Vector2 c1 = body1.getWorldCenter();
    Vector2 c2 = body2.getWorldCenter();
    return new Vector2(
        c1.x, c1.y,
        c2.x, c2.y);
  }

  @Override
  public DoubleRange jointActiveAngleRange() {
    return jointActiveAngleRange;
  }

  @Override
  public double jointAngle() {
    return joint.getJointAngle();
  }

  @Override
  public double jointLength() {
    return jointLength;
  }

  @Override
  public DoubleRange jointPassiveAngleRange() {
    return JOINT_PASSIVE_ANGLE_RANGE;
  }

  @Override
  public Point jointPoint() {
    return Utils.point(joint.getAnchor1());
  }

  @Override
  public double jointTargetAngle() {
    return jointTargetAngle;
  }

  protected void setJointTargetAngle(double jointTargetAngle) {
    this.jointTargetAngle = jointActiveAngleRange().clip(jointTargetAngle);
  }

  @Override
  public String toString() {
    return String.format("%s at %s", this.getClass().getSimpleName(), poly().center());
  }
}
