
package io.github.ericmedvet.mrsim2d.viewer.drawers.actions;

import io.github.ericmedvet.mrsim2d.core.ActionOutcome;
import io.github.ericmedvet.mrsim2d.core.geometry.Point;
import io.github.ericmedvet.mrsim2d.viewer.DrawingUtils;

import java.awt.*;
public class SenseVelocity extends AbstractActionComponentDrawer<io.github.ericmedvet.mrsim2d.core.actions.SenseVelocity,
    Double> {

  private final static Color COLOR = Color.GREEN;
  private final static double MULT = 1d;

  private final Color color;

  public SenseVelocity(Color color) {
    super(io.github.ericmedvet.mrsim2d.core.actions.SenseVelocity.class);
    this.color = color;
  }

  public SenseVelocity() {
    this(COLOR);
  }

  @Override
  protected boolean innerDraw(
      double t,
      ActionOutcome<io.github.ericmedvet.mrsim2d.core.actions.SenseVelocity, Double> ao,
      Graphics2D g
  ) {
    //draw line
    g.setColor(color);
    Point src = ao.action().body().poly().center();
    Point dst = src
        .sum(new Point(ao.action().direction())
            .scale(ao.outcome().orElse(0d) * MULT));
    DrawingUtils.drawLine(g, src, dst);
    return true;
  }
}
