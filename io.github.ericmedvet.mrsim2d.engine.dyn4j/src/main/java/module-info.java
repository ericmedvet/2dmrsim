import io.github.ericmedvet.mrsim2d.core.engine.Engine;
import io.github.ericmedvet.mrsim2d.engine.dyn4j.Dyn4JEngine;

/**
 * @author "Eric Medvet" on 2022/08/28 for 2dmrsim
 */
module io.github.ericmedvet.mrsim2d.engine.dyn4j {
  requires org.dyn4j;
  requires io.github.ericmedvet.mrsim2d.core;
  requires io.github.ericmedvet.mrsim2d.viewer;
  requires java.desktop;
  provides Engine with Dyn4JEngine;
  exports io.github.ericmedvet.mrsim2d.engine.dyn4j.drawers;
}