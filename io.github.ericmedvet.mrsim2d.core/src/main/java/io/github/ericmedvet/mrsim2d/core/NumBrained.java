
package io.github.ericmedvet.mrsim2d.core;

import io.github.ericmedvet.jsdynsym.core.numerical.NumericalDynamicalSystem;

import java.util.List;

public interface NumBrained extends NumMultiBrained {

  NumericalDynamicalSystem<?> brain();

  BrainIO brainIO();

  @Override
  default List<BrainIO> brainIOs() {
    return List.of(brainIO());
  }

  @Override
  default List<NumericalDynamicalSystem<?>> brains() {
    return List.of(brain());
  }
}
