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

package io.github.ericmedvet.mrsim2d.viewer.framers;

import io.github.ericmedvet.mrsim2d.core.EmbodiedAgent;
import io.github.ericmedvet.mrsim2d.core.Snapshot;
import io.github.ericmedvet.mrsim2d.core.geometry.BoundingBox;

/**
 * @author "Eric Medvet" on 2022/07/07 for 2dmrsim
 */
public class AllAgentsFramer extends AbstractFramer<Snapshot> {

  public AllAgentsFramer(double sizeRelativeMargin) {
    super(sizeRelativeMargin);
  }

  @Override
  protected BoundingBox getCurrentBoundingBox(Snapshot snapshot) {
    return snapshot.agents().stream()
        .filter(a -> a instanceof EmbodiedAgent)
        .map(a -> ((EmbodiedAgent) a).bodyParts().stream()
            .map(b -> b.poly().boundingBox())
            .reduce(BoundingBox::enclosing)
            .orElse(DEFAULT_BOUNDING_BOX)
        )
        .reduce(BoundingBox::enclosing)
        .orElse(DEFAULT_BOUNDING_BOX);
  }
}
