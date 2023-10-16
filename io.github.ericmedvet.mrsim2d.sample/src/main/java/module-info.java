import io.github.ericmedvet.mrsim2d.core.engine.Engine;
module io.github.ericmedvet.mrsim2d.sample {
  requires java.logging;
  uses Engine;
  requires io.github.ericmedvet.mrsim2d.core;
  requires io.github.ericmedvet.mrsim2d.viewer;
  requires io.github.ericmedvet.jnb.core;
  requires io.github.ericmedvet.jsdynsym.core;
  requires io.github.ericmedvet.mrsim2d.buildable;
  exports io.github.ericmedvet.mrsim2d.sample;
}