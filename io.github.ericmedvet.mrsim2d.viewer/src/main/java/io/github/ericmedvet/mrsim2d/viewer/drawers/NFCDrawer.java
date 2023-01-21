package io.github.ericmedvet.mrsim2d.viewer.drawers;

import io.github.ericmedvet.mrsim2d.core.NFCMessage;
import io.github.ericmedvet.mrsim2d.core.Snapshot;
import io.github.ericmedvet.mrsim2d.core.geometry.Point;
import io.github.ericmedvet.mrsim2d.viewer.Drawer;
import io.github.ericmedvet.mrsim2d.viewer.DrawingUtils;

import java.awt.*;
import java.util.List;

/**
 * @author "Eric Medvet" on 2023/01/21 for 2dmrsim
 */
public class NFCDrawer implements Drawer {
  private final static Color[] COLORS = new Color[]{
      Color.PINK,
      Color.CYAN,
      Color.YELLOW,
      Color.MAGENTA,
      Color.ORANGE
  };
  private final static double MAX_LENGTH = 1d;
  private final static double ARROW_LENGTH = 0.15d;
  private final static double ARROW_ANGLE = Math.PI / 6d;

  private final Color[] colors;
  private final double maxLenght;

  public NFCDrawer(Color[] colors, double maxLenght) {
    this.colors = colors;
    this.maxLenght = maxLenght;
  }

  public NFCDrawer() {
    this(COLORS, MAX_LENGTH);
  }

  @Override
  public boolean draw(List<Snapshot> snapshots, Graphics2D g) {
    for (NFCMessage message : snapshots.get(snapshots.size() - 1).nfcMessages()) {
      g.setColor(colors[message.channel() % colors.length]);
      Point src = message.source();
      Point dst = src.sum(new Point(message.direction()).scale(maxLenght * Math.abs(message.value())));
      DrawingUtils.drawLine(g, src, dst);
      Point mid = Point.average(src, dst);
      double dDirection = message.value() > 0d ? Math.PI : 0d;
      Point arrowEnd1 = mid.sum(new Point(message.direction() + ARROW_ANGLE + dDirection).scale(ARROW_LENGTH));
      Point arrowEnd2 = mid.sum(new Point(message.direction() - ARROW_ANGLE + dDirection).scale(ARROW_LENGTH));
      DrawingUtils.drawLine(g, mid, arrowEnd1);
      DrawingUtils.drawLine(g, mid, arrowEnd2);
    }
    return true;
  }
}
