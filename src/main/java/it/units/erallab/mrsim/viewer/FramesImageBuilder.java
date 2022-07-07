/*
 * Copyright 2022 Eric Medvet <eric.medvet@gmail.com> (as eric)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package it.units.erallab.mrsim.viewer;

import it.units.erallab.mrsim.core.Snapshot;
import it.units.erallab.mrsim.core.geometry.BoundingBox;
import it.units.erallab.mrsim.core.geometry.Point;
import it.units.erallab.mrsim.util.Accumulator;

import java.awt.*;
import java.awt.image.BufferedImage;

/**
 * @author "Eric Medvet" on 2022/07/07 for 2dmrsim
 */
public class FramesImageBuilder implements Accumulator<BufferedImage, Snapshot> {
  public enum Direction {
    HORIZONTAL, VERTICAL
  }

  private final int nOfFrames;
  private final double deltaT;
  private final Direction direction;
  private final Drawer drawer;
  private final BufferedImage image;
  private int frameCount;
  private double latestDrawnT;

  public FramesImageBuilder(int frameW, int frameH, int nOfFrames, double deltaT, Direction direction, Drawer drawer) {
    this.nOfFrames = nOfFrames;
    this.deltaT = deltaT;
    this.direction = direction;
    this.drawer = drawer;
    frameCount = 0;
    latestDrawnT = 0d;
    //prepare image
    int overallW = frameW;
    int overallH = frameH;
    if (direction.equals(Direction.HORIZONTAL)) {
      overallW = frameW * nOfFrames;
    } else {
      overallH = frameH * nOfFrames;
    }
    image = new BufferedImage(overallW, overallH, BufferedImage.TYPE_3BYTE_BGR);
  }

  @Override
  public BufferedImage get() {
    return image;
  }

  @Override
  public void accept(Snapshot snapshot) {
    if (snapshot.t() < latestDrawnT + deltaT || frameCount > nOfFrames) {
      return;
    }
    latestDrawnT = snapshot.t();
    frameCount = frameCount + 1;
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
    //draw
    Graphics2D g = image.createGraphics();
    g.setClip(0, 0, image.getWidth(), image.getHeight());
    Drawer.clip(imageFrame, drawer).draw(snapshot, g);
    g.dispose();
  }
}
