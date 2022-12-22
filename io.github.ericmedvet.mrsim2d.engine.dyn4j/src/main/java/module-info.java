/**
 * @author "Eric Medvet" on 2022/08/28 for 2dmrsim
 */
module it.units.erallab.mrsim2d.engine.dyn4j {
  requires org.dyn4j;
  requires it.units.erallab.mrsim2d.core;
  requires it.units.erallab.mrsim2d.viewer;
  requires java.desktop;
  provides it.units.erallab.mrsim2d.core.engine.Engine with it.units.erallab.mrsim2d.engine.dyn4j.Dyn4JEngine;
  exports it.units.erallab.mrsim2d.engine.dyn4j.drawers;
}