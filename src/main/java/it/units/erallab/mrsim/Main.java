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

package it.units.erallab.mrsim;

import it.units.erallab.mrsim.core.Snapshot;
import it.units.erallab.mrsim.core.actions.*;
import it.units.erallab.mrsim.core.bodies.Body;
import it.units.erallab.mrsim.core.bodies.Voxel;
import it.units.erallab.mrsim.core.geometry.Point;
import it.units.erallab.mrsim.core.geometry.Poly;
import it.units.erallab.mrsim.engine.Engine;
import it.units.erallab.mrsim.engine.dyn4j.Dyn4JEngine;
import it.units.erallab.mrsim.viewer.*;

import java.io.File;
import java.io.IOException;
import java.util.function.Consumer;

/**
 * @author "Eric Medvet" on 2022/07/06 for 2dmrsim
 */
public class Main {
  public static void main(String[] args) throws IOException {
    Poly ball = Poly.regular(1, 20);
    Poly ground = Poly.rectangle(10, 2);
    double ballInterval = 2.5d;
    Engine engine = new Dyn4JEngine();
    Voxel v1 = engine.perform(new CreateAndTranslateVoxel(1, 1, new Point(5, 4))).orElseThrow();
    Voxel v2 = engine.perform(new CreateAndTranslateVoxel(1, 1, new Point(5, 5))).orElseThrow();
    Voxel v3 = engine.perform(new CreateAndTranslateVoxel(1, 1, new Point(6, 5))).orElseThrow();
    engine.perform(new AttachClosestAnchors(2, v1, v2)).orElseThrow();
    engine.perform(new AttachClosestAnchors(2, v2, v3)).orElseThrow();
    engine.perform(new CreateUnmovableBody(ground));
    FramesImageBuilder imageBuilder = new FramesImageBuilder(
        400,
        200,
        20,
        0.25,
        FramesImageBuilder.Direction.VERTICAL,
        Drawers.basic()
    );
    VideoBuilder videoBuilder = new VideoBuilder(
        600,
        400,
        0,
        20,
        24,
        VideoUtils.EncoderFacility.FFMPEG_SMALL,
        new File("/home/eric/experiments/balls.mp4"),
        Drawers.basic()
    );
    RealtimeViewer viewer = new RealtimeViewer(Drawers.basic());
    Consumer<Snapshot> consumer = viewer;
    while (engine.t() < 100) {
      Snapshot snapshot = engine.tick();
      if (Math.floor(engine.t() / ballInterval) > (snapshot.bodies().size() - 4)) {
        engine.perform(new CreateAndTranslateRigidBody(
            ball,
            2,
            new Point(5.5, snapshot.bodies()
                .stream()
                .mapToDouble(b -> b.poly().boundingBox().max().y())
                .max()
                .orElse(10)+2d)
        ));
      }
      if (engine.t() > 10 && engine.t() < 11) {
        engine.perform(new DetachAllAnchorsFromAnchorable(v1, v2));
      }
      for (Body body : snapshot.bodies()) {
        if (body.poly().center().y() < -3) {
          engine.perform(new RemoveBody(body));
        }
      }
      consumer.accept(snapshot);
    }
    //ImageIO.write(imageBuilder.get(), "png", new File("/home/eric/experiments/simple.png"));
    //videoBuilder.get();
  }
}
