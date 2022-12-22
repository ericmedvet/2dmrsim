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

package io.github.ericmedvet.mrsim2d.core;

import io.github.ericmedvet.mrsim2d.core.bodies.Body;
import io.github.ericmedvet.mrsim2d.core.engine.ActionException;
import io.github.ericmedvet.mrsim2d.core.geometry.BoundingBox;
import io.github.ericmedvet.mrsim2d.core.geometry.Point;
import io.github.ericmedvet.mrsim2d.core.geometry.Shape;

import java.util.List;

/**
 * @author "Eric Medvet" on 2022/07/06 for 2dmrsim
 */
public interface EmbodiedAgent extends Agent, Shape {
  void assemble(ActionPerformer actionPerformer) throws ActionException;

  List<Body> bodyParts();

  @Override
  default BoundingBox boundingBox() {
    return bodyParts().stream()
        .map(b -> b.poly().boundingBox())
        .reduce(BoundingBox::enclosing)
        .orElseThrow();
  }

  @Override
  default double area() {
    return bodyParts().stream().mapToDouble(b -> b.poly().area()).sum();
  }

  @Override
  default Point center() {
    //could be weighted by area
    return Point.average(bodyParts().stream().map(b -> b.poly().center()).toArray(Point[]::new));
  }
}
