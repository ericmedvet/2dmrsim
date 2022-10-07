package it.units.erallab.mrsim2d.core.functions;

import it.units.erallab.mrsim2d.core.util.Parametrized;

/**
 * @author "Eric Medvet" on 2022/10/07 for 2dmrsim
 */
public abstract class CompositeTRF implements TimedRealFunction, Parametrized {
  protected final TimedRealFunction innerF;

  public CompositeTRF(TimedRealFunction innerF) {
    this.innerF = innerF;
  }

  public double[] getParams() {
    if (innerF instanceof Parametrized parametrized) {
      return parametrized.getParams();
    }
    return new double[0];
  }

  @Override
  public void setParams(double[] params) {
    if (innerF instanceof Parametrized parametrized) {
      parametrized.setParams(params);
    } else {
      if (params.length > 0) {
        throw new IllegalArgumentException("Cannot set params because the function %s has no params".formatted(
            innerF));
      }
    }
  }

}
