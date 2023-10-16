
package io.github.ericmedvet.mrsim2d.buildable.builders;

import io.github.ericmedvet.jnb.core.Param;
import io.github.ericmedvet.mrsim2d.core.geometry.Path;
import io.github.ericmedvet.mrsim2d.core.geometry.Point;
import io.github.ericmedvet.mrsim2d.core.geometry.Terrain;

import java.util.Random;
import java.util.random.RandomGenerator;
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
    return Terrain.fromPath(
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
    return Terrain.fromPath(
        new Path(new Point(w, 0)),
        h,
        borderW,
        borderH
    );
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
    return Terrain.fromPath(path, h, borderW, borderH);
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
    return Terrain.fromPath(path, h, borderW, borderH);
  }

  @SuppressWarnings("unused")
  public static Terrain uphill(
      @Param(value = "w", dD = W) Double w,
      @Param(value = "h", dD = H) Double h,
      @Param(value = "borderW", dD = BORDER_W) Double borderW,
      @Param(value = "borderH", dD = BORDER_H) Double borderH,
      @Param(value = "a", dD = ANGLE) Double a
  ) {
    return Terrain.fromPath(
        new Path(new Point(w, w * Math.toRadians(a))),
        h, borderW, borderH
    );
  }

}
