/*-
 * ========================LICENSE_START=================================
 * mrsim2d-viewer
 * %%
 * Copyright (C) 2020 - 2025 Eric Medvet
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * =========================LICENSE_END==================================
 */

package io.github.ericmedvet.mrsim2d.viewer.drawers.actions;

import io.github.ericmedvet.jnb.datastructure.DoubleRange;
import io.github.ericmedvet.mrsim2d.core.ActionOutcome;
import io.github.ericmedvet.mrsim2d.viewer.DrawingUtils;
import java.awt.*;
import java.awt.geom.Line2D;
import java.awt.geom.Path2D;

public class AttractAnchor extends AbstractActionComponentDrawer<io.github.ericmedvet.mrsim2d.core.actions.AttractAnchor, Double> {

  private static final Color COLOR = DrawingUtils.alphaed(Color.GREEN, 0.25f);

  private static final DoubleRange RADIUS = new DoubleRange(0.1, 0.5);
  private static final double ANGLE = Math.PI / 6d;
  private static final double ATTRACTED_LENGTH_RATIO = 0.5;

  private final Color color;

  public AttractAnchor(Color color) {
    super(io.github.ericmedvet.mrsim2d.core.actions.AttractAnchor.class);
    this.color = color;
  }

  public AttractAnchor() {
    this(COLOR);
  }

  @Override
  protected boolean innerDraw(
      double t,
      ActionOutcome<io.github.ericmedvet.mrsim2d.core.actions.AttractAnchor, Double> o,
      Graphics2D g
  ) {
    if (o.outcome().isEmpty()) {
      return true;
    }
    double magnitude = o.outcome().get();
    if (magnitude == 0) {
      return true;
    }
    double a = o.action()
        .destination()
        .point()
        .diff(o.action().source().point())
        .direction();
    double sX = o.action().source().point().x();
    double sY = o.action().source().point().y();
    double dX = o.action().destination().point().x();
    double dY = o.action().destination().point().y();
    double l = RADIUS.denormalize(magnitude);
    Path2D triangle = new Path2D.Double();
    triangle.moveTo(sX, sY);
    triangle.lineTo(sX + Math.cos(a - ANGLE / 2d) * l, sY + Math.sin(a - ANGLE / 2d) * l);
    triangle.lineTo(sX + Math.cos(a + ANGLE / 2d) * l, sY + Math.sin(a + ANGLE / 2d) * l);
    triangle.closePath();
    g.setColor(color);
    g.fill(triangle);
    g.draw(
        new Line2D.Double(
            dX,
            dY,
            dX - Math.cos(a) * l * ATTRACTED_LENGTH_RATIO,
            dY - Math.sin(a) * l * ATTRACTED_LENGTH_RATIO
        )
    );
    return true;
  }
}
