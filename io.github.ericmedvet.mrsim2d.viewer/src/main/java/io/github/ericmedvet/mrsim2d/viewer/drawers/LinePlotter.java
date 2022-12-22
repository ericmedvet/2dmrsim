package io.github.ericmedvet.mrsim2d.viewer.drawers;

import io.github.ericmedvet.mrsim2d.core.Snapshot;
import io.github.ericmedvet.mrsim2d.core.geometry.BoundingBox;
import io.github.ericmedvet.mrsim2d.core.geometry.Point;
import io.github.ericmedvet.mrsim2d.core.util.DoubleRange;
import io.github.ericmedvet.mrsim2d.viewer.AbstractMemoryDrawer;
import io.github.ericmedvet.mrsim2d.viewer.DrawingUtils;

import java.awt.*;
import java.util.SortedMap;
import java.util.function.Function;

/**
 * @author "Eric Medvet" on 2022/10/07 for 2dmrsim
 */
public class LinePlotter extends AbstractMemoryDrawer<Double> {
  private final String format;

  public LinePlotter(
      Function<Snapshot, Double> extractor,
      double windowT,
      String format
  ) {
    super(extractor, windowT, WindowType.SNAPSHOT_TIME);
    this.format = format;
  }

  @Override
  protected boolean innerDraw(SortedMap<Double, Double> memory, Graphics2D g) {
    //find ranges
    DoubleRange tRange = new DoubleRange(
        memory.lastKey() - getWindowT(),
        memory.lastKey()
    );
    DoubleRange vRange = new DoubleRange(
        Math.min(0, memory.values().stream().mapToDouble(d -> d).min().orElse(0d)),
        Math.max(0, memory.values().stream().mapToDouble(d -> d).max().orElse(0d))
    );
    BoundingBox gBB = DrawingUtils.getBoundingBox(g);
    //prepare points
    Point[] drawPoints = memory.entrySet().stream()
        .map(e -> new Point(
            gBB.xRange().denormalize(tRange.normalize(e.getKey())),
            gBB.yRange().denormalize(1 - vRange.normalize(e.getValue()))
        ))
        .toArray(Point[]::new);
    Point[] fillPoints = new Point[drawPoints.length + 2];
    fillPoints[0] = new Point(drawPoints[0].x(), gBB.max().y());
    fillPoints[fillPoints.length - 1] = new Point(gBB.max().x(), gBB.max().y());
    System.arraycopy(drawPoints, 0, fillPoints, 1, drawPoints.length);
    //draw
    g.setColor(DrawingUtils.Colors.DATA);
    g.draw(DrawingUtils.toPath(drawPoints));
    g.setColor(DrawingUtils.alphaed(DrawingUtils.Colors.DATA, 0.5f));
    g.fill(DrawingUtils.toPath(fillPoints));
    if (format != null) {
      g.setFont(DrawingUtils.FONT);
      String s = format.formatted(memory.get(memory.lastKey()));
      double sW = g.getFontMetrics().stringWidth(s);
      g.setColor(DrawingUtils.Colors.TEXT);
      g.drawString(
          s,
          (float) (gBB.center().x() - sW / 2d),
          (float) (gBB.center().y() + g.getFontMetrics().getHeight() / 2d)
      );
    }
    return true;
  }

}
