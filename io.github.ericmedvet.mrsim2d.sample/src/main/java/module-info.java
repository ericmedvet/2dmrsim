import io.github.ericmedvet.mrsim2d.core.engine.Engine;

/**
 * @author "Eric Medvet" on 2022/08/28 for 2dmrsim
 */
module io.github.ericmedvet.mrsim2d.sample {
  uses Engine;
  requires io.github.ericmedvet.mrsim2d.core;
  requires io.github.ericmedvet.mrsim2d.viewer;
  requires io.github.ericmedvet.jnb.core;
  requires io.github.ericmedvet.mrsim2d.buildable;
  exports io.github.ericmedvet.mrsim2d.sample;
}