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

package io.github.ericmedvet.mrsim2d.buildable.builders;

import io.github.ericmedvet.jnb.core.Param;
import io.github.ericmedvet.jsdynsym.core.DoubleRange;
import io.github.ericmedvet.mrsim2d.core.geometry.Path;
import io.github.ericmedvet.mrsim2d.core.geometry.Point;
import io.github.ericmedvet.mrsim2d.core.geometry.Terrain;

import java.util.Arrays;
import java.util.Random;
import java.util.random.RandomGenerator;

/**
 * @author "Eric Medvet" on 2022/08/11 for 2dmrsim
 */
public class Terrains {
  public final static double BORDER_H = 100d;
  public static final double W = 500d;
  public static final double H = 25d;
  public static final double CHUNK_W = 5d;
  public static final double CHUNK_H = 0.75d;
  public static final double BORDER_W = 10d;
  public static final double ANGLE = 10d;

  @SuppressWarnings("unused")
  public static Terrain downhill(
      @Param(value = "w", dD = W) Double w,
      @Param(value = "h", dD = H) Double h,
      @Param(value = "borderW", dD = BORDER_W) Double borderW,
      @Param(value = "borderH", dD = BORDER_H) Double borderH,
      @Param(value = "a", dD = ANGLE) Double a
  ) {
    return fromPath(
        new Path(new Point(w, -w * Math.toRadians(a))),
        h, borderW, borderH
    );
  }

  @SuppressWarnings("unused")
  public static Terrain flat(
      @Param(value = "w", dD = W) Double w,
      @Param(value = "h", dD = H) Double h,
      @Param(value = "borderW", dD = BORDER_W) Double borderW,
      @Param(value = "borderH", dD = BORDER_H) Double borderH
  ) {
    return fromPath(
        new Path(new Point(w, 0)),
        h,
        borderW,
        borderH
    );
  }

  @SuppressWarnings("unused")
  private static Terrain fromPath(Path partialPath, double terrainH, double borderW, double borderH) {
    Path path = new Path(Point.ORIGIN)
        .moveBy(0, borderH)
        .moveBy(borderW, 0)
        .moveBy(0, -borderH)
        .moveBy(partialPath)
        .moveBy(0, borderH)
        .moveBy(borderW, 0)
        .moveBy(0, -borderH);
    double maxX = Arrays.stream(path.points()).mapToDouble(Point::x).max().orElse(borderW);
    double minY = Arrays.stream(path.points()).mapToDouble(Point::y).min().orElse(borderW);
    path = path
        .add(maxX, minY - terrainH)
        .moveBy(-maxX, 0);
    return new Terrain(path.toPoly(), new DoubleRange(borderW, maxX - borderW));
  }

  @SuppressWarnings("unused")
  public static Terrain hilly(
      @Param(value = "w", dD = W) Double w,
      @Param(value = "h", dD = H) Double h,
      @Param(value = "borderW", dD = BORDER_W) Double borderW,
      @Param(value = "borderH", dD = BORDER_H) Double borderH,
      @Param(value = "chunkW", dD = CHUNK_W) Double chunkW,
      @Param(value = "chunkH", dD = CHUNK_H) Double chunkH,
      @Param(value = "seed", dI = 1) Integer seed
  ) {
    RandomGenerator random = new Random(seed);
    Path path = new Path(new Point(chunkW, 0));
    double dW = 0d;
    while (dW < w) {
      double sW = Math.max(1d, (random.nextGaussian() * 0.25 + 1) * chunkW);
      double sH = random.nextGaussian() * chunkH;
      dW = dW + sW;
      path = path.moveBy(sW, sH);
    }
    return fromPath(path, h, borderW, borderH);
  }

  @SuppressWarnings("unused")
  public static Terrain steppy(
      @Param(value = "w", dD = W) Double w,
      @Param(value = "h", dD = H) Double h,
      @Param(value = "borderW", dD = BORDER_W) Double borderW,
      @Param(value = "borderH", dD = BORDER_H) Double borderH,
      @Param(value = "chunkW", dD = CHUNK_W) Double chunkW,
      @Param(value = "chunkH", dD = CHUNK_H) Double chunkH,
      @Param(value = "seed", dI = 1) Integer seed
  ) {
    RandomGenerator random = new Random(seed);
    Path path = new Path(new Point(chunkW, 0));
    double dW = 0d;
    while (dW < w) {
      double sW = Math.max(1d, (random.nextGaussian() * 0.25 + 1) * chunkW);
      double sH = random.nextGaussian() * chunkH;
      dW = dW + sW;
      path = path
          .moveBy(sW, 0)
          .moveBy(0, sH);
    }
    return fromPath(path, h, borderW, borderH);
  }

  @SuppressWarnings("unused")
  public static Terrain uphill(
      @Param(value = "w", dD = W) Double w,
      @Param(value = "h", dD = H) Double h,
      @Param(value = "borderW", dD = BORDER_W) Double borderW,
      @Param(value = "borderH", dD = BORDER_H) Double borderH,
      @Param(value = "a", dD = ANGLE) Double a
  ) {
    return fromPath(
        new Path(new Point(w, w * Math.toRadians(a))),
        h, borderW, borderH
    );
  }

}
