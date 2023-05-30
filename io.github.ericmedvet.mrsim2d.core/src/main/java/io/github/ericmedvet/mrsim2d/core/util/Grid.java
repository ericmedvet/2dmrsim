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

  record Entry<V>(Key key, V value) implements Serializable {
    @Override
    public String toString() {
      return key.toString() + "->" + value.toString();
    }
  }

  record Key(int x, int y) implements Serializable {
    public Key at(int dX, int dY) {
      return new Key(x + dX, y + dY);
    }

    @Override
    public String toString() {
      return "(" + x + ',' + y + ')';
    }
  }

  T get(Key key);

  int h();

  void set(Key key, T t);

  int w();

  static <T> Collector<Entry<T>, ?, Grid<T>> collector() {
    return Collectors.collectingAndThen(Collectors.toList(), list -> {
      int maxX = list.stream().map(e -> e.key().x()).max(Comparator.comparingInt(v -> v)).orElse(0);
      int maxY = list.stream().map(e -> e.key().y()).max(Comparator.comparingInt(v -> v)).orElse(0);
      Grid<T> grid = create(maxX + 1, maxY + 1);
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
      Function<Key, K> fillerFunction
  ) {
    Grid<K> grid = new ArrayGrid<>(w, h);
    grid.keys().forEach(k -> grid.set(k, fillerFunction.apply(k)));
    return grid;
  }

  static <K> Grid<K> create(int w, int h, List<K> ks) {
    if (ks.size() != w * h) {
      throw new IllegalArgumentException("Wrong list size: %d x %d = %d expected, %d found".formatted(
          w,
          h,
          w * h,
          ks.size()
      ));
    }
    return create(w, h, (x, y) -> ks.get(y + w * x));
  }

  static <K> Grid<K> create(
      int w,
      int h,
      BiFunction<Integer, Integer, K> fillerFunction
  ) {
    return create(w, h, k -> fillerFunction.apply(k.x(), k.y()));
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
    return toString(grid, (Entry<K> e) -> p.test(e.value()) ? FULL_CELL_CHAR : EMPTY_CELL_CHAR, separator);
  }

  static <K> String toString(Grid<K> grid, Function<K, Character> function) {
    return toString(grid, (Entry<K> e) -> function.apply(e.value()), "\n");
  }

  static <K> String toString(Grid<K> grid, Function<Entry<K>, Character> function, String separator) {
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
    return keys().stream().map(k -> new Entry<>(k, get(k))).toList();
  }

  default T get(int x, int y) {
    return get(new Key(x, y));
  }

  @SuppressWarnings("BooleanMethodIsAlwaysInverted")
  default boolean isValid(Key key) {
    return key.x() >= 0 && key.x() < w() && key.y() >= 0 && key.y() < h();
  }

  default Iterator<Entry<T>> iterator() {
    return entries().iterator();
  }

  default List<Key> keys() {
    List<Key> keys = new ArrayList<>(w() * h());
    for (int x = 0; x < w(); x++) {
      for (int y = 0; y < h(); y++) {
        keys.add(new Key(x, y));
      }
    }
    return Collections.unmodifiableList(keys);
  }

  default <S> Grid<S> map(Function<T, S> function) {
    return map((k, t) -> function.apply(t));
  }

  default <S> Grid<S> map(BiFunction<Key, T, S> function) {
    return entries().stream()
        .map(e -> new Entry<>(e.key(), function.apply(e.key(), e.value())))
        .collect(collector());
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
    set(new Key(x, y), t);
  }

  default Stream<Entry<T>> stream() {
    return StreamSupport.stream(spliterator(), false);
  }

  default boolean[][] toArray(Predicate<T> p) {
    boolean[][] b = new boolean[w()][h()];
    for (Entry<T> entry : this) {
      b[entry.key().x()][entry.key().y()] = p.test(entry.value);
    }
    return b;
  }

  default List<T> values() {
    return keys().stream().map(this::get).toList();
  }

  private static <K> Grid<Integer> partitionGrid(Grid<K> kGrid, Predicate<K> p) {
    Grid<Integer> iGrid = Grid.create(kGrid.w(), kGrid.h());
    for (int x = 0; x < kGrid.w(); x++) {
      for (int y = 0; y < kGrid.h(); y++) {
        if (iGrid.get(x, y) == null) {
          int index = iGrid.values().stream().filter(Objects::nonNull).mapToInt(i -> i).max().orElse(0);
          partitionGrid(x, y, index + 1, kGrid, iGrid, p);
        }
      }
    }
    return iGrid;
  }

  private static <K> void partitionGrid(int x, int y, int i, Grid<K> kGrid, Grid<Integer> iGrid, Predicate<K> p) {
    boolean hereFilled = p.test(kGrid.get(x, y));
    //already done
    if (iGrid.get(x, y) != null) {
      return;
    }
    //filled but not done
    if (hereFilled) {
      iGrid.set(x, y, i);
      //expand east
      if (x > 0) {
        partitionGrid(x - 1, y, i, kGrid, iGrid, p);
      }
      //expand west
      if (x < kGrid.w() - 1) {
        partitionGrid(x + 1, y, i, kGrid, iGrid, p);
      }
      //expand north
      if (y > 0) {
        partitionGrid(x, y - 1, i, kGrid, iGrid, p);
      }
      //expand south
      if (y < kGrid.h() - 1) {
        partitionGrid(x, y + 1, i, kGrid, iGrid, p);
      }
    }
  }

  default Grid<T> largestConnected(Predicate<T> p, T emptyT) {
    Grid<Integer> iGrid = partitionGrid(this, p);
    //count elements per partition
    Map<Integer, Integer> counts = new LinkedHashMap<>();
    for (Integer i : iGrid.values()) {
      if (i != null) {
        counts.put(i, counts.getOrDefault(i, 0) + 1);
      }
    }
    //find largest
    Integer maxIndex = counts.entrySet().stream()
        .max(Comparator.comparingInt(Map.Entry::getValue))
        .map(Map.Entry::getKey)
        .orElse(null);
    //filter map
    return map((k, t) -> (iGrid.get(k) != null && iGrid.get(k).equals(maxIndex)) ? get(k) : emptyT);
  }

}
