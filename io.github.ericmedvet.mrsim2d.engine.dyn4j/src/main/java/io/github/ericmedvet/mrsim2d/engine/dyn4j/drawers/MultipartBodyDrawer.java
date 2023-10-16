
package io.github.ericmedvet.mrsim2d.engine.dyn4j.drawers;

import io.github.ericmedvet.mrsim2d.core.bodies.Body;
import io.github.ericmedvet.mrsim2d.engine.dyn4j.MultipartBody;
import io.github.ericmedvet.mrsim2d.viewer.DrawingUtils;
import io.github.ericmedvet.mrsim2d.viewer.drawers.AbstractComponentDrawer;
import org.dyn4j.collision.Fixture;
import org.dyn4j.geometry.Circle;
import org.dyn4j.geometry.Polygon;
import org.dyn4j.geometry.Transform;
import org.dyn4j.geometry.Vector2;

import java.awt.*;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Path2D;
public class MultipartBodyDrawer extends AbstractComponentDrawer<Body> {
  private final static Color BORDER_COLOR = Color.RED;
  private final static double VERTEX_DOT_RADIUS = .05;

  private final Color drawColor;
  private final Color fillColor;

  public MultipartBodyDrawer(Color color) {
    super(Body.class);
    this.drawColor = color;
    this.fillColor = DrawingUtils.alphaed(color, 0.1f);
  }

  public MultipartBodyDrawer() {
    this(BORDER_COLOR);
  }

  @Override
  protected boolean innerDraw(double t, Body b, Graphics2D g) {
    if (b instanceof MultipartBody multipartBody) {
      for (org.dyn4j.dynamics.Body body : multipartBody.getBodies()) {
        Transform trans = body.getTransform();
        for (Fixture fixture : body.getFixtures()) {
          if (fixture.getShape() instanceof Polygon polygon) {
            g.setColor(drawColor);
            Path2D path = new Path2D.Double();
            for (int i = 0; i < polygon.getVertices().length; i++) {
              Vector2 tV = polygon.getVertices()[i].copy();
              trans.transform(tV);
              g.fill(new Ellipse2D.Double(
                  tV.x - VERTEX_DOT_RADIUS,
                  tV.y - VERTEX_DOT_RADIUS,
                  VERTEX_DOT_RADIUS * 2d,
                  VERTEX_DOT_RADIUS * 2d
              ));
              if (i == 0) {
                path.moveTo(tV.x, tV.y);
              } else {
                path.lineTo(tV.x, tV.y);
              }
            }
            path.closePath();
            g.setColor(fillColor);
            g.fill(path);
            g.setColor(drawColor);
            g.draw(path);
          } else if (fixture.getShape() instanceof Circle circle) {
            double r = circle.getRadius();
            Vector2 c = circle.getCenter().copy();
            trans.transform(c);
            Shape s = new Ellipse2D.Double(c.x - r, c.y - r, 2d * r, 2d * r);
            g.setColor(fillColor);
            g.fill(s);
            g.setColor(drawColor);
            g.draw(s);
          }
        }
      }
      g.setColor(drawColor);
      return true;
    }
    return false;
  }
}
