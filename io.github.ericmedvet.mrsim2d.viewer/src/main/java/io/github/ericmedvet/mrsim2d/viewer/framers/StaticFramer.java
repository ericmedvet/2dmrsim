/*-
 * ========================LICENSE_START=================================
 * mrsim2d-viewer
 * %%
 * Copyright (C) 2020 - 2024 Eric Medvet
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

package io.github.ericmedvet.mrsim2d.viewer.framers;

import io.github.ericmedvet.mrsim2d.core.Snapshot;
import io.github.ericmedvet.mrsim2d.core.geometry.BoundingBox;

public class StaticFramer extends AbstractFramer<Snapshot> {

  private BoundingBox initialBoundingBox;

  public StaticFramer(BoundingBox initialBoundingBox) {
    super(1d);
    this.initialBoundingBox = initialBoundingBox;
  }

  public StaticFramer(double sizeRelativeMargin) {
    super(sizeRelativeMargin);
  }

  @Override
  protected BoundingBox getCurrentBoundingBox(Snapshot snapshot) {
    if (initialBoundingBox == null) {
      initialBoundingBox = snapshot.bodies()
          .stream()
          .map(b -> b.poly().boundingBox())
          .reduce(BoundingBox::enclosing)
          .orElse(DEFAULT_BOUNDING_BOX);
    }
    return initialBoundingBox;
  }
}
