package io.github.ericmedvet.mrsim2d.viewer.drawers;

import io.github.ericmedvet.jsdynsym.core.DoubleRange;
import io.github.ericmedvet.mrsim2d.core.Agent;
import io.github.ericmedvet.mrsim2d.core.NumMultiBrained;
import io.github.ericmedvet.mrsim2d.core.Snapshot;
import io.github.ericmedvet.mrsim2d.core.geometry.BoundingBox;
import io.github.ericmedvet.mrsim2d.viewer.Drawer;
import io.github.ericmedvet.mrsim2d.viewer.DrawingUtils;

import java.awt.*;
import java.awt.geom.Rectangle2D;
import java.util.List;

public class NumMultiBrainedIODrawer implements Drawer {

  private final static double BRAIN_MARGIN = 2d;
  private final static double ITEM_MARGIN = 1d;
  private final static int MAX_N_OF_ROWS = 3;

  @Override
  public boolean draw(List<Snapshot> snapshots, Graphics2D g) {
    //get first agent of last snapshot
    Snapshot lastSnapshot = snapshots.get(snapshots.size() - 1);
    if (lastSnapshot.agents().isEmpty()) {
      return false;
    }
    Agent agent = lastSnapshot.agents().iterator().next();
    if (agent instanceof NumMultiBrained brained) {
      BoundingBox gBB = DrawingUtils.getBoundingBox(g);
      int nOfBrains = brained.brainIOs().size();
      double iMargin = ITEM_MARGIN;
      double bMargin = BRAIN_MARGIN;
      double bestL = 0d;
      int bestNOfRows = 1;
      for (int nOfRows = 1; nOfRows <= MAX_N_OF_ROWS; nOfRows = nOfRows + 1) {
        int localNOfRows = nOfRows;
        int allRowLength = brained.brainIOs().stream()
            .mapToInt(io -> Math.max(
                (int) Math.ceil((double)io.input().values().length / (double)localNOfRows),
                (int) Math.ceil((double)io.output().values().length / (double)localNOfRows)
            ))
            .sum();
        double itemMaxW = (gBB.width()
            - (double) (nOfBrains - 1) * bMargin
            - (double) (allRowLength - nOfBrains) * iMargin)
            / (double) allRowLength;
        double itemMaxH = (gBB.height()
            - bMargin - 2d * (double) (nOfRows - 1) * iMargin)
            / 2d / (double) nOfRows;
        double l = Math.min(itemMaxW, itemMaxH);
        if (l > bestL) {
          bestNOfRows = nOfRows;
          bestL = l;
        }
      }
      double l = bestL;
      int nOfRows = bestNOfRows;
      double xb = gBB.min().x();
      for (NumMultiBrained.BrainIO brainIO : brained.brainIOs()) {
        int rowLength = Math.max(brainIO.input().values().length, brainIO.output().values().length) / nOfRows;
        double x = xb;
        double y = gBB.min().y();
        int c = 0;
        double fix = x;
        double fox = x;
        //inputs
        DoubleRange iRange = brainIO.input().range();
        for (double value : brainIO.input().values()) {
          g.setColor(DrawingUtils.linear(
              DrawingUtils.Colors.DATA_NEGATIVE,
              DrawingUtils.Colors.DATA_POSITIVE,
              (float) iRange.min(),
              (float) iRange.max(),
              (float) value
          ));
          g.fill(new Rectangle2D.Double(x, y, l, l));
          fix = Math.max(fix, x + l);
          c = c + 1;
          if (c > rowLength) {
            x = xb;
            y = y + l + iMargin;
            c = 0;
          } else {
            x = x + l + iMargin;
          }
        }
        //outputs
        x = xb;
        y = gBB.min().y() + (double) nOfRows * l + (double) (nOfRows - 1) * iMargin + bMargin;
        c = 0;
        DoubleRange oRange = brainIO.output().range();
        for (double value : brainIO.output().values()) {
          g.setColor(DrawingUtils.linear(
              DrawingUtils.Colors.DATA_NEGATIVE,
              DrawingUtils.Colors.DATA_POSITIVE,
              (float) oRange.min(),
              (float) oRange.max(),
              (float) value
          ));
          g.fill(new Rectangle2D.Double(x, y, l, l));
          fox = Math.max(fox, x + l);
          c = c + 1;
          if (c > rowLength) {
            x = xb;
            y = y + l + iMargin;
            c = 0;
          } else {
            x = x + l + iMargin;
          }
        }
        //set next starting point
        xb = Math.max(fix, fox) + bMargin;
      }
      return true;
    }
    return false;
  }
}
