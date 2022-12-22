module it.units.erallab.mrsim2d.buildable {
  uses it.units.erallab.mrsim2d.core.engine.Engine;
  requires it.units.erallab.mrsim2d.core;
  requires it.units.erallab.mrsim2d.viewer;
  requires io.github.ericmedvet.jnb.core;
  exports it.units.erallab.mrsim2d.buildable;
  opens it.units.erallab.mrsim2d.buildable.builders to io.github.ericmedvet.jnb.core;
  exports it.units.erallab.mrsim2d.buildable.builders;
}