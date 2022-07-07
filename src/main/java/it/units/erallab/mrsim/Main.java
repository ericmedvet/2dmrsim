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
import it.units.erallab.mrsim.core.actions.CreateRigidBodyAt;
import it.units.erallab.mrsim.core.geometry.Point;
import it.units.erallab.mrsim.core.geometry.Poly;
import it.units.erallab.mrsim.engine.Engine;
import it.units.erallab.mrsim.engine.simple.SimpleEngine;
import it.units.erallab.mrsim.viewer.Drawers;
import it.units.erallab.mrsim.viewer.FramesImageBuilder;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

/**
 * @author "Eric Medvet" on 2022/07/06 for 2dmrsim
 */
public class Main {
  public static void main(String[] args) throws IOException {
    Poly triangle = new Poly(new Point(1, 1), new Point(2, 2), new Point(3, 1));
    Poly rectangle = new Poly(new Point(0, 0), new Point(0, 2), new Point(1, 2), new Point(1, 0));
    Engine engine = new SimpleEngine();
    engine.perform(new CreateRigidBodyAt(triangle, 1, new Point(0, 0)));
    FramesImageBuilder builder = new FramesImageBuilder(500,400,5,0.5, FramesImageBuilder.Direction.VERTICAL, Drawers.basic());
    while (engine.t() < 5) {
      Snapshot snapshot = engine.tick();
      if (engine.t()>2 && snapshot.bodies().size()<2) {
        engine.perform(new CreateRigidBodyAt(rectangle, 1, new Point(0, 0)));
      }
      System.out.printf(
          "t=%4.1f nBodies=%1d nAgents=%1d%n",
          snapshot.t(),
          snapshot.bodies().size(),
          snapshot.agentPairs().size()
      );
      builder.accept(snapshot);
    }
    BufferedImage image = builder.get();
    ImageIO.write(image,"png",new File("/home/eric/experiments/simple.png"));
  }
}
