
package io.github.ericmedvet.mrsim2d.viewer;

import io.github.ericmedvet.mrsim2d.core.Snapshot;
import io.github.ericmedvet.mrsim2d.core.geometry.BoundingBox;
import io.github.ericmedvet.mrsim2d.core.geometry.Point;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;
public class FramesImageBuilder implements Accumulator<BufferedImage, Snapshot> {
  private final int nOfFrames;
  private final double deltaT;
  private final Direction direction;
  private final Drawer drawer;
  private final BufferedImage image;
  private final List<Snapshot> snapshots;
  private final boolean justLastSnapshot;
  private int frameCount;
  private double lastDrawnT;

  public FramesImageBuilder(
      int frameW,
      int frameH,
      int nOfFrames,
      double deltaT,
      double startTime,
      Direction direction,
      boolean justLastSnapshot,
      Drawer drawer
  ) {
    this.nOfFrames = nOfFrames;
    this.deltaT = deltaT;
    this.direction = direction;
    this.drawer = drawer;
    this.justLastSnapshot = justLastSnapshot;
    frameCount = 0;
    lastDrawnT = startTime;
    //prepare image
    int overallW = frameW;
    int overallH = frameH;
    if (direction.equals(Direction.HORIZONTAL)) {
      overallW = frameW * nOfFrames;
    } else {
      overallH = frameH * nOfFrames;
    }
    image = new BufferedImage(overallW, overallH, BufferedImage.TYPE_3BYTE_BGR);
    snapshots = new ArrayList<>();
  }

  public enum Direction {
    HORIZONTAL, VERTICAL
  }

  @Override
  public void accept(Snapshot snapshot) {
    if (frameCount > nOfFrames) {
      return;
    }
    snapshots.add(snapshot);
    if (snapshot.t() >= lastDrawnT + deltaT) {
      lastDrawnT = snapshot.t();
      //frame
      BoundingBox imageFrame;
      if (direction.equals(Direction.HORIZONTAL)) {
        imageFrame = new BoundingBox(
            new Point((double) frameCount / (double) nOfFrames, 0),
            new Point((double) (frameCount + 1) / (double) nOfFrames, 1d)
        );
      } else {
        imageFrame = new BoundingBox(
            new Point(0d, (double) frameCount / (double) nOfFrames),
            new Point(1d, (double) (frameCount + 1) / (double) nOfFrames)
        );
      }
      frameCount = frameCount + 1;
      //draw
      Graphics2D g = image.createGraphics();
      g.setClip(0, 0, image.getWidth(), image.getHeight());
      Drawer.clip(imageFrame, drawer).draw(
          justLastSnapshot ? snapshots.subList(snapshots.size() - 1, snapshots.size()) : snapshots,
          g
      );
      g.dispose();
      snapshots.clear();
    }
  }

  @Override
  public BufferedImage get() {
    return image;
  }
}
