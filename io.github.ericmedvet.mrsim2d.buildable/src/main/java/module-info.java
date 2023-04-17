import io.github.ericmedvet.mrsim2d.core.engine.Engine;

module io.github.ericmedvet.mrsim2d.buildable {
  uses Engine;
  requires io.github.ericmedvet.mrsim2d.core;
  requires io.github.ericmedvet.mrsim2d.viewer;
  requires io.github.ericmedvet.jnb.core;
  requires io.github.ericmedvet.jsdynsym.buildable;
  requires io.github.ericmedvet.jsdynsym.core;
  exports io.github.ericmedvet.mrsim2d.buildable;
  opens io.github.ericmedvet.mrsim2d.buildable.builders to io.github.ericmedvet.jnb.core;
  exports io.github.ericmedvet.mrsim2d.buildable.builders;
}