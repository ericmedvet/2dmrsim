
package io.github.ericmedvet.mrsim2d.core.actions;

import io.github.ericmedvet.mrsim2d.core.Action;
import io.github.ericmedvet.mrsim2d.core.bodies.UnmovableBody;
import io.github.ericmedvet.mrsim2d.core.geometry.Poly;
public record CreateUnmovableBody(
    Poly poly,
    double anchorsDensity
) implements Action<UnmovableBody> {
  public CreateUnmovableBody(Poly poly) {
    this(poly, Double.POSITIVE_INFINITY);
  }
}
