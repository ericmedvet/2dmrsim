/*
 * Copyright 2022 eric
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

package it.units.erallab.mrsim2d.viewer.drawers;

import it.units.erallab.mrsim2d.core.Snapshot;
import it.units.erallab.mrsim2d.core.geometry.BoundingBox;
import it.units.erallab.mrsim2d.core.geometry.Point;
import it.units.erallab.mrsim2d.viewer.Drawer;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;

public class StackedMultipliedDrawer<T> implements Drawer {
  private final Supplier<Drawer> innerDrawerSupplier;
  private final Function<Snapshot, List<Snapshot>> multiplier;
  private final BoundingBox boundingBox;
  private final Direction direction;
  private final List<Drawer> drawers;

  public StackedMultipliedDrawer(
      Supplier<Drawer> innerDrawerSupplier,
      Function<Snapshot, List<Snapshot>> multiplier,
      BoundingBox boundingBox,
      Direction direction
  ) {
    this.innerDrawerSupplier = innerDrawerSupplier;
    this.multiplier = multiplier;
    this.boundingBox = boundingBox;
    this.direction = direction;
    this.drawers = new ArrayList<>();
  }

  public enum Direction {HORIZONTAL, VERTICAL}

  @Override
  public boolean draw(List<Snapshot> snapshots, Graphics2D g) {
    //build list of seqs of snapshots
    List<List<Snapshot>> lists = new ArrayList<>();
    for (Snapshot snapshot : snapshots) {
      List<Snapshot> multiplied = multiplier.apply(snapshot);
      if (drawers.isEmpty()) {
        multiplied.forEach(s -> drawers.add(innerDrawerSupplier.get()));
      }
      if (lists.isEmpty()) {
        multiplied.forEach(s -> lists.add(new ArrayList<>()));
      }
      for (int i = 0; i < multiplied.size(); i++) {
        lists.get(i).add(multiplied.get(i));
      }
    }
    boolean drawn = false;
    //iterate
    BoundingBox bb = boundingBox;
    for (int i = 0; i < lists.size(); i++) {
      List<Snapshot> localSnapshots = lists.get(i);
      boolean localDrawn = Drawer.clip(bb, drawers.get(i)).draw(localSnapshots, g);
      drawn = drawn || localDrawn;
      if (direction.equals(Direction.VERTICAL)) {
        bb = new BoundingBox(bb.min().sum(new Point(0, bb.height())), bb.max().sum(new Point(0, bb.height())));
      } else {
        bb = new BoundingBox(bb.min().sum(new Point(bb.width(), 0)), bb.max().sum(new Point(bb.height(), 0)));
      }
    }
    return drawn;
  }
}
