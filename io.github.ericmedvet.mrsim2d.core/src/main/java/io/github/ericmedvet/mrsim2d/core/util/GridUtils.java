package io.github.ericmedvet.mrsim2d.core.util;

import java.util.*;
import java.util.function.Predicate;

public class GridUtils {

  private final static int ELONGATION_STEPS = 20;

  private GridUtils() {
  }

  public static <T> double compactness(Grid<T> g, Predicate<T> p) {
    // approximate convex hull
    Grid<Boolean> convexHull = g.map(p::test);
    boolean none = false;
    // loop as long as there are false cells have at least five of the eight Moore neighbors as true
    while (!none) {
      none = true;
      for (Grid.Entry<Boolean> entry : convexHull) {
        if (convexHull.get(entry.key().x(), entry.key().y())) {
          continue;
        }
        int currentX = entry.key().x();
        int currentY = entry.key().y();
        int adjacentCount = 0;
        // count how many of the Moore neighbors are true
        for (int i : new int[]{1, -1}) {
          int neighborX = currentX;
          int neighborY = currentY + i;
          if (0 <= neighborY && neighborY < convexHull.h() && convexHull.get(neighborX, neighborY)) {
            adjacentCount += 1;
          }
          neighborX = currentX + i;
          neighborY = currentY;
          if (0 <= neighborX && neighborX < convexHull.w() && convexHull.get(neighborX, neighborY)) {
            adjacentCount += 1;
          }
          neighborX = currentX + i;
          neighborY = currentY + i;
          if (0 <= neighborX && 0 <= neighborY && neighborX < convexHull.w() && neighborY < convexHull.h() && convexHull.get(
              neighborX,
              neighborY
          )) {
            adjacentCount += 1;
          }
          neighborX = currentX + i;
          neighborY = currentY - i;
          if (0 <= neighborX && 0 <= neighborY && neighborX < convexHull.w() && neighborY < convexHull.h() && convexHull.get(
              neighborX,
              neighborY
          )) {
            adjacentCount += 1;
          }
        }
        // if at least five, fill the cell
        if (adjacentCount >= 5) {
          convexHull.set(entry.key().x(), entry.key().y(), true);
          none = false;
        }
      }
    }
    // compute are ratio between convex hull and posture
    double nVoxels = count(g, p);
    double nConvexHull = count(convexHull, b -> b);
    // -> 0.0 for less compact shapes, -> 1.0 for more compact shapes
    return nVoxels / nConvexHull;
  }

  public static <T> int count(Grid<T> g, Predicate<T> p) {
    return (int) g.values().stream().filter(p).count();
  }

  public static <T> double elongation(Grid<T> g, Predicate<T> p) {
    return elongation(g, p, ELONGATION_STEPS);
  }

  public static <T> double elongation(Grid<T> g, Predicate<T> p, int n) {
    if (g.values().stream().noneMatch(p)) {
      throw new IllegalArgumentException("Grid is empty");
    } else if (n <= 0) {
      throw new IllegalArgumentException(String.format("Non-positive number of directions provided: %d", n));
    }
    List<Grid.Key> coordinates = g.stream()
        .filter(e -> p.test(e.value()))
        .map(Grid.Entry::key)
        .toList();
    List<Double> diameters = new ArrayList<>();
    for (int i = 0; i < n; ++i) {
      double theta = (2 * i * Math.PI) / n;
      List<Grid.Key> rotatedCoordinates = coordinates.stream()
          .map(k -> new Grid.Key(
              (int) Math.round(k.x() * Math.cos(theta) - k.y() * Math.sin(theta)),
              (int) Math.round(k.x() * Math.sin(theta) + k.y() * Math.cos(theta))
          ))
          .toList();
      double minX = rotatedCoordinates.stream().min(Comparator.comparingInt(Grid.Key::x)).orElseThrow().x();
      double maxX = rotatedCoordinates.stream().max(Comparator.comparingInt(Grid.Key::x)).orElseThrow().x();
      double minY = rotatedCoordinates.stream().min(Comparator.comparingInt(Grid.Key::y)).orElseThrow().y();
      double maxY = rotatedCoordinates.stream().max(Comparator.comparingInt(Grid.Key::y)).orElseThrow().y();
      double sideX = maxX - minX + 1;
      double sideY = maxY - minY + 1;
      diameters.add(Math.min(sideX, sideY) / Math.max(sideX, sideY));
    }
    return 1.0 - Collections.min(diameters);
  }

  public static <T> Grid<T> fit(Grid<T> g, Predicate<T> p) {
    int minX = g.entries().stream().filter(e -> p.test(e.value())).mapToInt(e -> e.key().x()).min().orElseThrow();
    int maxX = g.entries().stream().filter(e -> p.test(e.value())).mapToInt(e -> e.key().x()).max().orElseThrow();
    int minY = g.entries().stream().filter(e -> p.test(e.value())).mapToInt(e -> e.key().y()).min().orElseThrow();
    int maxY = g.entries().stream().filter(e -> p.test(e.value())).mapToInt(e -> e.key().y()).max().orElseThrow();
    return Grid.create(maxX - minX + 1, maxY - minY + 1, (x, y) -> g.get(x - minX, y - minY));
  }

  public static <T> int h(Grid<T> g, Predicate<T> p) {
    return fit(g, p).h();
  }

  public static <T> Grid<T> largestConnected(Grid<T> g, Predicate<T> p, T emptyT) {
    Grid<Integer> iGrid = partitionGrid(g, p);
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
    return g.map((k, t) -> (iGrid.get(k) != null && iGrid.get(k).equals(maxIndex)) ? g.get(k) : emptyT);
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

  public static <T> int w(Grid<T> g, Predicate<T> p) {
    return fit(g, p).w();
  }

}
