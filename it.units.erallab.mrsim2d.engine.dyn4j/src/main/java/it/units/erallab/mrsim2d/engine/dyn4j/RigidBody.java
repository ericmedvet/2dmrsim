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

package it.units.erallab.mrsim2d.engine.dyn4j;

import it.units.erallab.mrsim2d.core.bodies.Anchor;
import it.units.erallab.mrsim2d.core.geometry.Point;
import it.units.erallab.mrsim2d.core.geometry.Poly;
import it.units.erallab.mrsim2d.core.geometry.Segment;
import org.dyn4j.dynamics.Body;
import org.dyn4j.dynamics.joint.Joint;
import org.dyn4j.geometry.*;

import java.util.*;

/**
 * @author "Eric Medvet" on 2022/07/07 for 2dmrsim
 */
public class RigidBody implements it.units.erallab.mrsim2d.core.bodies.RigidBody, MultipartBody {

  private final Body body;
  private final double mass;
  private final Vector2 initialFirstSideDirection;
  private final List<Anchor> anchors;

  public RigidBody(
      Poly convexPoly,
      double mass,
      double anchorsDensity,
      double friction,
      double restitution,
      double linearDamping,
      double angularDamping,
      double anchorVertexToCenterRatio
  ) {
    this.mass = mass;
    Convex convex = new Polygon(
        Arrays.stream(convexPoly.vertexes()).sequential()
            .map(v -> new Vector2(v.x(), v.y()))
            .toArray(Vector2[]::new)
    );
    body = new Body();
    body.addFixture(convex, mass / convexPoly.area(), friction, restitution);
    body.setMass(MassType.NORMAL);
    body.setLinearDamping(linearDamping);
    body.setAngularDamping(angularDamping);
    body.setUserData(this);
    initialFirstSideDirection = getFirstSideDirection();
    if (Double.isFinite(anchorsDensity)) {
      List<Anchor> localAnchors = new ArrayList<>();
      for (Segment segment : convexPoly.sides()) {
        Point p1 = segment.p1();
        Point p2 = segment.p2();
        double l = p1.distance(p2);
        double nOfAnchors = Math.floor(l * anchorsDensity);
        double anchorInterval = l / nOfAnchors;
        Point dir = new Point(p2.diff(p1).direction());
        for (int i = 0; i < nOfAnchors; i = i + 1) {
          Point aP = p1.sum(dir.scale(anchorInterval * (double) i));
          localAnchors.add(new BodyAnchor(
              body,
              aP.diff(convexPoly.center()).scale(1 - anchorVertexToCenterRatio),
              this
          ));
        }
      }
      anchors = Collections.unmodifiableList(localAnchors);
    } else {
      anchors = List.of();
    }
  }

  @Override
  public List<Anchor> anchors() {
    return anchors;
  }

  @Override
  public double angle() {
    Vector2 currentFirstSideDirection = getFirstSideDirection();
    return -currentFirstSideDirection.getAngleBetween(initialFirstSideDirection);
  }

  @Override
  public Point centerLinearVelocity() {
    Vector2 v = body.getLinearVelocity();
    return new Point(v.x, v.y);
  }

  @Override
  public double mass() {
    return mass;
  }

  @Override
  public Poly poly() {
    Transform t = body.getTransform();
    return new Poly(
        Arrays.stream(((Polygon) body.getFixture(0).getShape()).getVertices())
            .map(v -> {
              Vector2 cv = v.copy();
              t.transform(cv);
              return new Point(cv.x, cv.y);
            })
            .toArray(Point[]::new)
    );
  }

  @Override
  public Collection<Body> getBodies() {
    return List.of(body);
  }

  @Override
  public Collection<Joint<Body>> getJoints() {
    return List.of();
  }

  private Vector2 getFirstSideDirection() {
    Poly poly = poly();
    return new Vector2(
        poly.vertexes()[1].x() - poly.vertexes()[0].x(),
        poly.vertexes()[1].y() - poly.vertexes()[0].y()
    );
  }

  @Override
  public String toString() {
    return String.format("%s at %s", this.getClass().getSimpleName(), poly().center());
  }
}
