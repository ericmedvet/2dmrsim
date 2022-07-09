/*
 * Copyright (C) 2020 Eric Medvet <eric.medvet@gmail.com> (as Eric Medvet <eric.medvet@gmail.com>)
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
package it.units.erallab.mrsim.util;

import java.io.Serializable;
import java.util.*;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/**
 * @author Eric Medvet <eric.medvet@gmail.com>
 */
public class Grid<T> implements Iterable<Grid.Entry<T>>, Serializable {

  private final static char FULL_CELL_CHAR = '█';
  private final static char EMPTY_CELL_CHAR = '░';
  private final Object[] ts;
  private final int w;
  private final int h;

  private Grid(int w, int h) {
    this.w = w;
    this.h = h;
    this.ts = new Object[w * h];
  }

  public record Entry<V>(Key key, V value) implements Serializable {}

  private static final class GridIterator<V> implements Iterator<Entry<V>> {

    private final Grid<V> grid;
    private int c = 0;

    public GridIterator(Grid<V> grid) {
      this.grid = grid;
    }

    @Override
    public boolean hasNext() {
      return c < grid.w * grid.h;
    }

    @Override
    public Entry<V> next() {
      int y = Math.floorDiv(c, grid.w);
      int x = c % grid.w;
      c = c + 1;
      return new Entry<>(new Key(x, y), grid.get(x, y));
    }

  }

  public record Key(int x, int y) implements Serializable {}

  public static <T> Grid<T> copy(Grid<T> other) {
    Grid<T> grid = Grid.create(other);
    for (int x = 0; x < grid.w; x++) {
      for (int y = 0; y < grid.h; y++) {
        grid.set(x, y, other.get(x, y));
      }
    }
    return grid;
  }

  public static <S, T> Grid<T> create(Grid<S> source, Function<S, T> transformerFunction) {
    Grid<T> target = Grid.create(source);
    for (Entry<S> entry : source) {
      target.set(entry.key().x(), entry.key().y(), transformerFunction.apply(entry.value()));
    }
    return target;
  }

  public static <K> Grid<K> create(int w, int h, K k) {
    return create(w, h, (x, y) -> k);
  }

  public static <K> Grid<K> create(int w, int h, BiFunction<Integer, Integer, K> fillerFunction) {
    Grid<K> grid = new Grid<>(w, h);
    for (int x = 0; x < grid.w(); x++) {
      for (int y = 0; y < grid.h(); y++) {
        grid.set(x, y, fillerFunction.apply(x, y));
      }
    }
    return grid;
  }

  public static <K> Grid<K> create(int w, int h) {
    return create(w, h, (K) null);
  }

  public static <K> Grid<K> create(Grid<?> other) {
    return create(other.w(), other.h());
  }

  public static <K> String toString(Grid<K> grid, String format) {
    StringBuilder sb = new StringBuilder();
    for (int y = 0; y < grid.h(); y++) {
      for (int x = 0; x < grid.w(); x++) {
        sb.append(String.format(format, grid.get(x, y)));
      }
      if (y < grid.h() - 1) {
        sb.append(String.format("%n"));
      }
    }
    return sb.toString();
  }

  public static String toString(Grid<Boolean> grid) {
    return toString(grid, (Predicate<Boolean>) b -> b);
  }

  public static <K> String toString(Grid<K> grid, Predicate<K> p) {
    return toString(grid, p, "\n");
  }

  public static <K> String toString(Grid<K> grid, Predicate<K> p, String separator) {
    return toString(grid, (Entry<K> e) -> p.test(e.value()) ? FULL_CELL_CHAR : EMPTY_CELL_CHAR, separator);
  }

  public static <K> String toString(Grid<K> grid, Function<K, Character> function) {
    return toString(grid, (Entry<K> e) -> function.apply(e.value()), "\n");
  }

  public static <K> String toString(Grid<K> grid, Function<Entry<K>, Character> function, String separator) {
    StringBuilder sb = new StringBuilder();
    for (int y = 0; y < grid.h(); y++) {
      for (int x = 0; x < grid.w(); x++) {
        sb.append(function.apply(new Entry<>(new Key(x, y), grid.get(x, y))));
      }
      if (y < grid.h() - 1) {
        sb.append(separator);
      }
    }
    return sb.toString();
  }

  public List<List<T>> columns() {
    List<List<T>> columns = new ArrayList<>();
    for (int x = 0; x < w; x++) {
      List<T> column = new ArrayList<>();
      for (int y = 0; y < h; y++) {
        column.add(get(x, y));
      }
      columns.add(column);
    }
    return columns;
  }

  public long count(Predicate<T> predicate) {
    return values().stream().filter(predicate).count();
  }

  @SuppressWarnings("unchecked")
  public T get(int x, int y) {
    if ((x < 0) || (x >= w)) {
      return null;
    }
    if ((y < 0) || (y >= h)) {
      return null;
    }
    return (T) ts[(y * w) + x];
  }

  public T get(Key key) {
    return get(key.x, key.y);
  }

  public int h() {
    return h;
  }

  public int w() {
    return w;
  }

  @Override
  public int hashCode() {
    int result = Objects.hash(w, h);
    result = 31 * result + Arrays.hashCode(ts);
    return result;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o)
      return true;
    if (o == null || getClass() != o.getClass())
      return false;
    Grid<?> grid = (Grid<?>) o;
    return w == grid.w && h == grid.h && Arrays.equals(ts, grid.ts);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("[");
    for (int y = 0; y < h; y++) {
      for (int x = 0; x < w; x++) {
        sb.append(String.format("(%d,%d)=%s", x, y, get(x, y)));
        if ((x < (w - 1)) && (y < (h - 1))) {
          sb.append(", ");
        }
      }
    }
    sb.append("]");
    return sb.toString();
  }

  @Override
  public Iterator<Entry<T>> iterator() {
    return new GridIterator<>(this);
  }

  public List<List<T>> rows() {
    List<List<T>> rows = new ArrayList<>();
    for (int y = 0; y < h; y++) {
      List<T> row = new ArrayList<>();
      for (int x = 0; x < w; x++) {
        row.add(get(x, y));
      }
      rows.add(row);
    }
    return rows;
  }

  public void set(int x, int y, T t) {
    if (x < 0 || x >= w || y < 0 || y >= h) {
      throw new IllegalArgumentException(String.format("Cannot set element at %d,%d on a %dx%d grid", x, y, w, h));
    }
    ts[(y * w) + x] = t;
  }

  public void set(Key key, T t) {
    set(key.x, key.y, t);
  }

  public Stream<Entry<T>> stream() {
    return StreamSupport.stream(spliterator(), false);
  }

  public boolean[][] toArray(Predicate<T> p) {
    boolean[][] b = new boolean[w][h];
    for (Entry<T> entry : this) {
      b[entry.key().x()][entry.key().y()] = p.test(entry.value);
    }
    return b;
  }

  @SuppressWarnings("unchecked")
  public List<T> values() {
    return Arrays.stream(ts).map(o -> (T) o).toList();
  }

  public List<Key> keys() {
    List<Key> keys = new ArrayList<>(w * h);
    for (int x = 0; x < w; x++) {
      for (int y = 0; y < h; y++) {
        keys.add(new Key(x, y));
      }
    }
    return Collections.unmodifiableList(keys);
  }

  public List<Entry<T>> entries() {
    List<Entry<T>> entries = new ArrayList<>(w * h);
    for (int x = 0; x < w; x++) {
      for (int y = 0; y < h; y++) {
        entries.add(new Entry<>(new Key(x, y), get(x, y)));
      }
    }
    return Collections.unmodifiableList(entries);
  }

  public <R> Grid<R> map(Function<T, R> f) {
    return Grid.create(this, f);
  }

}
