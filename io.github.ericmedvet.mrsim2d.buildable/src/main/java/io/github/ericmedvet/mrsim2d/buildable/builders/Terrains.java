/*-
 * ========================LICENSE_START=================================
 * mrsim2d-buildable
 * %%
 * Copyright (C) 2020 - 2025 Eric Medvet
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

package io.github.ericmedvet.mrsim2d.buildable.builders;

import io.github.ericmedvet.jnb.core.Cacheable;
import io.github.ericmedvet.jnb.core.Discoverable;
import io.github.ericmedvet.jnb.core.Param;
import io.github.ericmedvet.mrsim2d.core.geometry.Path;
import io.github.ericmedvet.mrsim2d.core.geometry.Point;
import io.github.ericmedvet.mrsim2d.core.geometry.Terrain;
import java.util.List;
import java.util.Random;
import java.util.random.RandomGenerator;

@Discoverable(prefixTemplate = "sim|s.terrain|t")
public class Terrains {
  public static final double BORDER_H = 100d;
  public static final double W = 500d;
  public static final double H = 25d;
  public static final double CHUNK_W = 5d;
  public static final double CHUNK_H = 0.75d;
  public static final double BORDER_W = 10d;
  public static final double ANGLE = 10d;
  public static final double START_W = 30d;
  public static final double HOLE_H = 10d;
  public static final double HOLE_W = 5d;
  public static final double END_W = 30d;
  public static final double HOLE_DIS_W = 9d;
  public static final double FLAT_W = 5d;
  public static final double FLAT_H = 5d;

  private Terrains() {
  }

  @SuppressWarnings("unused")
  @Cacheable
  public static Terrain downhill(
      @Param(value = "name", iS = "downhill[{a}]") String name,
      @Param(value = "w", dD = W) Double w,
      @Param(value = "h", dD = H) Double h,
      @Param(value = "borderW", dD = BORDER_W) Double borderW,
      @Param(value = "borderH", dD = BORDER_H) Double borderH,
      @Param(value = "a", dD = ANGLE) Double a
  ) {
    return Terrain.fromPath(new Path(new Point(w, -w * Math.toRadians(a))), h, borderW, borderH);
  }

  @SuppressWarnings("unused")
  @Cacheable
  public static Terrain flat(
      @Param(value = "name", dS = "flat") String name,
      @Param(value = "w", dD = W) Double w,
      @Param(value = "h", dD = H) Double h,
      @Param(value = "borderW", dD = BORDER_W) Double borderW,
      @Param(value = "borderH", dD = BORDER_H) Double borderH
  ) {
    return Terrain.fromPath(new Path(new Point(w, 0)), h, borderW, borderH);
  }

  @SuppressWarnings("unused")
  @Cacheable
  public static Terrain hilly(
      @Param(value = "name", iS = "hilly[{chunkW}-{chunkH}]") String name,
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
    return Terrain.fromPath(path, h, borderW, borderH);
  }

  @SuppressWarnings("unused")
  @Cacheable
  public static Terrain holed(
      @Param(value = "name", dS = "holed") String name,
      @Param(value = "startW", dD = START_W) double startW,
      @Param(value = "holeH", dD = HOLE_H) double holeH,
      @Param(
          value = "holeWs", dDs = {HOLE_W}) List<Double> holeWs,
      @Param(value = "holeDisW", dD = HOLE_DIS_W) double holeDisW,
      @Param(value = "endW", dD = END_W) double endW,
      @Param(value = "borderW", dD = BORDER_W) double borderW,
      @Param(value = "borderH", dD = BORDER_H) double borderH
  ) {

    Path p = new Path(new Point(startW, 0));
    for (double holeW : holeWs) {
      p = p.moveBy(0, -holeH).moveBy(holeW, 0).moveBy(0, holeH).moveBy(holeDisW, 0);
    }
    p = p.moveBy(endW, 0);

    return Terrain.fromPath(p, H, borderW, borderH);
  }

  @SuppressWarnings("unused")
  @Cacheable
  public static Terrain steppy(
      @Param(value = "name", iS = "steppy[{chunkW}-{chunkH}]") String name,
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
      path = path.moveBy(sW, 0).moveBy(0, sH);
    }
    return Terrain.fromPath(path, h, borderW, borderH);
  }

  @SuppressWarnings("unused")
  @Cacheable
  public static Terrain sumoArena(
      @Param(value = "name", dS = "sumoArena") String name,
      @Param(value = "h", dD = 20) Double h,
      @Param(value = "borderW", dD = 1) Double borderW,
      @Param(value = "borderH", dD = 25) Double borderH,
      @Param(value = "holeW", dD = 20) Double holeW,
      @Param(value = "flatW", dD = 20) Double flatW,
      @Param(value = "flatH", dD = 15) Double flatH
  ) {
    Path p = new Path(new Point(holeW, 0));
    p = p.moveBy(0, flatH).moveBy(flatW, 0).moveBy(0, -flatH).moveBy(holeW, 0);
    return Terrain.fromPath(p, h, borderW, borderH);
  }

  @SuppressWarnings("unused")
  @Cacheable
  public static Terrain uphill(
      @Param(value = "name", iS = "uphill[{a}]") String name,
      @Param(value = "w", dD = W) Double w,
      @Param(value = "h", dD = H) Double h,
      @Param(value = "borderW", dD = BORDER_W) Double borderW,
      @Param(value = "borderH", dD = BORDER_H) Double borderH,
      @Param(value = "a", dD = ANGLE) Double a
  ) {
    return Terrain.fromPath(new Path(new Point(w, w * Math.toRadians(a))), h, borderW, borderH);
  }
}
