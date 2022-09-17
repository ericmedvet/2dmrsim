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

package it.units.erallab.mrsim2d.viewer.drawers.bodies;

import it.units.erallab.mrsim2d.core.bodies.RigidBody;
import it.units.erallab.mrsim2d.core.geometry.Point;
import it.units.erallab.mrsim2d.core.geometry.Poly;
import it.units.erallab.mrsim2d.viewer.DrawingUtils;
import it.units.erallab.mrsim2d.viewer.drawers.AbstractComponentDrawer;

import java.awt.*;
import java.awt.geom.Line2D;
import java.awt.geom.Path2D;

/**
 * @author "Eric Medvet" on 2022/07/07 for 2dmrsim
 */
public class RigidBodyDrawer extends AbstractComponentDrawer<RigidBody> {

  private final static Color FILL_COLOR = Color.GRAY;
  private final static Color STROKE_COLOR = Color.BLACK;

  private final Color fillColor;
  private final Color strokeColor;

  public RigidBodyDrawer(Color fillColor, Color strokeColor) {
    super(RigidBody.class);
    this.fillColor = fillColor;
    this.strokeColor = strokeColor;
  }

  public RigidBodyDrawer() {
    this(FILL_COLOR, STROKE_COLOR);
  }

  @Override
  protected boolean innerDraw(double t, RigidBody body, Graphics2D g) {
    Poly poly = body.poly();
    Path2D path = DrawingUtils.toPath(poly,true);
    g.setColor(fillColor);
    g.fill(path);
    g.setColor(strokeColor);
    g.draw(path);
    //plot angle
    Point center = poly.center();
    Point firstSideMeanPoint = poly.sides().get(0).center();
    g.draw(new Line2D.Double(center.x(), center.y(), firstSideMeanPoint.x(), firstSideMeanPoint.y()));
    return true;
  }
}
