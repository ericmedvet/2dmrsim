import io.github.ericmedvet.mrsim2d.core.engine.Engine;
import io.github.ericmedvet.mrsim2d.engine.dyn4j.Dyn4JEngine;
module io.github.ericmedvet.mrsim2d.engine.dyn4j {
  requires org.dyn4j;
  requires io.github.ericmedvet.mrsim2d.core;
  requires io.github.ericmedvet.mrsim2d.viewer;
  requires io.github.ericmedvet.jsdynsym.core;
  requires java.desktop;
  provides Engine with Dyn4JEngine;
  exports io.github.ericmedvet.mrsim2d.engine.dyn4j.drawers;
}