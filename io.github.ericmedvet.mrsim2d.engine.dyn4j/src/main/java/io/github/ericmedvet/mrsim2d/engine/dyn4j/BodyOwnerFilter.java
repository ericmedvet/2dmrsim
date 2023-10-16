
package io.github.ericmedvet.mrsim2d.engine.dyn4j;

import io.github.ericmedvet.mrsim2d.core.bodies.Body;
import org.dyn4j.collision.Filter;
public class BodyOwnerFilter implements Filter {
  private final Body owner;

  public BodyOwnerFilter(Body owner) {
    this.owner = owner;
  }

  public Body getOwner() {
    return owner;
  }

  @Override
  public boolean isAllowed(Filter filter) {
    if (filter instanceof BodyOwnerFilter otherBodyOwnerFilter) {
      return owner != otherBodyOwnerFilter.owner;
    }
    return true;
  }
}
