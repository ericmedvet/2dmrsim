package io.github.ericmedvet.mrsim2d.core.util;

import io.github.ericmedvet.mrsim2d.core.geometry.Point;

import java.util.Collection;
public interface SpatialMap<T> {
  void add(Point p, T t);

  Collection<T> all();

  void clear();

  Collection<T> get(Point p, double range);

}
