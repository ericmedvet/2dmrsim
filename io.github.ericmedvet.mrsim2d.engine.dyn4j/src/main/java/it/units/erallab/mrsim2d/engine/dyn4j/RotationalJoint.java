package it.units.erallab.mrsim2d.engine.dyn4j;

import it.units.erallab.mrsim2d.core.bodies.Anchor;
import it.units.erallab.mrsim2d.core.geometry.Path;
import it.units.erallab.mrsim2d.core.geometry.Point;
import it.units.erallab.mrsim2d.core.geometry.Poly;
import it.units.erallab.mrsim2d.core.util.DoubleRange;
import org.dyn4j.dynamics.Body;
import org.dyn4j.dynamics.joint.Joint;
import org.dyn4j.dynamics.joint.RevoluteJoint;
import org.dyn4j.geometry.*;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

public class RotationalJoint implements it.units.erallab.mrsim2d.core.bodies.RotationalJoint, MultipartBody, Actuable {
  private static final DoubleRange JOINT_ANGLE_RANGE = new DoubleRange(Math.toRadians(-90), Math.toRadians(90));
  private static final boolean SET_LIMITS = false;

  private static final double ANCHOR_REL_GAP = 0.1;
  private final double mass;
  private final Motor motor;

  private final Body body1;
  private final Body body2;
  private final RevoluteJoint<Body> joint;
  private final double jointLength;

  private final List<Anchor> anchors;
  private final Vector2 initialRefDirection;

  private double jointTargetAngle;
  private double angleErrorSummation;
  private double lastAngleError;

  public RotationalJoint(
      double length,
      double width,
      double mass,
      Motor motor,
      double friction,
      double restitution,
      double linearDamping,
      double angularDamping,
      double anchorVertexToCenterRatio
  ) {
    //check length and with consistency
    if (length < width) {
      throw new IllegalArgumentException("Length must be at least %f%% w.r.t. width: found l=%f and w=%f".formatted(
          1d + ANCHOR_REL_GAP,
          length,
          width
      ));
    }
    this.mass = mass;
    this.motor = motor;
    jointLength = Math.sqrt(2d) * width / 2d;
    //create bodies
    Poly poly1 = new Path(Point.ORIGIN).moveBy(length / 2d - width / 2d, 0)
        .moveBy(width / 2d, width / 2d)
        .moveBy(-width / 2d, width / 2d)
        .moveBy(-(length / 2d - width / 2d), 0)
        .toPoly();
    @SuppressWarnings("SuspiciousNameCombination")
    Poly poly2 = new Path(new Point(length / 2d, width / 2d))
        .moveBy(width / 2d, -width / 2d)
        .moveBy(length / 2d - width / 2d, 0)
        .moveBy(0, width)
        .moveBy(-(length / 2d - width / 2d), 0)
        .toPoly();
    body1 = createBody(mass / 2d, friction, restitution, linearDamping, angularDamping, this, poly1);
    body2 = createBody(mass / 2d, friction, restitution, linearDamping, angularDamping, this, poly2);
    //create joint
    joint = new RevoluteJoint<>(body1, body2, new Vector2(length / 2d, width / 2d));
    //joint.setReferenceAngle(0);
    if (SET_LIMITS) {
      joint.setLimits(JOINT_ANGLE_RANGE.min(), JOINT_ANGLE_RANGE.max());
      joint.setLimitEnabled(true);
    }
    joint.setMotorEnabled(true);
    joint.setMaximumMotorTorque(motor.maxTorque());
    //create anchors
    anchors = List.of(
        new BodyAnchor(
            body1,
            poly1.vertexes()[0].diff(poly1.center()).scale(1 - anchorVertexToCenterRatio),
            this
        ),
        new BodyAnchor(
            body1,
            poly1.vertexes()[4].diff(poly1.center()).scale(1 - anchorVertexToCenterRatio),
            this
        ),
        new BodyAnchor(
            body2,
            poly2.vertexes()[2].diff(poly2.center()).scale(1 - anchorVertexToCenterRatio),
            this
        ),
        new BodyAnchor(
            body2,
            poly2.vertexes()[3].diff(poly2.center()).scale(1 - anchorVertexToCenterRatio),
            this
        )
    );
    //set initial first direction
    initialRefDirection = getRefDirection();
    //set control vars
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
      Poly poly
  ) {
    Convex convex = new Polygon(Arrays.stream(poly.vertexes())
        .map(p -> new Vector2(p.x(), p.y()))
        .toArray(Vector2[]::new));
    Body body = new Body();
    body.addFixture(convex, mass / poly.area(), friction, restitution);
    body.setMass(MassType.NORMAL);
    body.setLinearDamping(linearDamping);
    body.setAngularDamping(angularDamping);
    body.setUserData(userData);
    return body;
  }

  private static Poly polyFromBody(Body body) {
    Transform t = body.getTransform();
    return new Poly(Arrays.stream(((Polygon) body.getFixture(0).getShape()).getVertices()).map(v -> {
      Vector2 cv = v.copy();
      t.transform(cv);
      return new Point(cv.x, cv.y);
    }).toArray(Point[]::new));
  }

  @Override
  public void actuate(double t, double lastT) {
    //compute things
    double dT = t - lastT;
    double angleError = jointTargetAngle - jointAngle();
    angleErrorSummation = angleErrorSummation + angleError * dT;
    double angleDerivate = (angleError - lastAngleError) / dT;
    lastAngleError = angleError;
    //check if need to control
    if (Math.abs(angleError) < motor.angleTolerance()) {
      joint.setMotorSpeed(0d);
      return;
    }
    //control
    double motorSpeed = motor.controlP() * angleError
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
    Vector2 v1 = body1.getLinearVelocity();
    Vector2 v2 = body2.getLinearVelocity();
    return new Point((v1.x + v2.x) / 2d, (v1.y + v2.y) / 2d);
  }

  @Override
  public double mass() {
    return mass;
  }

  @Override
  public Poly poly() {
    Poly poly1 = polyFromBody(body1);
    Poly poly2 = polyFromBody(body2);
    Point[] ps1 = poly1.vertexes();
    Point[] ps2 = poly2.vertexes();
    Point[] ps = new Point[10];
    System.arraycopy(ps1, 0, ps, 0, 2);
    System.arraycopy(ps2, 0, ps, 2, 5);
    System.arraycopy(ps1, 2, ps, 7, 3);
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
        c2.x, c2.y
    );
  }

  @Override
  public double jointAngle() {
    return joint.getJointAngle();
  }

  @Override
  public DoubleRange jointAngleRange() {
    return JOINT_ANGLE_RANGE;
  }

  @Override
  public double jointLength() {
    return jointLength;
  }

  @Override
  public Point jointPoint() {
    return poly().vertexes()[2];
  }

  @Override
  public double jointTargetAngle() {
    return jointTargetAngle;
  }

  protected void setJointTargetAngle(double jointTargetAngle) {
    this.jointTargetAngle = jointAngleRange().clip(jointTargetAngle);
  }

  @Override
  public String toString() {
    return String.format("%s at %s", this.getClass().getSimpleName(), poly().center());
  }

}
