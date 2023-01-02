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

package io.github.ericmedvet.mrsim2d.engine.dyn4j;

import io.github.ericmedvet.mrsim2d.core.geometry.Point;
import io.github.ericmedvet.mrsim2d.core.geometry.Poly;
import io.github.ericmedvet.mrsim2d.core.geometry.Segment;
import org.dyn4j.geometry.Polygon;
import org.dyn4j.geometry.Vector2;
import org.dyn4j.geometry.decompose.Bayazit;
import org.dyn4j.geometry.decompose.Decomposer;
import org.dyn4j.geometry.decompose.EarClipping;
import org.dyn4j.geometry.decompose.SweepLine;

import java.util.*;
import java.util.function.Supplier;
import java.util.stream.Stream;

/**
 * @author "Eric Medvet" on 2022/08/28 for 2dmrsim
 */
public class Utils {

  private final static double NATIVE_THRESHOLD = 100;

  private Utils() {
  }

  public enum DecomposeMethod {
    NATIVE_SWEEP_LINE(SweepLine::new),
    NATIVE_BAYAZIT(Bayazit::new),
    NATIVE_EAR_CLIPPING(EarClipping::new),
    Y_SECTION(null);

    private final Supplier<Decomposer> decomposer;

    DecomposeMethod(Supplier<Decomposer> decomposer) {
      this.decomposer = decomposer;
    }

    public Supplier<Decomposer> getDecomposer() {
      return decomposer;
    }
  }

  public static List<Poly> decompose(Poly poly) {
    if (poly.vertexes().length < NATIVE_THRESHOLD) {
      return decompose(poly, DecomposeMethod.NATIVE_BAYAZIT);
    }
    try {
      return decompose(poly, DecomposeMethod.Y_SECTION);
    } catch (IllegalArgumentException e) {
      return decompose(poly, DecomposeMethod.NATIVE_BAYAZIT);
    }
  }

  public static List<Poly> decompose(Poly poly, DecomposeMethod method) {
    if (method.getDecomposer() != null) {
      return method.getDecomposer().get().decompose(
              Arrays.stream(poly.vertexes()).map(Utils::point).toList()
          ).stream()
          .map(c -> {
            if (c instanceof Polygon polygon) {
              return new Poly(Arrays.stream(polygon.getVertices()).map(Utils::point).toArray(Point[]::new));
            }
            throw new IllegalArgumentException("Unsupported convex type %s".formatted(c.getClass().getSimpleName()));
          }).toList();
    }
    if (method.equals(DecomposeMethod.Y_SECTION)) {
      List<Double> xs = Arrays.stream(poly.vertexes())
          .map(Point::x).distinct()
          .sorted(Comparator.comparingDouble(x -> x))
          .toList();
      List<Segment> nonVerticalSides = poly.sides().stream()
          .filter(s -> s.p1().x() != s.p2().x())
          .toList();
      List<Poly> polies = new ArrayList<>();
      for (int i = 1; i < xs.size(); i = i + 1) {
        double x1 = xs.get(i - 1);
        double x2 = xs.get(i);
        //find left intersections
        List<Double> ys1 = nonVerticalSides.stream()
            .map(s -> s.yAt(x1))
            .filter(Optional::isPresent)
            .map(Optional::get)
            .toList();
        //find right intersections
        List<Double> ys2 = nonVerticalSides.stream()
            .map(s -> s.yAt(x2))
            .filter(Optional::isPresent)
            .map(Optional::get)
            .toList();
        //check number (>2 <5)
        if (ys1.size() + ys2.size() <= 2) {
          throw new IllegalArgumentException("Unsupported poly type: 0 thickness at [%.3f,%3f]".formatted(x1, x2));
        }
        if (ys1.size() > 4) {
          throw new IllegalArgumentException("Unsupported poly type: %d>4 intersections at %.3f".formatted(
              ys1.size(),
              x1
          ));
        }
        if (ys2.size() > 4) {
          throw new IllegalArgumentException("Unsupported poly type: %d>4 intersections at %.3f".formatted(
              ys2.size(),
              x2
          ));
        }
        //build poly
        List<Point> points = Stream.of(
            new Point(x1, ys1.stream().min(Comparator.comparingDouble(v -> v)).orElseThrow()),
            new Point(x2, ys1.stream().min(Comparator.comparingDouble(v -> v)).orElseThrow()),
            new Point(x2, ys1.stream().max(Comparator.comparingDouble(v -> v)).orElseThrow()),
            new Point(x1, ys1.stream().max(Comparator.comparingDouble(v -> v)).orElseThrow())
        ).distinct().toList();
        polies.add(new Poly(points.toArray(Point[]::new)));
      }
      return polies;
    }
    throw new UnsupportedOperationException("Decompose method %s is not supported".formatted(method));
  }

  public static Vector2 point(Point p) {
    return new Vector2(p.x(), p.y());
  }

  public static Point point(Vector2 v) {
    return new Point(v.x, v.y);
  }

}
