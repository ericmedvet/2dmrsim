/*-
 * ========================LICENSE_START=================================
 * mrsim2d-viewer
 * %%
 * Copyright (C) 2020 - 2024 Eric Medvet
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * =========================LICENSE_END==================================
 */
package io.github.ericmedvet.mrsim2d.viewer;

import io.github.ericmedvet.jviz.core.drawer.Video;
import io.github.ericmedvet.jviz.core.drawer.VideoBuilder;
import io.github.ericmedvet.mrsim2d.core.Snapshot;
import io.github.ericmedvet.mrsim2d.core.engine.Engine;
import io.github.ericmedvet.mrsim2d.core.tasks.Task;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

public class TaskVideoBuilder<A> implements VideoBuilder<A> {

  private final Task<A, ?, ?> task;
  private final Function<String, Drawer> drawerBuilder;
  private final Supplier<Engine> engineSupplier;
  private final String title;
  private final double startTime;
  private final double endTime;
  private final double frameRate;

  private class ImageCollector implements Consumer<Snapshot> {
    private final List<BufferedImage> images = new ArrayList<>();
    private final List<Snapshot> snapshots = new ArrayList<>();
    private final List<Double> snapshotTs = new ArrayList<>();
    private double lastDrawnT;
    private double lastT;
    private final int w;
    private final int h;
    private final Drawer drawer;

    public ImageCollector(int w, int h) {
      this.w = w;
      this.h = h;
      drawer = drawerBuilder.apply(title);
    }

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
        // create image
        BufferedImage image = new BufferedImage(w, h, BufferedImage.TYPE_3BYTE_BGR);
        // draw
        Graphics2D g = image.createGraphics();
        g.setClip(0, 0, image.getWidth(), image.getHeight());
        drawer.draw(snapshots, g);
        g.dispose();
        // add
        images.add(image);
        snapshots.clear();
      }
    }
  }

  public TaskVideoBuilder(
      Task<A, ?, ?> task,
      Function<String, Drawer> drawerBuilder,
      Supplier<Engine> engineSupplier,
      String title,
      double startTime,
      double endTime,
      double frameRate
  ) {
    this.task = task;
    this.drawerBuilder = drawerBuilder;
    this.engineSupplier = engineSupplier;
    this.title = title;
    this.startTime = startTime;
    this.endTime = endTime;
    this.frameRate = frameRate;
  }

  @Override
  public Video build(VideoInfo videoInfo, A a) {
    ImageCollector collector = new ImageCollector(videoInfo.w(), videoInfo.h());
    task.run(a, engineSupplier.get(), collector);
    return new Video(collector.images, frameRate, videoInfo.encoder());
  }
}
