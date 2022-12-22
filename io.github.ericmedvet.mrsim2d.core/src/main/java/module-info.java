/**
 * @author "Eric Medvet" on 2022/08/28 for 2dmrsim
 */

module it.units.erallab.mrsim2d.core {
  requires java.logging;
  exports it.units.erallab.mrsim2d.core;
  exports it.units.erallab.mrsim2d.core.actions;
  exports it.units.erallab.mrsim2d.core.agents.gridvsr;
  exports it.units.erallab.mrsim2d.core.agents.independentvoxel;
  exports it.units.erallab.mrsim2d.core.agents.legged;
  exports it.units.erallab.mrsim2d.core.bodies;
  exports it.units.erallab.mrsim2d.core.engine;
  exports it.units.erallab.mrsim2d.core.functions;
  exports it.units.erallab.mrsim2d.core.geometry;
  exports it.units.erallab.mrsim2d.core.tasks;
  exports it.units.erallab.mrsim2d.core.tasks.locomotion;
  exports it.units.erallab.mrsim2d.core.tasks.piling;
  exports it.units.erallab.mrsim2d.core.util;
}