
package io.github.ericmedvet.mrsim2d.core.agents.gridvsr;

import io.github.ericmedvet.jsdynsym.grid.Grid;
import io.github.ericmedvet.mrsim2d.core.Sensor;
import io.github.ericmedvet.mrsim2d.core.bodies.Body;
import io.github.ericmedvet.mrsim2d.core.bodies.Voxel;

import java.util.List;
import java.util.function.Function;

public record GridBody(Grid<SensorizedElement> grid) {

  public GridBody(
      Grid<VoxelType> shape,
      Function<Grid<Boolean>, Grid<List<Sensor<? super Body>>>> sensorizingFunction
  ) {
    this(Grid.create(
        shape.w(),
        shape.h(),
        k -> new SensorizedElement(
            new Element(
                shape.get(k),
                Voxel.DEFAULT_MATERIAL
            ),
            !shape.get(k).equals(VoxelType.NONE) ? sensorizingFunction.apply(shape.map(t -> !t.equals(VoxelType.NONE)))
                .get(k) : List.of()
        )
    ));
  }

  public enum VoxelType {NONE, SOFT, RIGID}

  public record Element(VoxelType type, Voxel.Material material) {}

  public record SensorizedElement(Element element, List<Sensor<? super Body>> sensors) {}

}
