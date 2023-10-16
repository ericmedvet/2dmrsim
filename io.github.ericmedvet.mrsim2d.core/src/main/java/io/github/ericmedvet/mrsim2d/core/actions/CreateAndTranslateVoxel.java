
package io.github.ericmedvet.mrsim2d.core.actions;

import io.github.ericmedvet.mrsim2d.core.ActionPerformer;
import io.github.ericmedvet.mrsim2d.core.Agent;
import io.github.ericmedvet.mrsim2d.core.SelfDescribedAction;
import io.github.ericmedvet.mrsim2d.core.bodies.Voxel;
import io.github.ericmedvet.mrsim2d.core.engine.ActionException;
import io.github.ericmedvet.mrsim2d.core.geometry.Point;
public record CreateAndTranslateVoxel(
    double sideLength,
    double mass,
    Voxel.Material material,
    Point translation
) implements SelfDescribedAction<Voxel> {
  public CreateAndTranslateVoxel(
      double sideLength,
      double mass,
      Point translation
  ) {
    this(sideLength, mass, new Voxel.Material(), translation);
  }

  @Override
  public Voxel perform(ActionPerformer performer, Agent agent) throws ActionException {
    Voxel voxel = performer.perform(
        new CreateVoxel(sideLength, mass, material),
        agent
    ).outcome().orElseThrow(() -> new ActionException(this, "Undoable creation"));
    performer.perform(new TranslateBody(voxel, translation), agent);
    return voxel;
  }
}
