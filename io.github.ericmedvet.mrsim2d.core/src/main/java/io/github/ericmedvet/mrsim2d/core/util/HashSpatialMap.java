/*-
 * ========================LICENSE_START=================================
 * mrsim2d-core
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
package io.github.ericmedvet.mrsim2d.core.util;

import io.github.ericmedvet.mrsim2d.core.geometry.Point;
import java.util.*;

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
  public Collection<T> all() {
    return map.values()
        .stream()
        .filter(Objects::nonNull)
        .flatMap(List::stream)
        .map(PositionedT::t)
        .toList();
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
    return "HashSpatialMap{" + "cellSize=" + cellSize + ", map=" + map + '}';
  }
}
