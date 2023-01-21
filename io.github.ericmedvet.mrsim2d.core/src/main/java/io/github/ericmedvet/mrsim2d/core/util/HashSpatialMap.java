package io.github.ericmedvet.mrsim2d.core.util;

import io.github.ericmedvet.mrsim2d.core.geometry.Point;

import java.util.*;

/**
 * @author "Eric Medvet" on 2023/01/21 for 2dmrsim
 */
public class HashSpatialMap<T> implements SpatialMap<T> {

  private final double cellSize;
  private final Map<Key, List<PositionedT<T>>> map;

  public HashSpatialMap(double cellSize) {
    this.cellSize = cellSize;
    map = new LinkedHashMap<>();
  }

  private record Key(int x, int y) {}

  private record PositionedT<T>(Point p, T t) {}

  @Override
  public void add(Point p, T t) {
    Key key = key(p);
    List<PositionedT<T>> ts = map.computeIfAbsent(key, k -> new ArrayList<>());
    ts.add(new PositionedT<>(p, t));
  }

  @Override
  public void clear() {
    map.clear();
  }

  @Override
  public Collection<T> get(Point p, double range) {
    return neighborhood(key(p), range).stream()
        .map(k -> map.getOrDefault(k, List.of()))
        .flatMap(List::stream)
        .filter(v -> v.p().distance(p) <= range)
        .map(PositionedT::t)
        .toList();
  }

  private Key key(Point p) {
    return new Key((int) Math.floor(p.x() / cellSize), (int) Math.floor(p.y() / cellSize));
  }

  private List<Key> neighborhood(Key center, double range) {
    int keyRadius = (int) Math.ceil(range / cellSize);
    List<Key> keys = new ArrayList<>();
    for (int x = center.x() - keyRadius; x <= center.x() + keyRadius; x = x + 1) {
      for (int y = center.y() - keyRadius; y <= center.y() + keyRadius; y = y + 1) {
        keys.add(new Key(x, y));
      }
    }
    return keys;
  }

  @Override
  public String toString() {
    return "HashSpatialMap{" +
        "cellSize=" + cellSize +
        ", map=" + map +
        '}';
  }
}
