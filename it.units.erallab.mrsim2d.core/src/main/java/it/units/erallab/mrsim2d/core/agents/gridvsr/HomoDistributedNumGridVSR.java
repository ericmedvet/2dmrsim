package it.units.erallab.mrsim2d.core.agents.gridvsr;

import it.units.erallab.mrsim2d.core.functions.TimedRealFunction;
import it.units.erallab.mrsim2d.core.util.Grid;
import it.units.erallab.mrsim2d.core.util.Parametrized;

import java.util.function.Supplier;

public class HomoDistributedNumGridVSR extends HeteroDistributedNumGridVSR {

  public HomoDistributedNumGridVSR(GridBody body, Supplier<TimedRealFunction> timedRealFunctionsSupplier, int nSignals, boolean directional) {
    super(body, body.grid().map(i -> timedRealFunctionsSupplier.get()), nSignals, directional);
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
