/**
 * @author "Eric Medvet" on 2022/08/28 for 2dmrsim
 */
module it.units.erallab.mrsim2d.viewer {
  requires jcodec;
  requires java.desktop;
  requires java.logging;
  requires it.units.erallab.mrsim2d.core;
  exports it.units.erallab.mrsim2d.viewer;
  exports it.units.erallab.mrsim2d.viewer.drawers;
  exports it.units.erallab.mrsim2d.viewer.drawers.actions;
  exports it.units.erallab.mrsim2d.viewer.drawers.bodies;
  exports it.units.erallab.mrsim2d.viewer.framers;
}