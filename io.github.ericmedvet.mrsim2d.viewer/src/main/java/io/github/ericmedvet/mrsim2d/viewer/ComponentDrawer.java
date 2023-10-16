
package io.github.ericmedvet.mrsim2d.viewer;

import java.awt.*;
public interface ComponentDrawer {

  boolean draw(double t, Object component, Graphics2D g);

  default ComponentDrawer andThen(ComponentDrawer otherDrawer) {
    ComponentDrawer thisDrawer = this;
    return (t, component, g) -> {
      if (thisDrawer.draw(t, component, g)) {
        return otherDrawer.draw(t, component, g);
      }
      return false;
    };
  }

}
