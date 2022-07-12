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
import it.units.erallab.mrsim.core.bodies.Anchorable;
import it.units.erallab.mrsim.core.geometry.Point;
import it.units.erallab.mrsim.util.Pair;
import org.dyn4j.dynamics.Body;
import org.dyn4j.dynamics.joint.Joint;
import org.dyn4j.geometry.Vector2;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @author "Eric Medvet" on 2022/07/08 for 2dmrsim
 */
public class BodyAnchor implements Anchor {

  private final Body body;
  private final Anchorable anchorable;
  private final Map<Link, Joint<Body>> jointMap;

  public BodyAnchor(Body body, Anchorable anchorable) {
    this.body = body;
    this.anchorable = anchorable;
    jointMap = new HashMap<>();
  }

  @Override
  public Point point() {
    Vector2 center = body.getWorldCenter();
    return new Point(center.x, center.y);
  }

  @Override
  public Anchorable anchorable() {
    return anchorable;
  }


  @Override
  public Collection<Link> links() {
    return jointMap.keySet();
  }

  protected Body getBody() {
    return body;
  }

  protected Map<Link, Joint<Body>> getJointMap() {
    return jointMap;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o)
      return true;
    if (o == null || getClass() != o.getClass())
      return false;
    BodyAnchor that = (BodyAnchor) o;
    return body.equals(that.body);
  }

  @Override
  public int hashCode() {
    return Objects.hash(body);
  }
}
