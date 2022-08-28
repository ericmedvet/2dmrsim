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

package it.units.erallab.mrsim2d.engine.dyn4j;

import it.units.erallab.mrsim2d.core.geometry.Point;
import it.units.erallab.mrsim2d.core.geometry.Poly;
import org.dyn4j.geometry.Triangle;
import org.dyn4j.geometry.Vector2;
import org.dyn4j.geometry.decompose.SweepLine;
import org.dyn4j.geometry.decompose.Triangulator;

import java.util.Arrays;
import java.util.List;

/**
 * @author "Eric Medvet" on 2022/08/28 for 2dmrsim
 */
public class Utils {

  private Utils() {
  }

  public static List<Poly> decompose(Poly poly) {
    Triangulator triangulator = new SweepLine();
    List<Triangle> triangles = triangulator.triangulate(
        Arrays.stream(poly.vertexes()).map(p -> new Vector2(p.x(), p.y())).toArray(Vector2[]::new)
    );
    return triangles.stream()
        .map(c -> new Poly(
            Arrays.stream(c.getVertices()).map(v -> new Point(v.x, v.y)).toArray(Point[]::new)
        ))
        .toList();
  }

}
