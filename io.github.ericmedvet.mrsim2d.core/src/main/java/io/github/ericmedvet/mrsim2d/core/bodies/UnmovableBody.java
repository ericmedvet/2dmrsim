
package io.github.ericmedvet.mrsim2d.core.bodies;

import io.github.ericmedvet.mrsim2d.core.geometry.Point;
public interface UnmovableBody extends RigidBody {
  @Override
  default double angle() {
    return 0;
  }

  @Override
  default Point centerLinearVelocity() {
    return Point.ORIGIN;
  }

  @Override
  default double mass() {
    return Double.POSITIVE_INFINITY;
  }
}
