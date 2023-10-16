
package io.github.ericmedvet.mrsim2d.core.actions;

import io.github.ericmedvet.jsdynsym.core.DoubleRange;
import io.github.ericmedvet.mrsim2d.core.bodies.Voxel;

import java.util.EnumMap;
import java.util.Map;
public record ActuateVoxel(Voxel body, EnumMap<Voxel.Side, Double> values) implements Actuate<Voxel, Voxel> {
  public ActuateVoxel(Voxel voxel, double value) {
    this(voxel, value, value, value, value);
  }

  public ActuateVoxel(Voxel voxel, double nValue, double eValue, double sValue, double wValue) {
    this(voxel, new EnumMap<>(Map.of(
        Voxel.Side.N, nValue,
        Voxel.Side.E, eValue,
        Voxel.Side.S, sValue,
        Voxel.Side.W, wValue
    )));
  }

  @Override
  public DoubleRange range() {
    return DoubleRange.SYMMETRIC_UNIT;
  }
}
