
package io.github.ericmedvet.mrsim2d.core;

import io.github.ericmedvet.jsdynsym.core.DoubleRange;
import io.github.ericmedvet.jsdynsym.core.numerical.NumericalDynamicalSystem;

import java.util.List;

public interface NumMultiBrained {

  record BrainIO(RangedValues input, RangedValues output) {}

  record RangedValues(double[] values, DoubleRange range) {}

  List<BrainIO> brainIOs();

  List<NumericalDynamicalSystem<?>> brains();

}
