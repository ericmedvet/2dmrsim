module it.units.erallab.mrsim2d.mrsim2d.buildable {
  requires it.units.erallab.mrsim2d.core;
  requires it.units.malelab.jnb.core;
  exports it.units.erallab.mrsim2d.buildable;
  opens it.units.erallab.mrsim2d.buildable.builders to it.units.malelab.jnb.core;
}