import io.github.ericmedvet.mrsim2d.core.engine.Engine;

/**
 * @author "Eric Medvet" on 2022/08/28 for 2dmrsim
 */
module it.units.erallab.mrsim2d.sample {
  uses Engine;
  requires it.units.erallab.mrsim2d.core;
  requires it.units.erallab.mrsim2d.viewer;
  requires io.github.ericmedvet.jnb.core;
  requires it.units.erallab.mrsim2d.buildable;
  exports io.github.ericmedvet.mrsim2d.sample;
}