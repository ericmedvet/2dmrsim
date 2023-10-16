
package io.github.ericmedvet.mrsim2d.buildable.builders;

import io.github.ericmedvet.jnb.core.Param;
import io.github.ericmedvet.jsdynsym.grid.Grid;
import io.github.ericmedvet.mrsim2d.core.Sensor;
import io.github.ericmedvet.mrsim2d.core.bodies.Voxel;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.function.Function;
public class VSRSensorizingFunctions {

  @SuppressWarnings("unused")
  public static Function<Grid<Boolean>, Grid<List<Sensor<? super Voxel>>>> directional(
      @Param(value = "nSensors") List<Sensor<? super Voxel>> nSensors,
      @Param(value = "eSensors") List<Sensor<? super Voxel>> eSensors,
      @Param(value = "sSensors") List<Sensor<? super Voxel>> sSensors,
      @Param(value = "wSensors") List<Sensor<? super Voxel>> wSensors,
      @Param(value = "headSensors") List<Sensor<? super Voxel>> headSensors
  ) {
    return shape -> Grid.create(shape.w(), shape.h(), (Integer x, Integer y) -> {
      if (!shape.get(x, y)) {
        return null;
      }
      int maxX = shape.entries()
          .stream()
          .filter(e -> e.key().y() == y && e.value())
          .mapToInt(e -> e.key().x())
          .max()
          .orElse(0);
      int minX = shape.entries()
          .stream()
          .filter(e -> e.key().y() == y && e.value())
          .mapToInt(e -> e.key().x())
          .min()
          .orElse(0);
      Grid.Key headKey = shape.entries().stream()
          .filter(Grid.Entry::value)
          .sorted(Comparator.comparingInt(e -> -e.key().x() - e.key().y()))
          .limit(1)
          .toList()
          .get(0)
          .key();
      List<Sensor<? super Voxel>> localSensors = new ArrayList<>();
      if (x == maxX) {
        localSensors.addAll(eSensors);
      }
      if (x == minX) {
        localSensors.addAll(wSensors);
      }
      if (y == 0) {
        localSensors.addAll(sSensors);
      }
      if (y == shape.h() - 1) {
        localSensors.addAll(nSensors);
      }
      if (x == headKey.x() && y == headKey.y()) {
        localSensors.addAll(headSensors);
      }
      return localSensors;
    });
  }

  @SuppressWarnings("unused")
  public static Function<Grid<Boolean>, Grid<List<Sensor<? super Voxel>>>> empty() {
    return shape -> shape.map(b -> List.of());
  }

  @SuppressWarnings("unused")
  public static Function<Grid<Boolean>, Grid<List<Sensor<? super Voxel>>>> uniform(
      @Param(value = "sensors") List<Sensor<? super Voxel>> sensors
  ) {
    return shape -> shape.map(b -> sensors);
  }

}
