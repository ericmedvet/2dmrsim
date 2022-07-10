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

import it.units.erallab.mrsim.core.geometry.Point;
import it.units.erallab.mrsim.core.geometry.Poly;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.random.RandomGenerator;

/**
 * @author "Eric Medvet" on 2022/07/10 for 2dmrsim
 */
public class PolyUtils {

  public final static double TERRAIN_BORDER_HEIGHT = 100d;
  public static final int TERRAIN_WIDTH = 2000;
  public static final double TERRAIN_BORDER_WIDTH = 10d;
  public static final double TERRAIN_START_LENGTH = 10d;

  private PolyUtils() {
  }

  public static Poly createTerrain(String name) {
    return createTerrain(name, TERRAIN_WIDTH, TERRAIN_BORDER_HEIGHT, TERRAIN_BORDER_WIDTH, TERRAIN_BORDER_HEIGHT);
  }

  public static Poly createTerrain(String name, double terrainW, double terrainH, double borderW, double borderH) {
    String flat = "flat";
    String hilly = "hilly-(?<h>[0-9]+(\\.[0-9]+)?)-(?<w>[0-9]+(\\.[0-9]+)?)-(?<seed>[0-9]+)";
    String steppy = "steppy-(?<h>[0-9]+(\\.[0-9]+)?)-(?<w>[0-9]+(\\.[0-9]+)?)-(?<seed>[0-9]+)";
    String downhill = "downhill-(?<angle>[0-9]+(\\.[0-9]+)?)";
    String uphill = "uphill-(?<angle>[0-9]+(\\.[0-9]+)?)";
    Map<String, String> params;
    List<Point> ps = new ArrayList<>();
    ps.add(new Point(0, 0));
    ps.add(new Point(0, borderH));
    ps.add(new Point(borderW, borderH));
    ps.add(new Point(borderW, 0));
    //noinspection UnusedAssignment
    if ((params = StringUtils.params(flat, name)) != null) {
      ps.add(new Point(terrainW + borderW, 0));
    } else if ((params = StringUtils.params(hilly, name)) != null) {
      double h = Double.parseDouble(params.get("h"));
      double w = Double.parseDouble(params.get("w"));
      RandomGenerator random = new Random(Integer.parseInt(params.get("seed")));
      while (ps.get(ps.size() - 1).x() < terrainW + borderW) {
        Point last = ps.get(ps.size() - 1);
        ps.add(new Point(
            last.x() + Math.max(1d, (random.nextGaussian() * 0.25 + 1) * w),
            last.y() + random.nextGaussian() * h
        ));
      }
    } else if ((params = StringUtils.params(steppy, name)) != null) {
      double h = Double.parseDouble(params.get("h"));
      double w = Double.parseDouble(params.get("w"));
      RandomGenerator random = new Random(Integer.parseInt(params.get("seed")));
      while (ps.get(ps.size() - 1).x() < terrainW + borderW) {
        Point last = ps.get(ps.size() - 1);
        ps.add(new Point(
            last.x() + Math.max(1d, (random.nextGaussian() * 0.25 + 1) * w),
            last.y()
        ));
        last = ps.get(ps.size() - 1);
        ps.add(new Point(
            last.x(),
            last.y() + random.nextGaussian() * h
        ));
      }
    } else if ((params = StringUtils.params(downhill, name)) != null) {
      double angle = Double.parseDouble(params.get("angle"));
      double dY = terrainW * Math.sin(angle / 180 * Math.PI);
      ps.add(new Point(terrainW + borderW, -dY));
    } else if ((params = StringUtils.params(uphill, name)) != null) {
      double angle = Double.parseDouble(params.get("angle"));
      double dY = (TERRAIN_WIDTH - 2 * TERRAIN_BORDER_WIDTH) * Math.sin(angle / 180 * Math.PI);
      ps.add(new Point(terrainW + borderW, dY));
    } else {
      throw new IllegalArgumentException(String.format("Unknown terrain name: %s", name));
    }
    double maxX = ps.stream().mapToDouble(Point::x).max().orElse(borderW);
    double minY = ps.stream().mapToDouble(Point::y).min().orElse(borderW);
    ps.add(new Point(maxX, 0));
    ps.add(new Point(maxX, borderH));
    ps.add(new Point(maxX + borderW, borderH));
    ps.add(new Point(maxX + borderW, minY - terrainH));
    ps.add(new Point(0, minY - terrainH));
    return new Poly(ps.toArray(Point[]::new));
  }

}
