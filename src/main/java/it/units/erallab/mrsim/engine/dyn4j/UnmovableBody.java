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

import it.units.erallab.mrsim.core.geometry.Poly;
import it.units.erallab.mrsim.util.PolyUtils;
import org.dyn4j.dynamics.Body;
import org.dyn4j.dynamics.joint.Joint;
import org.dyn4j.geometry.Convex;
import org.dyn4j.geometry.MassType;
import org.dyn4j.geometry.Polygon;
import org.dyn4j.geometry.Vector2;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

/**
 * @author "Eric Medvet" on 2022/07/07 for 2dmrsim
 */
public class UnmovableBody implements it.units.erallab.mrsim.core.bodies.UnmovableBody, MultipartBody {

  protected final static double FRICTION = 1d;
  protected final static double RESTITUTION = 0.5d;

  private final Poly poly;
  private final List<Body> bodies;

  public UnmovableBody(
      Poly poly,
      double friction,
      double restitution
  ) {
    this.poly = poly;
    bodies = PolyUtils.decompose(poly).stream()
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
  }

  @Override
  public Poly poly() {
    return poly;
  }

  @Override
  public Collection<Body> getBodies() {
    return bodies;
  }

  @Override
  public Collection<Joint<Body>> getJoints() {
    return List.of();
  }
}
