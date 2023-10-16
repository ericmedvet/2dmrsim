
package io.github.ericmedvet.mrsim2d.core.bodies;

import io.github.ericmedvet.mrsim2d.core.geometry.Point;
import io.github.ericmedvet.mrsim2d.core.geometry.Poly;
public interface Body {
  double angle();

  Point centerLinearVelocity();

  double mass();

  Poly poly();
}
