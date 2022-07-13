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

import it.units.erallab.mrsim.viewer.drawers.AbstractComponentDrawer;
import org.dyn4j.collision.Fixture;
import org.dyn4j.dynamics.Body;
import org.dyn4j.dynamics.joint.Joint;
import org.dyn4j.geometry.Circle;
import org.dyn4j.geometry.Polygon;
import org.dyn4j.geometry.Transform;
import org.dyn4j.geometry.Vector2;

import java.awt.*;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;
import java.awt.geom.Path2D;
import java.util.Arrays;

/**
 * @author "Eric Medvet" on 2022/07/13 for 2dmrsim
 */
public class VoxelDrawer extends AbstractComponentDrawer<Voxel> {
  private final static Color COLOR = Color.BLACK;

  private final Color color;

  public VoxelDrawer(Color color) {
    super(Voxel.class);
    this.color = color;
  }

  public VoxelDrawer() {
    this(COLOR);
  }

  @Override
  protected boolean innerDraw(double t, Voxel voxel, Graphics2D g) {
    g.setColor(color);
    for (Body body : voxel.getBodies()) {
      Transform trans = body.getTransform();
      for (Fixture fixture : body.getFixtures()) {
        if (fixture.getShape() instanceof Polygon polygon) {
          Path2D path = new Path2D.Double();
          for (int i = 0; i < polygon.getVertices().length; i++) {
            Vector2 tV = polygon.getVertices()[i].copy();
            trans.transform(tV);
            if (i == 0) {
              path.moveTo(tV.x, tV.y);
            } else {
              path.lineTo(tV.x, tV.y);
            }
          }
          path.closePath();
          g.draw(path);
        } else if (fixture.getShape() instanceof Circle circle) {
          double r = circle.getRadius();
          Vector2 c = circle.getCenter().copy();
          trans.transform(c);
          g.draw(new Ellipse2D.Double(c.x - r / 2d, c.y - r / 2d, c.x + r / 2d, c.y + r / 2d));
        }
      }
    }
    for (Joint<Body> joint : voxel.getJoints()) {
      g.draw(new Line2D.Double(joint.getAnchor1().x, joint.getAnchor1().y, joint.getAnchor2().x, joint.getAnchor2().y));
    }
    return true;
  }
}
