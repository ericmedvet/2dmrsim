package io.github.ericmedvet.mrsim2d.viewer;

import io.github.ericmedvet.mrsim2d.core.geometry.BoundingBox;
import io.github.ericmedvet.mrsim2d.core.geometry.Point;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
@FunctionalInterface
public interface Framer<K> {

  BoundingBox getFrame(double t, K k, double ratio);

  default Framer<K> averaged(double windowT) {
    Framer<K> thisFramer = this;
    Map<Double, BoundingBox> memory = new HashMap<>();
    return (t, k, ratio) -> {
      BoundingBox currentBB = thisFramer.getFrame(t, k, ratio);
      //update memory
      memory.put(t, currentBB);
      List<Double> toRemoveKeys = memory.keySet().stream().filter(ot -> ot < t - windowT).toList();
      toRemoveKeys.forEach(memory::remove);
      //average bounding box
      double x = memory.values().stream().mapToDouble(bb -> bb.center().x()).average().orElse(currentBB.center().x());
      double y = memory.values().stream().mapToDouble(bb -> bb.center().y()).average().orElse(currentBB.center().y());
      double w = memory.values().stream().mapToDouble(BoundingBox::width).average().orElse(currentBB.width());
      double h = memory.values().stream().mapToDouble(BoundingBox::height).average().orElse(currentBB.height());
      return new BoundingBox(new Point(x - w / 2d, y - h / 2d), new Point(x + w / 2d, y + h / 2d));
    };
  }

  default Framer<K> largest(double windowT) {
    Framer<K> thisFramer = this;
    Map<Double, BoundingBox> memory = new HashMap<>();
    return (t, k, ratio) -> {
      BoundingBox currentBB = thisFramer.getFrame(t, k, ratio);
      //update memory
      memory.put(t, currentBB);
      List<Double> toRemoveKeys = memory.keySet().stream().filter(ot -> ot < t - windowT).toList();
      toRemoveKeys.forEach(memory::remove);
      //take largest bounding box
      return memory.values().stream().reduce(BoundingBox::enclosing).orElse(currentBB);
    };
  }
}
