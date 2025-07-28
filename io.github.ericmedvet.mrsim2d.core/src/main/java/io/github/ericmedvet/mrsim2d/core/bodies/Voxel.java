/*-
 * ========================LICENSE_START=================================
 * mrsim2d-core
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

package io.github.ericmedvet.mrsim2d.core.bodies;

import io.github.ericmedvet.jnb.datastructure.DoubleRange;
import io.github.ericmedvet.mrsim2d.core.geometry.Point;
import io.github.ericmedvet.mrsim2d.core.geometry.Segment;
import java.util.Collection;

public interface Voxel extends SoftBody, Anchorable {

  Material DEFAULT_MATERIAL = new Material();

  enum Side {
    N(Vertex.NE, Vertex.NW, Math.PI / 2d), E(Vertex.NE, Vertex.SE, 0d), W(Vertex.NW, Vertex.SW, Math.PI), S(
        Vertex.SE,
        Vertex.SW,
        -Math.PI / 2d
    );

    private final Vertex vertex1;
    private final Vertex vertex2;
    private final double normalAngle;

    Side(Vertex vertex1, Vertex vertex2, double normalAngle) {
      this.vertex1 = vertex1;
      this.vertex2 = vertex2;
      this.normalAngle = normalAngle;
    }

    public Vertex getVertex1() {
      return vertex1;
    }

    public Vertex getVertex2() {
      return vertex2;
    }

    public double getNormalAngle() {
      return normalAngle;
    }
  }

  enum Vertex {
    NW, NE, SE, SW
  }

  record Material(double softness, DoubleRange areaRatioRange) {

    public static final double SOFTNESS = 0.75d;
    public static final double DEFAULT_AREA_RATIO = .2;

    public Material(double softness, double areaRatioRangeDelta) {
      this(softness, new DoubleRange(1 - Math.abs(areaRatioRangeDelta), 1 + Math.abs(areaRatioRangeDelta)));
    }

    public Material() {
      this(SOFTNESS, DEFAULT_AREA_RATIO);
    }
  }

  Anchor anchorOn(Vertex vertex);

  Collection<Anchor> anchorsOn(Side side);

  Point vertex(Vertex vertex);

  default Segment side(Side side) {
    return new Segment(vertex(side.getVertex1()), vertex(side.getVertex2()));
  }
}
