package it.units.erallab.mrsim2d.core.agents.gridvsr;

import it.units.erallab.mrsim2d.core.functions.TimedRealFunction;
import it.units.erallab.mrsim2d.core.util.Grid;
import it.units.erallab.mrsim2d.core.util.Parametrized;

public class HomoGridTimedRealFunction extends HeteroGridTimedRealFunction {

  // TODO create a copy of the function
  public HomoGridTimedRealFunction(GridBody body, int nSignals, boolean directional, TimedRealFunction timedRealFunction) {
    super(body, nSignals, directional, Grid.create(body.grid().w(), body.grid().h(), timedRealFunction));
  }

  @Override
  public double[] getParams() {
    for (Grid.Entry<TimedRealFunction> functionEntry : timedRealFunctionsGrid) {
      if (functionEntry.value() instanceof Parametrized parametrized) {
        return parametrized.getParams();
      }
    }
    return new double[0];
  }

  @Override
  public void setParams(double[] params) {
    for (Grid.Entry<TimedRealFunction> functionEntry : timedRealFunctionsGrid) {
      if (functionEntry.value() instanceof Parametrized parametrized) {
        parametrized.setParams(params);
      }
    }
  }
}
