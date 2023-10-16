
package io.github.ericmedvet.mrsim2d.core.actions;

import io.github.ericmedvet.jsdynsym.core.DoubleRange;
import io.github.ericmedvet.mrsim2d.core.ActionPerformer;
import io.github.ericmedvet.mrsim2d.core.Agent;
import io.github.ericmedvet.mrsim2d.core.SelfDescribedAction;
import io.github.ericmedvet.mrsim2d.core.bodies.Voxel;
import io.github.ericmedvet.mrsim2d.core.engine.ActionException;

public record SenseSideCompression(Voxel.Side side, Voxel body) implements Sense<Voxel>, SelfDescribedAction<Double> {
  private final static DoubleRange RANGE = new DoubleRange(0.5, 1.5);

  @Override
  public Double perform(ActionPerformer performer, Agent agent) throws ActionException {
    double avgL = Math.sqrt(body.areaRatio() * body.restArea());
    return RANGE.clip(body.vertex(side.getVertex1()).distance(body.vertex(side.getVertex2())) / avgL);
  }

  @Override
  public DoubleRange range() {
    return RANGE;
  }
}
