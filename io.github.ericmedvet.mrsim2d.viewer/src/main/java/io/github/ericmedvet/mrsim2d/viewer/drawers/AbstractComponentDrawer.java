
package io.github.ericmedvet.mrsim2d.viewer.drawers;

import io.github.ericmedvet.mrsim2d.viewer.ComponentDrawer;

import java.awt.*;
public abstract class AbstractComponentDrawer<K> implements ComponentDrawer {
  private final Class<K> bodyClass;

  public AbstractComponentDrawer(Class<K> bodyClass) {
    this.bodyClass = bodyClass;
  }

  protected abstract boolean innerDraw(double t, K k, Graphics2D g);

  @SuppressWarnings("unchecked")
  @Override
  public boolean draw(double t, Object o, Graphics2D g) {
    if (bodyClass.isAssignableFrom(o.getClass())) {
      return innerDraw(t, (K) o, g);
    }
    return false;
  }
}
