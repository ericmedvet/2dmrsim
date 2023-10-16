
package io.github.ericmedvet.mrsim2d.buildable.builders;

import io.github.ericmedvet.jnb.core.Param;
import io.github.ericmedvet.jsdynsym.grid.Grid;
import io.github.ericmedvet.mrsim2d.core.Sensor;
import io.github.ericmedvet.mrsim2d.core.agents.gridvsr.GridBody;
import io.github.ericmedvet.mrsim2d.core.bodies.Body;

import java.util.List;
import java.util.function.Function;

public class VSRMisc {
  private VSRMisc() {
  }

  @SuppressWarnings("unused")
  public static GridBody gridBody(
      @Param("shape") Grid<GridBody.VoxelType> shape,
      @Param("sensorizingFunction") Function<Grid<Boolean>, Grid<List<Sensor<? super Body>>>> sensorizingFunction
  ) {
    return new GridBody(shape, sensorizingFunction);
  }
}
