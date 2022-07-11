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

package it.units.erallab.mrsim.viewer.drawers.bodies;

import it.units.erallab.mrsim.core.bodies.Anchor;
import it.units.erallab.mrsim.core.bodies.Anchorable;
import it.units.erallab.mrsim.core.geometry.Point;
import it.units.erallab.mrsim.viewer.drawers.AbstractComponentDrawer;

import java.awt.*;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;

/**
 * @author "Eric Medvet" on 2022/07/10 for 2dmrsim
 */
public class AnchorableBodyDrawer extends AbstractComponentDrawer<Anchorable> {

  private final static Color ANCHOR_COLOR = Color.GRAY;
  private final static double ANCHOR_DOT_RADIUS = .05;

  private final Color anchorColor;

  public AnchorableBodyDrawer(Color anchorColor) {
    super(Anchorable.class);
    this.anchorColor = anchorColor;
  }

  public AnchorableBodyDrawer() {
    this(ANCHOR_COLOR);
  }

  @Override
  protected boolean innerDraw(double t, Anchorable anchorable, Graphics2D g) {
    g.setColor(anchorColor);
    for (Anchor anchor : anchorable.anchors()) {
      g.fill(new Ellipse2D.Double(
          anchor.point().x() - ANCHOR_DOT_RADIUS,
          anchor.point().y() - ANCHOR_DOT_RADIUS,
          ANCHOR_DOT_RADIUS * 2d,
          ANCHOR_DOT_RADIUS * 2d
      ));
      for (Anchor dstAnchor : anchor.attachedAnchors()) {
        it.units.erallab.mrsim.core.geometry.Point midPoint = Point.average(anchor.point(), dstAnchor.point());
        g.draw(new Line2D.Double(
            anchor.point().x(), anchor.point().y(),
            midPoint.x(), midPoint.y()
        ));
      }
    }
    return !anchorable.anchors().isEmpty();
  }

}
