package io.github.ericmedvet.mrsim2d.viewer.drawers.bodies;

import io.github.ericmedvet.mrsim2d.core.bodies.RotationalJoint;
import io.github.ericmedvet.mrsim2d.core.geometry.Point;
import io.github.ericmedvet.mrsim2d.core.geometry.Poly;
import io.github.ericmedvet.mrsim2d.viewer.DrawingUtils;
import io.github.ericmedvet.mrsim2d.viewer.drawers.AbstractComponentDrawer;

import java.awt.*;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;
import java.awt.geom.Path2D;

public class RotationalJointDrawer extends AbstractComponentDrawer<RotationalJoint> {
  private final static Color FILL_COLOR = Color.DARK_GRAY;
  private final static Color STROKE_COLOR = Color.BLACK;
  private final static Color ANGLE_CURRENT_COLOR = Color.RED;
  private final static Color ANGLE_TARGET_COLOR = Color.PINK;
  private final static double JOINT_RADIUS_RATIO = 0.25;

  private final Color fillColor;
  private final Color strokeColor;
  private final Color angleCurrentColor;
  private final Color angleTargetColor;

  public RotationalJointDrawer(Color fillColor, Color strokeColor, Color angleCurrentColor, Color TargetColor) {
    super(RotationalJoint.class);
    this.fillColor = fillColor;
    this.strokeColor = strokeColor;
    this.angleCurrentColor = angleCurrentColor;
    this.angleTargetColor = TargetColor;
  }

  public RotationalJointDrawer() {
    this(FILL_COLOR, STROKE_COLOR, ANGLE_CURRENT_COLOR, ANGLE_TARGET_COLOR);
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
    Point ca1 = c.sum(new Point(body.angle() - body.jointAngle() / 2d).scale(r));
    Point ca2 = c.sum(new Point(body.angle() + Math.PI + body.jointAngle() / 2d).scale(r));
    Point ta1 = c.sum(new Point(body.angle() - body.jointTargetAngle() / 2d).scale(r));
    Point ta2 = c.sum(new Point(body.angle() + Math.PI + body.jointTargetAngle() / 2d).scale(r));
    g.setColor(angleCurrentColor);
    g.draw(new Line2D.Double(c.x(), c.y(), ca1.x(), ca1.y()));
    g.draw(new Line2D.Double(c.x(), c.y(), ca2.x(), ca2.y()));
    g.setColor(angleTargetColor);
    g.draw(new Line2D.Double(c.x(), c.y(), ta1.x(), ta1.y()));
    g.draw(new Line2D.Double(c.x(), c.y(), ta2.x(), ta2.y()));
    return true;
  }

}
