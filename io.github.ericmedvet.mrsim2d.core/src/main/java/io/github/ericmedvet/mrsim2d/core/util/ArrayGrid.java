package io.github.ericmedvet.mrsim2d.core.util;

import java.io.Serializable;
import java.util.Arrays;

/**
 * @author "Eric Medvet" on 2023/01/03 for 2dmrsim
 */
public class ArrayGrid<T> extends AbstractGrid<T> implements Serializable {

  private final Object[] ts;

  public ArrayGrid(int w, int h) {
    super(w,h);
    this.ts = new Object[w * h];
  }

  @Override
  public T get(Key key) {
    if (!isValid(key)) {
      throw new IllegalArgumentException("Invalid coords %d,%d on a %dx%d grid".formatted(key.x(), key.y(), w(), h()));
    }
    //noinspection unchecked
    return (T) ts[(key.y() * w()) + key.x()];
  }

  @Override
  public void set(Key key, T t) {
    if (!isValid(key)) {
      throw new IllegalArgumentException("Invalid coords %d,%d on a %dx%d grid".formatted(key.x(), key.y(), w(), h()));
    }
    ts[(key.y() * w()) + key.x()] = t;
  }

  @Override
  public int hashCode() {
    int result = super.hashCode();
    result = 31 * result + Arrays.hashCode(ts);
    return result;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o)
      return true;
    if (o == null || getClass() != o.getClass())
      return false;
    if (!super.equals(o))
      return false;
    ArrayGrid<?> arrayGrid = (ArrayGrid<?>) o;
    return Arrays.equals(ts, arrayGrid.ts);
  }


}
