/*-
 * ========================LICENSE_START=================================
 * mrsim2d-viewer
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

package io.github.ericmedvet.mrsim2d.viewer.drawers;

import io.github.ericmedvet.mrsim2d.core.Snapshot;
import io.github.ericmedvet.mrsim2d.core.geometry.BoundingBox;
import io.github.ericmedvet.mrsim2d.core.geometry.Point;
import io.github.ericmedvet.mrsim2d.viewer.Drawer;
import java.awt.Graphics2D;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;

public class StackedMultipliedDrawer<T> implements Drawer {
  private static final double MARGIN_RATIO = 0.01;
  private final Supplier<Drawer> innerDrawerSupplier;
  private final Function<Snapshot, List<Snapshot>> multiplier;

  private final double widthRatio;
  private final double heightRatio;
  private final Direction direction;
  private final VerticalPosition verticalPosition;
  private final HorizontalPosition horizontalPosition;
  private final List<Drawer> drawers;

  public StackedMultipliedDrawer(
      Supplier<Drawer> innerDrawerSupplier,
      Function<Snapshot, List<Snapshot>> multiplier,
      double widthRatio,
      double heightRatio,
      Direction direction,
      VerticalPosition verticalPosition,
      HorizontalPosition horizontalPosition
  ) {
    this.innerDrawerSupplier = innerDrawerSupplier;
    this.multiplier = multiplier;
    this.widthRatio = widthRatio;
    this.heightRatio = heightRatio;
    this.direction = direction;
    this.drawers = new ArrayList<>();
    this.verticalPosition = verticalPosition;
    this.horizontalPosition = horizontalPosition;
  }

  @Override
  public boolean draw(List<Snapshot> snapshots, Graphics2D g) {
    // build list of seqs of snapshots
    List<List<Snapshot>> multiplied = snapshots.stream().map(multiplier).toList();
    int lastSize = multiplied.getLast().size();
    multiplied = multiplied.stream().filter(l -> l.size() == lastSize).toList();
    List<List<Snapshot>> lists = new ArrayList<>(lastSize);
    for (int i = 0; i < lastSize; i++) {
      int finalI = i;
      lists.add(multiplied.stream().map(l -> l.get(finalI)).toList());
    }
    // possibly rebuild drawers
    if (drawers.size() != lists.size()) {
      drawers.clear();
      lists.forEach(l -> drawers.add(innerDrawerSupplier.get()));
    }
    // prepare bounding boxes
    double nOfChildren = lists.size();
    double bbW = switch (direction) {
      case VERTICAL -> widthRatio;
      case HORIZONTAL -> widthRatio * nOfChildren + MARGIN_RATIO * (nOfChildren - 1);
    };
    double bbH = switch (direction) {
      case VERTICAL -> heightRatio * nOfChildren + MARGIN_RATIO * (nOfChildren - 1);
      case HORIZONTAL -> heightRatio;
    };
    double x = switch (horizontalPosition) {
      case LEFT -> MARGIN_RATIO;
      case RIGHT -> 1d - bbW - MARGIN_RATIO;
    };
    double y = switch (verticalPosition) {
      case TOP -> MARGIN_RATIO;
      case BOTTOM -> 1d - bbH - MARGIN_RATIO;
    };
    // iterate
    boolean drawn = false;
    for (int i = 0; i < lists.size(); i++) {
      BoundingBox bb = new BoundingBox(new Point(x, y), new Point(x + widthRatio, y + heightRatio));
      List<Snapshot> localSnapshots = lists.get(i);
      boolean localDrawn = Drawer.clip(bb, drawers.get(i)).draw(localSnapshots, g);
      drawn = drawn || localDrawn;
      if (direction.equals(Direction.VERTICAL)) {
        y = bb.max().y() + MARGIN_RATIO;
      } else {
        x = bb.max().x() + MARGIN_RATIO;
      }
    }
    return drawn;
  }
}
