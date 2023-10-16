
package io.github.ericmedvet.mrsim2d.core.actions;

import io.github.ericmedvet.mrsim2d.core.Action;
import io.github.ericmedvet.mrsim2d.core.bodies.RigidBody;
import io.github.ericmedvet.mrsim2d.core.geometry.Poly;
public record CreateRigidBody(
    Poly poly,
    double mass,
    double anchorsDensity
) implements Action<RigidBody> {
  public CreateRigidBody(Poly poly, double mass) {
    this(poly, mass, Double.POSITIVE_INFINITY);
  }
}
