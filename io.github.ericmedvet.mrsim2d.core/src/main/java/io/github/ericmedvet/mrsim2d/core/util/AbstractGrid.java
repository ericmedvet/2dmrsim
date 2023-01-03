package io.github.ericmedvet.mrsim2d.core.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * @author "Eric Medvet" on 2023/01/03 for 2dmrsim
 */
public abstract class AbstractGrid<T> implements Grid<T> {

  private final int w;
  private final int h;
  private final List<Key> keys;

  public AbstractGrid(int w, int h) {
    this.w = w;
    this.h = h;
    List<Key> localKeys = new ArrayList<>(w() * h());
    for (int x = 0; x < w(); x++) {
      for (int y = 0; y < h(); y++) {
        localKeys.add(new Key(x, y));
      }
    }
    keys = Collections.unmodifiableList(localKeys);
  }

  protected void checkValidity(Key key) {
    if (!isValid(key)) {
      throw new IllegalArgumentException("Invalid coords (%d,%d) on a %dx%d grid".formatted(
          key.x(),
          key.y(),
          w(),
          h()
      ));
    }
  }

  @Override
  public int h() {
    return h;
  }

  @Override
  public int w() {
    return w;
  }

  @Override
  public List<Key> keys() {
    return keys;
  }

  @Override
  public int hashCode() {
    return Objects.hash(w, h);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o)
      return true;
    if (o == null || getClass() != o.getClass())
      return false;
    AbstractGrid<?> that = (AbstractGrid<?>) o;
    return w == that.w && h == that.h;
  }

  @Override
  public String toString() {
    return "%s(%dx%d)%s".formatted(getClass().getSimpleName(), w(), h(), entries());
  }

}
