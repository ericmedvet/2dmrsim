
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

  private final static Color ANCHOR_COLOR = Color.LIGHT_GRAY;
  private final static double ANCHOR_DOT_RADIUS = .05;
  private final static int SOFT_LINK_POINTS = 3;
  private final static double SOFT_LINK_WIDTH = .1;

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
      for (Anchor.Link link : anchor.links()) {
        switch (link.type()) {
          case RIGID -> g.draw(new Line2D.Double(
              anchor.point().x(), anchor.point().y(),
              link.destination().point().x(), link.destination().point().y()
          ));
          case SOFT -> g.draw(DrawingUtils.toPath(
              PolyUtils.zigZag(anchor.point(), link.destination().point(), SOFT_LINK_POINTS, SOFT_LINK_WIDTH).points()
          ));
        }
      }
    }
    return true;
  }

}
