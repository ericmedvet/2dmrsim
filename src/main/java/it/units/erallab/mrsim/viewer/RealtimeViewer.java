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

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferStrategy;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * @author "Eric Medvet" on 2022/07/08 for 2dmrsim
 */
public class RealtimeViewer extends JFrame implements Consumer<Snapshot> {
  private final static long WAIT_MILLIS = 10;
  private final static double FRAME_RATE = 24;
  private final static int INIT_WIN_WIDTH = 1000;
  private final static int INIT_WIN_HEIGHT = 600;

  private final double frameRate;
  private final Drawer drawer;

  private final Canvas canvas;
  private final JButton pauseButton;
  private double lastDrawnT;
  private double lastDrawingMillis;
  private Instant startingInstant;
  private final List<Snapshot> snapshots;
  private boolean isPaused;

  public RealtimeViewer(double frameRate, Drawer drawer) {
    super("Realtime simulation viewer");
    this.frameRate = frameRate;
    this.drawer = drawer;
    //create/set ui components
    setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    Dimension dimension = new Dimension(INIT_WIN_WIDTH, INIT_WIN_HEIGHT);
    canvas = new Canvas();
    canvas.setPreferredSize(dimension);
    canvas.setMinimumSize(dimension);
    canvas.setMaximumSize(dimension);
    getContentPane().add(canvas, BorderLayout.CENTER);
    pauseButton = new JButton();
    pauseButton.setText("Pause");
    isPaused = false;
    pauseButton.addActionListener(e -> {
      isPaused = !isPaused;
      pauseButton.setText(isPaused ? "Unpause" : "Pause");
    });
    getContentPane().add(pauseButton, BorderLayout.PAGE_END);
    //pack
    pack();
    //start
    setVisible(true);
    canvas.setIgnoreRepaint(true);
    canvas.createBufferStrategy(2);
    lastDrawnT = Double.NEGATIVE_INFINITY;
    snapshots = new ArrayList<>();
  }

  public RealtimeViewer(Drawer drawer) throws HeadlessException {
    this(FRAME_RATE, drawer);
  }

  @SuppressWarnings("BusyWait")
  @Override
  public void accept(Snapshot snapshot) {
    if (startingInstant == null) {
      startingInstant = Instant.now();
    }
    snapshots.add(snapshot);
    if (snapshot.t() - lastDrawnT >= 1d / frameRate) {
      //wait
      while (true) {
        double currentWallTime = Duration.between(startingInstant, Instant.now()).toMillis() / 1000d;
        if (isPaused || snapshot.t() > currentWallTime - lastDrawingMillis / 1000d - WAIT_MILLIS / 1000d) {
          try {
            Thread.sleep(WAIT_MILLIS);
          } catch (InterruptedException e) {
            //ignore
          }
        } else {
          break;
        }
      }
      Instant drawingTimeStart = Instant.now();
      //get graphics
      Graphics2D g = (Graphics2D) canvas.getBufferStrategy().getDrawGraphics();
      g.setClip(0, 0, canvas.getWidth(), canvas.getHeight());
      //draw
      drawer.draw(snapshots, g);
      //dispose and encode
      g.dispose();
      BufferStrategy strategy = canvas.getBufferStrategy();
      if (!strategy.contentsLost()) {
        strategy.show();
      }
      Toolkit.getDefaultToolkit().sync();
      lastDrawingMillis = Duration.between(drawingTimeStart, Instant.now()).toMillis();
      //update time
      lastDrawnT = snapshot.t();
      snapshots.clear();
    }
  }
}
