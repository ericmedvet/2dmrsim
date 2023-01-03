package io.github.ericmedvet.mrsim2d.core.util;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * @author "Eric Medvet" on 2023/01/03 for 2dmrsim
 */
public class HashGrid<T> extends AbstractGrid<T> implements Serializable {
  private final Map<Grid.Key, T> map;

  public HashGrid(int w, int h) {
    super(w, h);
    this.map = new HashMap<>(w * h);
  }

  @Override
  public T get(Key key) {
    checkValidity(key);
    return map.get(key);
  }

  @Override
  public void set(Key key, T t) {
    checkValidity(key);
    map.put(key, t);
  }

  @Override
  public int hashCode() {
    return Objects.hash(super.hashCode(), map);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o)
      return true;
    if (o == null || getClass() != o.getClass())
      return false;
    if (!super.equals(o))
      return false;
    HashGrid<?> hashGrid = (HashGrid<?>) o;
    return map.equals(hashGrid.map);
  }
}
