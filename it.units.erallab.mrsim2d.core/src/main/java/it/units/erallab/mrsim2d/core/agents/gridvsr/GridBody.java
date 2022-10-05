package it.units.erallab.mrsim2d.core.agents.gridvsr;

import it.units.erallab.mrsim2d.builder.BuilderMethod;
import it.units.erallab.mrsim2d.builder.Param;
import it.units.erallab.mrsim2d.core.actions.Sense;
import it.units.erallab.mrsim2d.core.bodies.Voxel;
import it.units.erallab.mrsim2d.core.util.Grid;
import it.units.erallab.mrsim2d.core.util.Pair;

import java.util.List;
import java.util.function.Function;

public record GridBody(Grid<Pair<Voxel.Material, List<Function<? super Voxel, Sense<? super Voxel>>>>> grid) {
  @BuilderMethod()
  public GridBody(
      @Param("shape") Grid<Boolean> shape,
      @Param("sensorizingFunction") Function<Grid<Boolean>,
          Grid<List<Function<? super Voxel, Sense<? super Voxel>>>>> sensorizingFunction
  ) {
    this(Grid.create(
        shape.w(),
        shape.h(),
        (x, y) -> new Pair<>(
            shape.get(x, y) ? new Voxel.Material() : null,
            shape.get(x, y) ? sensorizingFunction.apply(shape).get(x, y) : null
        )
    ));
  }

  public Grid<Voxel.Material> materialGrid() {
    return Grid.create(grid, Pair::first);
  }

  public Grid<List<Function<? super Voxel, Sense<? super Voxel>>>> sensorsGrid() {
    return Grid.create(grid, Pair::second);
  }
}
