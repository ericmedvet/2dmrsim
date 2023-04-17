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

package io.github.ericmedvet.mrsim2d.core.bodies;

import io.github.ericmedvet.jsdynsym.core.DoubleRange;
import io.github.ericmedvet.mrsim2d.core.geometry.Point;
import io.github.ericmedvet.mrsim2d.core.geometry.Segment;

import java.util.Collection;

/**
 * @author "Eric Medvet" on 2022/07/07 for 2dmrsim
 */
public interface Voxel extends SoftBody, Anchorable {
  enum Side {
    N(Vertex.NE, Vertex.NW), E(Vertex.NE, Vertex.SE), W(Vertex.NW, Vertex.SW), S(Vertex.SE, Vertex.SW);
    private final Vertex vertex1;
    private final Vertex vertex2;

    Side(Vertex vertex1, Vertex vertex2) {
      this.vertex1 = vertex1;
      this.vertex2 = vertex2;
    }

    public Vertex getVertex1() {
      return vertex1;
    }

    public Vertex getVertex2() {
      return vertex2;
    }
  }

  enum Vertex {NW, NE, SE, SW}

  record Material(double softness, DoubleRange areaRatioRange) {
    static double SOFTNESS = 0.75d;
    static DoubleRange AREA_RATIO_RANGE = new DoubleRange(0.8, 1.2);

    public Material(double softness, double areaRatioRangeDelta) {
      this(softness, new DoubleRange(1 - Math.abs(areaRatioRangeDelta), 1 + Math.abs(areaRatioRangeDelta)));
    }

    public Material() {
      this(SOFTNESS, AREA_RATIO_RANGE);
    }
  }

  Anchor anchorOn(Vertex vertex);

  Collection<Anchor> anchorsOn(Side side);

  Point vertex(Vertex vertex);

  default Segment side(Side side) {
    return new Segment(vertex(side.getVertex1()), vertex(side.getVertex2()));
  }

}
