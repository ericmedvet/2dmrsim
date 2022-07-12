/*
 * Copyright (C) 2021 Eric Medvet <eric.medvet@gmail.com> (as Eric Medvet <eric.medvet@gmail.com>)
 *
 * This program is free software: you can redistribute it and/or modify it
 * under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package it.units.erallab.mrsim.viewer;

import it.units.erallab.mrsim.core.Snapshot;
import it.units.erallab.mrsim.core.geometry.BoundingBox;
import it.units.erallab.mrsim.core.geometry.Point;

import java.util.*;

/**
 * @author Eric Medvet <eric.medvet@gmail.com>
 */
@FunctionalInterface
public interface Framer {

  BoundingBox getFrame(Snapshot snapshot, double ratio);

  default Framer averaged(double windowT) {
    Framer thisFramer = this;
    Map<Double, BoundingBox> memory = new HashMap<>();
    return (snapshot, ratio) -> {
      BoundingBox currentBB = thisFramer.getFrame(snapshot, ratio);
      //update memory
      memory.put(snapshot.t(), currentBB);
      List<Double> toRemoveKeys = memory.keySet().stream().filter(t -> t < snapshot.t() - windowT).toList();
      toRemoveKeys.forEach(memory::remove);
      //average bounding box
      double x = memory.values().stream().mapToDouble(bb -> bb.center().x()).average().orElse(currentBB.center().x());
      double y = memory.values().stream().mapToDouble(bb -> bb.center().y()).average().orElse(currentBB.center().y());
      double w = memory.values().stream().mapToDouble(BoundingBox::width).average().orElse(currentBB.width());
      double h = memory.values().stream().mapToDouble(BoundingBox::height).average().orElse(currentBB.height());
      return new BoundingBox(new Point(x - w / 2d, y - h / 2d), new Point(x + w / 2d, y + h / 2d));
    };
  }
}
