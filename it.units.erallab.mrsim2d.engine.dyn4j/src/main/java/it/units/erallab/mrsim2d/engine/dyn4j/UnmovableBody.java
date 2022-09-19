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
import org.dyn4j.dynamics.AbstractPhysicsBody;
import org.dyn4j.dynamics.Body;
import org.dyn4j.dynamics.joint.Joint;
import org.dyn4j.geometry.Convex;
import org.dyn4j.geometry.MassType;
import org.dyn4j.geometry.Polygon;
import org.dyn4j.geometry.Vector2;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author "Eric Medvet" on 2022/07/07 for 2dmrsim
 */
public class UnmovableBody implements it.units.erallab.mrsim2d.core.bodies.UnmovableBody, MultipartBody {

  private final Poly poly;
  private final List<Body> bodies;
  private final List<Anchor> anchors;

  private final Point initialCenter;

  public UnmovableBody(
      Poly poly,
      boolean useAnchors,
      double friction,
      double restitution
  ) {
    this.poly = poly;
    List<Poly> parts = (poly.vertexes().length > 3) ? Utils.decompose(poly) : List.of(poly);
    bodies = parts.stream()
        .map(c -> {
          Convex convex = new Polygon(
              Arrays.stream(c.vertexes()).sequential()
                  .map(v -> new Vector2(v.x(), v.y()))
                  .toArray(Vector2[]::new)
          );
          Body body = new Body();
          body.addFixture(convex, 1d, friction, restitution);
          body.setMass(MassType.INFINITE);
          return body;
        })
        .toList();
    initialCenter = center(bodies);
    bodies.forEach(b -> b.setUserData(this));
    if (useAnchors) {
      anchors = poly.sides().stream()
          .map(s -> new BodyAnchor(
              bodies.get(0),
              new Point(bodies.get(0).getWorldCenter().x, bodies.get(0).getWorldCenter().y).diff(s.center()),
              this
          ))
          .collect(Collectors.toList());
    } else {
      anchors = List.of();
    }
  }

  private static Point center(List<Body> bodies) {
    return Point.average(bodies.stream()
        .map(AbstractPhysicsBody::getWorldCenter)
        .map(v -> new Point(v.x, v.y))
        .toArray(Point[]::new));
  }

  @Override
  public List<Anchor> anchors() {
    return anchors;
  }

  @Override
  public Collection<Body> getBodies() {
    return bodies;
  }

  @Override
  public Collection<Joint<Body>> getJoints() {
    return List.of();
  }

  @Override
  public Poly poly() {
    // assuming it can only be translated, we just check diff wrt initial center
    Point t = center(bodies).diff(initialCenter);
    return new Poly(Arrays.stream(poly.vertexes())
        .map(p -> p.sum(t))
        .toArray(Point[]::new));
  }

  @Override
  public String toString() {
    return String.format("%s at %s", this.getClass().getSimpleName(), poly().center());
  }
}
