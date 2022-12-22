/*
 * Copyright 2022 eric
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

package it.units.erallab.mrsim2d.viewer;

import it.units.erallab.mrsim2d.core.Snapshot;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

/**
 * @author "Eric Medvet" on 2022/07/07 for 2dmrsim
 */
public class VideoBuilder implements Accumulator<File, Snapshot> {

  private static final Logger L = Logger.getLogger(VideoBuilder.class.getName());

  private final int w;
  private final int h;
  private final double startTime;
  private final double endTime;
  private final double frameRate;
  private final VideoUtils.EncoderFacility encoder;
  private final File file;
  private final Drawer drawer;
  private final List<BufferedImage> images;
  private final List<Snapshot> snapshots;
  private final List<Double> snapshotTs;
  private double lastDrawnT;
  private double lastT;

  public VideoBuilder(
      int w,
      int h,
      double startTime,
      double endTime,
      double frameRate,
      VideoUtils.EncoderFacility encoder,
      File file,
      Drawer drawer
  ) {
    this.w = w;
    this.h = h;
    this.startTime = startTime;
    this.endTime = endTime;
    this.frameRate = frameRate;
    this.encoder = encoder;
    this.file = file;
    this.drawer = drawer;
    images = new ArrayList<>((int) Math.ceil((endTime - startTime) * frameRate));
    snapshots = new ArrayList<>();
    snapshotTs = new ArrayList<>();
    lastT = Double.NaN;
  }

  @Override
  public void accept(Snapshot snapshot) {
    if (!Double.isNaN(lastT)) {
      snapshotTs.add(snapshot.t() - lastT);
    }
    lastT = snapshot.t();
    if (snapshot.t() < startTime || snapshot.t() > endTime) {
      return;
    }
    double tolerance = snapshotTs.stream().mapToDouble(t -> t).average().orElse(0d) / 2d;
    snapshots.add(snapshot);
    if (snapshot.t() >= lastDrawnT + (1d / frameRate) - tolerance) {
      lastDrawnT = snapshot.t();
      //create image
      BufferedImage image = new BufferedImage(w, h, BufferedImage.TYPE_3BYTE_BGR);
      //draw
      Graphics2D g = image.createGraphics();
      g.setClip(0, 0, image.getWidth(), image.getHeight());
      drawer.draw(snapshots, g);
      g.dispose();
      //add
      images.add(image);
      snapshots.clear();
    }
  }

  @Override
  public File get() {
    if (images.isEmpty()) {
      L.warning("No snapshot to save: abort");
      return null;
    }
    L.fine(String.format("Saving video on %s", file));
    try {
      Instant encodingStartInstant = Instant.now();
      VideoUtils.encodeAndSave(images, frameRate, file, encoder);
      L.fine(String.format(
          "Video saved: %.1fMB written in %.2fs",
          Files.size(file.toPath()) / 1024f / 1024f,
          Duration.between(encodingStartInstant, Instant.now()).toMillis() / 1000f
      ));
    } catch (IOException e) {
      L.severe(String.format("Cannot save file due to %s", e));
      return null;
    }
    return file;
  }
}
