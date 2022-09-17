package it.units.erallab.mrsim2d.engine.dyn4j;

import it.units.erallab.mrsim2d.core.bodies.Anchor;
import it.units.erallab.mrsim2d.core.geometry.Path;
import it.units.erallab.mrsim2d.core.geometry.Point;
import it.units.erallab.mrsim2d.core.geometry.Poly;
import org.dyn4j.dynamics.Body;
import org.dyn4j.dynamics.joint.Joint;
import org.dyn4j.dynamics.joint.RevoluteJoint;
import org.dyn4j.geometry.*;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

public class RotationalJoint implements it.units.erallab.mrsim2d.core.bodies.RotationalJoint, MultipartBody {

  private static final double ANCHOR_REL_GAP = 0.1;

  private final double mass;
  private final Body body1;
  private final Body body2;
  private final RevoluteJoint<Body> joint;

  private final List<Anchor> anchors;
  private final Vector2 initialRefDirection;

  public RotationalJoint(
      double length,
      double width,
      double mass,
      double friction,
      double restitution,
      double linearDamping,
      double angularDamping
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
    //create bodies
    Path path1 = new Path(new Point(0, 0)).moveBy(length / 2d - width / 2d, 0)
        .moveBy(width / 2d, width / 2d)
        .moveBy(-width / 2d, width / 2d)
        .moveBy(-(length / 2d - width / 2d), 0);
    Path path2 = new Path(new Point(length / 2d, width / 2d))
        .moveBy(width / 2d, -width / 2d)
        .moveBy(length / 2d - width / 2d, 0)
        .moveBy(0, width)
        .moveBy(-(length / 2d - width / 2d), 0);
    body1 = createBody(mass / 2d, friction, restitution, linearDamping, angularDamping, this, new Poly(path1.points()));
    body2 = createBody(mass / 2d, friction, restitution, linearDamping, angularDamping, this, new Poly(path2.points()));
    //create joint
    joint = new RevoluteJoint<>(body1, body2, new Vector2(length / 2d, width / 2d));
    joint.setReferenceAngle(0);
    joint.setLimits(Math.toRadians(-90), Math.toRadians(90));
    joint.setLimitEnabled(true);
    //create anchors
    anchors = List.of(
        new BodyAnchor(body1, this),
        new BodyAnchor(body2, this)
    );
    //set initial first direction
    initialRefDirection = getRefDirection(poly());
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

  private static Vector2 getRefDirection(Poly poly) {
    Point pEast = poly.sides().get(9).center();
    Point pWest = poly.sides().get(4).center();
    return new Vector2(
        pEast.x(), pEast.y(),
        pWest.x(), pEast.y()
    );
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
  public List<Anchor> anchors() {
    return anchors;
  }

  @Override
  public double angle() {
    Vector2 currentRefDirection = getRefDirection(poly());
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
    Poly poly2 = polyFromBody(body1);
    Point[] ps1 = poly1.vertexes();
    Point[] ps2 = poly2.vertexes();
    Point[] ps = new Point[10];
    System.arraycopy(ps1, 0, ps, 0, 3);
    System.arraycopy(ps2, 0, ps, 3, 5);
    System.arraycopy(ps1, 3, ps, 8, 2);
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
}
