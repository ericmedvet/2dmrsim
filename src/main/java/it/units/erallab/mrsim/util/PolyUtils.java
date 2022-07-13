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

package it.units.erallab.mrsim.util;

import it.units.erallab.mrsim.core.bodies.Anchor;
import it.units.erallab.mrsim.core.geometry.Path;
import it.units.erallab.mrsim.core.geometry.Point;
import it.units.erallab.mrsim.core.geometry.Poly;
import org.dyn4j.geometry.Convex;
import org.dyn4j.geometry.Polygon;
import org.dyn4j.geometry.Triangle;
import org.dyn4j.geometry.Vector2;
import org.dyn4j.geometry.decompose.SweepLine;
import org.dyn4j.geometry.decompose.Triangulator;

import java.util.*;
import java.util.function.Function;
import java.util.random.RandomGenerator;
import java.util.stream.Collectors;

/**
 * @author "Eric Medvet" on 2022/07/10 for 2dmrsim
 */
public class PolyUtils {

  public final static double TERRAIN_BORDER_HEIGHT = 100d;
  public static final int TERRAIN_WIDTH = 2000;
  public static final double TERRAIN_BORDER_WIDTH = 10d;

  private PolyUtils() {
  }

  public static Poly createTerrain(String name) {
    return createTerrain(name, TERRAIN_WIDTH, TERRAIN_BORDER_HEIGHT, TERRAIN_BORDER_WIDTH, TERRAIN_BORDER_HEIGHT);
  }

  public static Poly createTerrain(String name, double terrainW, double terrainH, double borderW, double borderH) {
    Function<String, Optional<Path>> provider = StringUtils.formattedProvider(Map.ofEntries(
        Map.entry("flat", p -> new Path(new Point(terrainW, 0))),
        Map.entry("hilly-(?<h>[0-9]+(\\.[0-9]+)?)-(?<w>[0-9]+(\\.[0-9]+)?)-(?<seed>[0-9]+)", p -> {
          double h = p.d().get("h");
          double w = p.d().get("w");
          RandomGenerator random = new Random(p.i().get("seed"));
          Path path = new Path(new Point(w, 0));
          double dW = 0d;
          while (dW < terrainW) {
            double sW = Math.max(1d, (random.nextGaussian() * 0.25 + 1) * w);
            double sH = random.nextGaussian() * h;
            dW = dW + sW;
            path = path.moveTo(sW, sH);
          }
          return path;
        }),
        Map.entry("steppy-(?<h>[0-9]+(\\.[0-9]+)?)-(?<w>[0-9]+(\\.[0-9]+)?)-(?<seed>[0-9]+)", p -> {
          double h = p.d().get("h");
          double w = p.d().get("w");
          RandomGenerator random = new Random(p.i().get("seed"));
          Path path = new Path(new Point(w, 0));
          double dW = 0d;
          while (dW < terrainW) {
            double sW = Math.max(1d, (random.nextGaussian() * 0.25 + 1) * w);
            double sH = random.nextGaussian() * h;
            dW = dW + sW;
            path = path
                .moveTo(sW, 0)
                .moveTo(0, sH);
          }
          return path;
        }),
        Map.entry("downhill-(?<angle>[0-9]+(\\.[0-9]+)?)", p -> {
          double angle = p.d().get("angle");
          return new Path(new Point(terrainW, -terrainW * Math.sin(angle / 180 * Math.PI)));
        }),
        Map.entry("uphill-(?<angle>[0-9]+(\\.[0-9]+)?)", p -> {
          double angle = p.d().get("angle");
          return new Path(new Point(terrainW, terrainW * Math.sin(angle / 180 * Math.PI)));
        })
    ));
    Path path = new Path(new Point(0, 0))
        .moveTo(0, borderH)
        .moveTo(borderW, 0)
        .moveTo(0, -borderH)
        .moveTo(provider.apply(name).orElseThrow())
        .moveTo(0, borderH)
        .moveTo(borderW, 0)
        .moveTo(0, -borderH);
    double maxX = Arrays.stream(path.points()).mapToDouble(Point::x).max().orElse(borderW);
    double minY = Arrays.stream(path.points()).mapToDouble(Point::y).min().orElse(borderW);
    path = path
        .add(maxX, minY - terrainH)
        .moveTo(-maxX, 0);
    return path.toPoly();
  }

  public static Set<Poly> decompose(Poly poly) {
    Triangulator triangulator = new SweepLine();
    List<Triangle> triangles = triangulator.triangulate(
        Arrays.stream(poly.vertexes()).map(p -> new Vector2(p.x(), p.y())).toArray(Vector2[]::new)
    );
    return triangles.stream()
        .map(c -> new Poly(
            Arrays.stream(c.getVertices()).map(v -> new Point(v.x, v.y)).toArray(Point[]::new)
        ))
        .collect(Collectors.toSet());
  }

  private static double angle(Point a, Point b, Point c) {
    double angle = c.diff(b).direction() - a.diff(b).direction();
    return angle > 0 ? angle : (angle + 2d * Math.PI);
  }

  public static Poly makeCounterClockwise(Poly poly) {
    Point c = poly.center();
    double sum = 0d;
    for (int i = 1; i < poly.vertexes().length; i++) {
      sum = sum + angle(poly.vertexes()[i], c, poly.vertexes()[i - 1]);
    }
    sum = sum + angle(poly.vertexes()[0], c, poly.vertexes()[poly.vertexes().length - 1]);
    if (sum >= 0) {
      return poly;
    }
    Point[] vertexes = new Point[poly.vertexes().length];
    for (int i = 0; i < vertexes.length; i++) {
      vertexes[i] = poly.vertexes()[vertexes.length - 1 - i];
    }
    return new Poly(vertexes);
  }

  public static Path zigZag(Point src, Point dst, int n, double w) {
    Point dL = dst.diff(src).scale(0.5d / (double) n);
    double a = dL.direction() + Math.PI / 2d;
    Point dP = new Point(a).scale(w);
    Path p = new Path(src)
        .moveTo(dL.diff(dP.scale(0.5)));
    for (int i = 0; i < n - 1; i++) {
      p = p
          .moveTo(dL.sum(dP))
          .moveTo(dL.diff(dP));
    }
    return p.moveTo(dL.sum(dP.scale(0.5)));
  }

  public static double distance(Point p, Point p1, Point p2) {
    p2 = p2.diff(p1);
    p = p.diff(p1);
    double a = p.angle(p2);
    double l = p.magnitude();
    if (a < Math.PI / 2d && a > -Math.PI / 2d) {
      if (Math.cos(a) * l < p2.magnitude()) {
        return Math.sin(a) * l;
      }
      return p2.sum(p1).distance(p);
    }
    return l;
  }

  public static double distance(Point p, Poly poly) {
    double minD = distance(p, poly.vertexes()[poly.vertexes().length - 1], poly.vertexes()[0]);
    for (int i = 0; i < poly.vertexes().length - 1; i++) {
      minD = Math.min(minD, distance(p, poly.vertexes()[i], poly.vertexes()[i + 1]));
    }
    return minD;
  }

  public static double minAnchorDistance(Anchor anchor1, Anchor anchor2) {
    double d1 = PolyUtils.distance(anchor1.point(), anchor1.anchorable().poly());
    double d2 = PolyUtils.distance(anchor2.point(), anchor2.anchorable().poly());
    return d1 + d2;
  }

}
