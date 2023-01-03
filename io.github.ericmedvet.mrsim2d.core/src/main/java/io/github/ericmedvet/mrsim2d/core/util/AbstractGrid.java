package io.github.ericmedvet.mrsim2d.core.util;

import java.util.Objects;

/**
 * @author "Eric Medvet" on 2023/01/03 for 2dmrsim
 */
public abstract class AbstractGrid<T> implements Grid<T> {

  private final int w;
  private final int h;

  public AbstractGrid(int w, int h) {
    this.w = w;
    this.h = h;
  }

  @Override
  public int h() {
    return 0;
  }

  @Override
  public int w() {
    return 0;
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
    return "%s(%dx%d)[%s]".formatted(getClass().getSimpleName(), w(), h(), entries());
  }
}
