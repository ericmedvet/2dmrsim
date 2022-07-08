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

import it.units.erallab.mrsim.core.geometry.Point;
import it.units.erallab.mrsim.core.geometry.Poly;
import org.dyn4j.dynamics.Body;
import org.dyn4j.geometry.*;

import java.util.Arrays;

/**
 * @author "Eric Medvet" on 2022/07/07 for 2dmrsim
 */
public class RigidBody implements it.units.erallab.mrsim.core.bodies.RigidBody {

  protected final static double FRICTION = 1d;
  protected final static double RESTITUTION = 0.5d;
  protected final static double LINEAR_DAMPING = 0.1d;
  protected final static double ANGULAR_DAMPING = 0.1d;

  private final Body body;
  private final double mass;
  private final Vector2 initialFirstSideDirection;

  public RigidBody(
      Poly convexPoly,
      double mass,
      double friction,
      double restitution,
      double linearDamping,
      double angularDamping
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
    initialFirstSideDirection = getFirstSideDirection();
  }

  private Vector2 getFirstSideDirection() {
    Poly poly = poly();
    return new Vector2(
        poly.vertexes()[1].x() - poly.vertexes()[0].x(),
        poly.vertexes()[1].y() - poly.vertexes()[0].y()
    );
  }

  protected Body getBody() {
    return body;
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
  public double mass() {
    return mass;
  }

  @Override
  public Point centerLinearVelocity() {
    Vector2 v = body.getLinearVelocity();
    return new Point(v.x, v.y);
  }

  @Override
  public double angle() {
    Vector2 currentFirstSideDirection = getFirstSideDirection();
    return currentFirstSideDirection.getAngleBetween(initialFirstSideDirection);
  }
}
