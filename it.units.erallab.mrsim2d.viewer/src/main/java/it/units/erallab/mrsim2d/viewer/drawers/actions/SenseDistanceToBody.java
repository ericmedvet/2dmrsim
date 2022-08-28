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

package it.units.erallab.mrsim2d.viewer.drawers.actions;

import it.units.erallab.mrsim2d.core.ActionOutcome;
import it.units.erallab.mrsim2d.core.geometry.Point;

import java.awt.*;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;

/**
 * @author "Eric Medvet" on 2022/07/17 for 2dmrsim
 */
public class SenseDistanceToBody extends AbstractActionComponentDrawer<it.units.erallab.mrsim2d.core.actions.SenseDistanceToBody,
    Double> {

  private final static Color COLOR = Color.RED;

  private final static double RADIUS = 0.1;

  private final Color color;

  public SenseDistanceToBody(Color color) {
    super(it.units.erallab.mrsim2d.core.actions.SenseDistanceToBody.class);
    this.color = color;
  }

  public SenseDistanceToBody() {
    this(COLOR);
  }

  @Override
  protected boolean innerDraw(
      double t,
      ActionOutcome<it.units.erallab.mrsim2d.core.actions.SenseDistanceToBody, Double> ao,
      Graphics2D g
  ) {
    //draw line
    g.setColor(color);
    Point src = ao.action().body().poly().center();
    Point dst = src
        .sum(new Point(ao.action().direction() + ao.action().body().angle())
            .scale(ao.action().distanceRange()));
    g.draw(new Line2D.Double(src.x(), src.y(), dst.x(), dst.y()));
    //draw circle
    Point target = src
        .sum(new Point(ao.action().direction() + ao.action().body().angle())
            .scale(ao.outcome().orElse(ao.action().distanceRange())));
    g.draw(new Ellipse2D.Double(target.x() - RADIUS / 2d, target.y() - RADIUS / 2d, RADIUS, RADIUS));
    return true;
  }
}
