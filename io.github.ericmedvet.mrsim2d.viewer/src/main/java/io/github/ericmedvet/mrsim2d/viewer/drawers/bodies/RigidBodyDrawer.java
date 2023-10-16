
package io.github.ericmedvet.mrsim2d.viewer.drawers.bodies;

import io.github.ericmedvet.mrsim2d.core.bodies.RigidBody;
import io.github.ericmedvet.mrsim2d.core.geometry.Point;
import io.github.ericmedvet.mrsim2d.core.geometry.Poly;
import io.github.ericmedvet.mrsim2d.viewer.DrawingUtils;
import io.github.ericmedvet.mrsim2d.viewer.drawers.AbstractComponentDrawer;

import java.awt.*;
import java.awt.geom.Line2D;
import java.awt.geom.Path2D;
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
