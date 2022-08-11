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

package it.units.erallab.mrsim.util.builder;

import it.units.erallab.mrsim.core.geometry.Path;
import it.units.erallab.mrsim.core.geometry.Point;
import it.units.erallab.mrsim.core.geometry.Terrain;
import it.units.erallab.mrsim.util.DoubleRange;

import java.util.Arrays;
import java.util.Random;
import java.util.random.RandomGenerator;

/**
 * @author "Eric Medvet" on 2022/08/11 for 2dmrsim
 */
public class TerrainNamedBuilder extends NamedBuilder {
  public final static double BORDER_H = 100d;
  public static final double W = 2000d;
  public static final double H = 25d;
  public static final double CHUNK_W = 5d;
  public static final double CHUNK_H = 1d;
  public static final double BORDER_W = 10d;
  public static final double ANGLE = 10d;

  private TerrainNamedBuilder() {
    register("flat", TerrainNamedBuilder::createFlat);
    register("hilly", TerrainNamedBuilder::createHilly);
    register("steppy", TerrainNamedBuilder::createSteppy);
    register("downhill", TerrainNamedBuilder::createDownhill);
    register("uphill", TerrainNamedBuilder::createUphill);
  }

  private static Terrain createFlat(ParamMap m, NamedBuilder nb) {
    return fromPath(
        new Path(new Point(m.d("w", W), 0)),
        m.d("h", H),
        m.d("borderW", BORDER_W),
        m.d("borderH", BORDER_H)
    );
  }

  private static Terrain createHilly(ParamMap m, NamedBuilder nb) {
    double h = m.d("hillH", CHUNK_H);
    double w = m.d("hillW", CHUNK_W);
    RandomGenerator random = new Random(m.i("seed", 1));
    Path path = new Path(new Point(w, 0));
    double dW = 0d;
    while (dW < m.d("w", W)) {
      double sW = Math.max(1d, (random.nextGaussian() * 0.25 + 1) * w);
      double sH = random.nextGaussian() * h;
      dW = dW + sW;
      path = path.moveTo(sW, sH);
    }
    return fromPath(
        path,
        m.d("h", H),
        m.d("borderW", BORDER_W),
        m.d("borderH", BORDER_H)
    );
  }

  private static Terrain createSteppy(ParamMap m, NamedBuilder nb) {
    double h = m.d("hillH", CHUNK_H);
    double w = m.d("hillW", CHUNK_W);
    RandomGenerator random = new Random(m.i("seed", 1));
    Path path = new Path(new Point(w, 0));
    double dW = 0d;
    while (dW < m.d("w", W)) {
      double sW = Math.max(1d, (random.nextGaussian() * 0.25 + 1) * w);
      double sH = random.nextGaussian() * h;
      dW = dW + sW;
      path = path
          .moveTo(sW, 0)
          .moveTo(0, sH);
    }
    return fromPath(
        path,
        m.d("h", H),
        m.d("borderW", BORDER_W),
        m.d("borderH", BORDER_H)
    );
  }

  private static Terrain createDownhill(ParamMap m, NamedBuilder nb) {
    return fromPath(
        new Path(new Point(
            m.d("w", W),
            -m.d("w", W) * Math.sin(m.d("a", ANGLE) / 180 * Math.PI)
        )),
        m.d("h", H),
        m.d("borderW", BORDER_W),
        m.d("borderH", BORDER_H)
    );
  }

  private static Terrain createUphill(ParamMap m, NamedBuilder nb) {
    return fromPath(
        new Path(new Point(
            m.d("w", W),
            -m.d("w", W) * Math.sin(m.d("a", ANGLE) / 180 * Math.PI)
        )),
        m.d("h", H),
        m.d("borderW", BORDER_W),
        m.d("borderH", BORDER_H)
    );
  }

  private static Terrain fromPath(Path partialPath, double terrainH, double borderW, double borderH) {
    Path path = new Path(new Point(0, 0))
        .moveTo(0, borderH)
        .moveTo(borderW, 0)
        .moveTo(0, -borderH)
        .moveTo(partialPath)
        .moveTo(0, borderH)
        .moveTo(borderW, 0)
        .moveTo(0, -borderH);
    double maxX = Arrays.stream(path.points()).mapToDouble(Point::x).max().orElse(borderW);
    double minY = Arrays.stream(path.points()).mapToDouble(Point::y).min().orElse(borderW);
    path = path
        .add(maxX, minY - terrainH)
        .moveTo(-maxX, 0);
    return new Terrain(path.toPoly(), new DoubleRange(borderW, maxX - borderW));
  }

  private final static TerrainNamedBuilder INSTANCE = new TerrainNamedBuilder();

  public static TerrainNamedBuilder getInstance() {
    return INSTANCE;
  }
}
