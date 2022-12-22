/**
 * @author "Eric Medvet" on 2022/08/28 for 2dmrsim
 */

module it.units.erallab.mrsim2d.core {
  requires java.logging;
  exports io.github.ericmedvet.mrsim2d.core;
  exports io.github.ericmedvet.mrsim2d.core.actions;
  exports io.github.ericmedvet.mrsim2d.core.agents.gridvsr;
  exports io.github.ericmedvet.mrsim2d.core.agents.independentvoxel;
  exports io.github.ericmedvet.mrsim2d.core.agents.legged;
  exports io.github.ericmedvet.mrsim2d.core.bodies;
  exports io.github.ericmedvet.mrsim2d.core.engine;
  exports io.github.ericmedvet.mrsim2d.core.functions;
  exports io.github.ericmedvet.mrsim2d.core.geometry;
  exports io.github.ericmedvet.mrsim2d.core.tasks;
  exports io.github.ericmedvet.mrsim2d.core.tasks.locomotion;
  exports io.github.ericmedvet.mrsim2d.core.tasks.piling;
  exports io.github.ericmedvet.mrsim2d.core.util;
}