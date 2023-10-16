/*-
 * ========================LICENSE_START=================================
 * mrsim2d-viewer
 * %%
 * Copyright (C) 2020 - 2023 Eric Medvet
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

package io.github.ericmedvet.mrsim2d.viewer.drawers.bodies;

import io.github.ericmedvet.mrsim2d.core.bodies.Anchor;
import io.github.ericmedvet.mrsim2d.core.bodies.Anchorable;
import io.github.ericmedvet.mrsim2d.core.util.PolyUtils;
import io.github.ericmedvet.mrsim2d.viewer.DrawingUtils;
import io.github.ericmedvet.mrsim2d.viewer.drawers.AbstractComponentDrawer;
import java.awt.*;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;

public class AnchorableBodyDrawer extends AbstractComponentDrawer<Anchorable> {

  private static final Color ANCHOR_COLOR = Color.LIGHT_GRAY;
  private static final double ANCHOR_DOT_RADIUS = .05;
  private static final int SOFT_LINK_POINTS = 3;
  private static final double SOFT_LINK_WIDTH = .1;

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
      g.fill(
          new Ellipse2D.Double(
              anchor.point().x() - ANCHOR_DOT_RADIUS,
              anchor.point().y() - ANCHOR_DOT_RADIUS,
              ANCHOR_DOT_RADIUS * 2d,
              ANCHOR_DOT_RADIUS * 2d));
      for (Anchor.Link link : anchor.links()) {
        switch (link.type()) {
          case RIGID -> g.draw(
              new Line2D.Double(
                  anchor.point().x(), anchor.point().y(),
                  link.destination().point().x(), link.destination().point().y()));
          case SOFT -> g.draw(
              DrawingUtils.toPath(
                  PolyUtils.zigZag(
                          anchor.point(),
                          link.destination().point(),
                          SOFT_LINK_POINTS,
                          SOFT_LINK_WIDTH)
                      .points()));
        }
      }
    }
    return true;
  }
}
