package it.units.erallab.mrsim2d.viewer.drawers.bodies;

import it.units.erallab.mrsim2d.core.bodies.RotationalJoint;
import it.units.erallab.mrsim2d.core.geometry.Point;
import it.units.erallab.mrsim2d.core.geometry.Poly;
import it.units.erallab.mrsim2d.viewer.DrawingUtils;
import it.units.erallab.mrsim2d.viewer.drawers.AbstractComponentDrawer;

import java.awt.*;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;
import java.awt.geom.Path2D;

public class RotationalJointDrawer extends AbstractComponentDrawer<RotationalJoint> {
  private final static Color FILL_COLOR = Color.DARK_GRAY;
  private final static Color STROKE_COLOR = Color.BLACK;
  private final static Color ANGLE_COLOR = Color.RED;
  private final static double JOINT_RADIUS_RATIO = 0.25;

  private final Color fillColor;
  private final Color strokeColor;
  private final Color anlgeColor;

  public RotationalJointDrawer(Color fillColor, Color strokeColor, Color anlgeColor) {
    super(RotationalJoint.class);
    this.fillColor = fillColor;
    this.strokeColor = strokeColor;
    this.anlgeColor = anlgeColor;
  }

  public RotationalJointDrawer() {
    this(FILL_COLOR, STROKE_COLOR, ANGLE_COLOR);
  }

  @Override
  protected boolean innerDraw(double t, RotationalJoint body, Graphics2D g) {
    Poly poly = body.poly();
    Path2D path = DrawingUtils.toPath(poly, true);
    g.setColor(fillColor);
    g.fill(path);
    g.setColor(strokeColor);
    g.draw(path);
    //draw circle
    g.setColor(STROKE_COLOR);
    double r = body.jointLength() * JOINT_RADIUS_RATIO;
    Point c = body.jointPoint();
    g.fill(new Ellipse2D.Double(
        c.x() - r, c.y() - r,
        2d * r, 2d * r
    ));
    //draw joint angle
    Point a = c.sum(new Point(body.angle() + body.jointAngle() - Math.PI / 2d).scale(r));
    g.setColor(anlgeColor);
    g.draw(new Line2D.Double(
        c.x(), c.y(),
        a.x(), a.y()
    ));
    return true;
  }

}
