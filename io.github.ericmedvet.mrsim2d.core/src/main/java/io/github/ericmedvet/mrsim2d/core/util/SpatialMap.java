package io.github.ericmedvet.mrsim2d.core.util;

import io.github.ericmedvet.mrsim2d.core.geometry.Point;

import java.util.Collection;

/**
 * @author "Eric Medvet" on 2023/01/21 for 2dmrsim
 */
public interface SpatialMap<T> {
  void add(Point p, T t);

  Collection<T> all();

  void clear();

  Collection<T> get(Point p, double range);

}
