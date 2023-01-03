package io.github.ericmedvet.mrsim2d.core.util;

import java.io.Serializable;
import java.util.*;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/**
 * @author "Eric Medvet" on 2023/01/03 for 2dmrsim
 */
public interface Grid<T> extends Iterable<Grid.Entry<T>> {
  char FULL_CELL_CHAR = '█';
  char EMPTY_CELL_CHAR = '░';

  record Entry<V>(Grid.Key key, V value) implements Serializable {}

  record Key(int x, int y) implements Serializable {}

  T get(Key key);

  int h();

  void set(Key key, T t);

  int w();

  static <T> Collector<Grid.Entry<T>, ?, Grid<T>> collector() {
    return Collectors.collectingAndThen(Collectors.toList(), list -> {
      int maxX = list.stream().map(e -> e.key().x()).max(Comparator.comparingInt(v -> v)).orElse(0);
      int maxY = list.stream().map(e -> e.key().y()).max(Comparator.comparingInt(v -> v)).orElse(0);
      Grid<T> grid = Grid.create(maxX + 1, maxY + 1);
      list.forEach(e -> grid.set(e.key(), e.value()));
      return grid;
    });
  }

  static <K> Grid<K> create(int w, int h, K k) {
    return create(w, h, (x, y) -> k);
  }

  static <K> Grid<K> create(
      int w,
      int h,
      BiFunction<Integer, Integer, K> fillerFunction
  ) {
    Grid<K> grid = new ArrayGrid<>(w, h);
    for (int x = 0; x < grid.w(); x++) {
      for (int y = 0; y < grid.h(); y++) {
        grid.set(x, y, fillerFunction.apply(x, y));
      }
    }
    return grid;
  }

  static <K> Grid<K> create(int w, int h) {
    return create(w, h, (K) null);
  }

  static String toString(Grid<Boolean> grid) {
    return toString(grid, (Predicate<Boolean>) b -> b);
  }

  static <K> String toString(Grid<K> grid, Predicate<K> p) {
    return toString(grid, p, "\n");
  }

  static <K> String toString(Grid<K> grid, Predicate<K> p, String separator) {
    return toString(grid, (Grid.Entry<K> e) -> p.test(e.value()) ? FULL_CELL_CHAR : EMPTY_CELL_CHAR, separator);
  }

  static <K> String toString(Grid<K> grid, Function<K, Character> function) {
    return toString(grid, (Grid.Entry<K> e) -> function.apply(e.value()), "\n");
  }

  static <K> String toString(Grid<K> grid, Function<Grid.Entry<K>, Character> function, String separator) {
    StringBuilder sb = new StringBuilder();
    for (int y = 0; y < grid.h(); y++) {
      for (int x = 0; x < grid.w(); x++) {
        sb.append(function.apply(new Grid.Entry<>(new Grid.Key(x, y), grid.get(x, y))));
      }
      if (y < grid.h() - 1) {
        sb.append(separator);
      }
    }
    return sb.toString();
  }

  default List<List<T>> columns() {
    List<List<T>> columns = new ArrayList<>();
    for (int x = 0; x < w(); x++) {
      List<T> column = new ArrayList<>();
      for (int y = 0; y < h(); y++) {
        column.add(get(x, y));
      }
      columns.add(column);
    }
    return columns;
  }

  default Grid<T> copy() {
    return map(Function.identity());
  }

  default List<Entry<T>> entries() {
    return keys().stream().map(k -> new Grid.Entry<>(k, get(k))).toList();
  }

  default T get(int x, int y) {
    Key k = new Key(x, y);
    if (!isValid(k)) {
      throw new IllegalArgumentException("Invalid coords %d,%d on a %dx%d grid".formatted(k.x(), k.y(), w(), h()));
    }
    return get(k);
  }

  @SuppressWarnings("BooleanMethodIsAlwaysInverted")
  default boolean isValid(Key key) {
    return key.x() >= 0 && key.x() < w() && key.y() >= 0 && key.y() < h();
  }

  default Iterator<Grid.Entry<T>> iterator() {
    return entries().iterator();
  }

  default List<Grid.Key> keys() {
    List<Grid.Key> keys = new ArrayList<>(w() * h());
    for (int x = 0; x < w(); x++) {
      for (int y = 0; y < h(); y++) {
        keys.add(new Grid.Key(x, y));
      }
    }
    return Collections.unmodifiableList(keys);
  }

  default <S> Grid<S> map(Function<T, S> function) {
    return entries().stream()
        .map(e -> new Entry<>(e.key(), function.apply(e.value())))
        .collect(Grid.collector());
  }

  default List<List<T>> rows() {
    List<List<T>> rows = new ArrayList<>();
    for (int y = 0; y < h(); y++) {
      List<T> row = new ArrayList<>();
      for (int x = 0; x < w(); x++) {
        row.add(get(x, y));
      }
      rows.add(row);
    }
    return rows;
  }

  default void set(int x, int y, T t) {
    Key k = new Key(x, y);
    if (!isValid(k)) {
      throw new IllegalArgumentException("Invalid coords %d,%d on a %dx%d grid".formatted(k.x(), k.y(), w(), h()));
    }
    set(k, t);
  }

  default Stream<Grid.Entry<T>> stream() {
    return StreamSupport.stream(spliterator(), false);
  }

  default boolean[][] toArray(Predicate<T> p) {
    boolean[][] b = new boolean[w()][h()];
    for (Grid.Entry<T> entry : this) {
      b[entry.key().x()][entry.key().y()] = p.test(entry.value);
    }
    return b;
  }

  default List<T> values() {
    return keys().stream().map(this::get).toList();
  }

}
