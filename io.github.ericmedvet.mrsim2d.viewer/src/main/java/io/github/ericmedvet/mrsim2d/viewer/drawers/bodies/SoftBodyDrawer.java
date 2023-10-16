
package io.github.ericmedvet.mrsim2d.viewer.drawers.bodies;

import io.github.ericmedvet.jsdynsym.core.DoubleRange;
import io.github.ericmedvet.mrsim2d.core.bodies.SoftBody;
import io.github.ericmedvet.mrsim2d.core.geometry.Point;
import io.github.ericmedvet.mrsim2d.core.geometry.Poly;
import io.github.ericmedvet.mrsim2d.viewer.DrawingUtils;
import io.github.ericmedvet.mrsim2d.viewer.drawers.AbstractComponentDrawer;

import java.awt.*;
import java.awt.geom.Line2D;
import java.awt.geom.Path2D;
public class SoftBodyDrawer extends AbstractComponentDrawer<SoftBody> {

  private final static Color CONTRACTED_COLOR = new Color(252, 141, 89);
  private final static Color REST_COLOR = new Color(255, 255, 191);
  private final static Color EXPANDED_COLOR = new Color(145, 191, 219);
  private final static Color BORDER_COLOR = Color.BLACK;
  private final static DoubleRange AREA_RATIO_RANGE = new DoubleRange(0.75, 1.25);

  private final Color contractedColor;
  private final Color restColor;
  private final Color expandedColor;
  private final Color borderColor;
  private final DoubleRange areaRatioRange;

  public SoftBodyDrawer(
      Color contractedColor,
      Color restColor,
      Color expandedColor,
      Color borderColor,
      DoubleRange areaRatioRange
  ) {
    super(SoftBody.class);
    this.contractedColor = contractedColor;
    this.restColor = restColor;
    this.expandedColor = expandedColor;
    this.borderColor = borderColor;
    this.areaRatioRange = areaRatioRange;
  }

  public SoftBodyDrawer() {
    this(CONTRACTED_COLOR, REST_COLOR, EXPANDED_COLOR, BORDER_COLOR, AREA_RATIO_RANGE);
  }

  @Override
  protected boolean innerDraw(double t, SoftBody body, Graphics2D g) {
    Poly poly = body.poly();
    Path2D path = DrawingUtils.toPath(poly, true);
    g.setColor(DrawingUtils.linear(
        contractedColor,
        restColor,
        expandedColor,
        (float) areaRatioRange.min(),
        1,
        (float) areaRatioRange.max(),
        (float) body.areaRatio()
    ));
    g.fill(path);
    g.setColor(borderColor);
    g.draw(path);
    //angle line
    Point center = poly.center();
    Point firstSideMeanPoint = Point.average(poly.vertexes()[0], poly.vertexes()[1]);
    g.draw(new Line2D.Double(center.x(), center.y(), firstSideMeanPoint.x(), firstSideMeanPoint.y()));
    return true;
  }
}
