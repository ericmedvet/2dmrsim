
package io.github.ericmedvet.mrsim2d.core.actions;

import io.github.ericmedvet.mrsim2d.core.Action;
import io.github.ericmedvet.mrsim2d.core.bodies.Voxel;
public record CreateVoxel(
    double sideLength,
    double mass,
    Voxel.Material material
) implements Action<Voxel> {
  public CreateVoxel(double sideLength, double mass) {
    this(sideLength, mass, new Voxel.Material());
  }
}
