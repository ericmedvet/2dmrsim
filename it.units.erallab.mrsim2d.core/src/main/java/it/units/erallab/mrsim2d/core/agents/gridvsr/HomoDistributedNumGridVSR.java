package it.units.erallab.mrsim2d.core.agents.gridvsr;

import it.units.erallab.mrsim2d.core.functions.TimedRealFunction;
import it.units.erallab.mrsim2d.core.util.Grid;
import it.units.erallab.mrsim2d.core.util.Parametrized;
import it.units.malelab.jnb.core.BuilderMethod;
import it.units.malelab.jnb.core.Param;

import java.util.List;
import java.util.Objects;
import java.util.function.BiFunction;
import java.util.function.Supplier;

public class HomoDistributedNumGridVSR extends HeteroDistributedNumGridVSR {

  public HomoDistributedNumGridVSR(GridBody body, Supplier<TimedRealFunction> timedRealFunctionsSupplier, int nSignals, boolean directional) {
    super(body, body.grid().map(i -> timedRealFunctionsSupplier.get()), nSignals, directional);
  }

  @BuilderMethod
  public HomoDistributedNumGridVSR(
      @Param("body") GridBody body,
      @Param("function") BiFunction<Integer, Integer, ? extends TimedRealFunction> timedRealFunctionBuilder,
      @Param("signals") int nSignals,
      @Param("directional") boolean directional
  ) {
    this(body,
        () -> timedRealFunctionBuilder.apply(
            4 * nSignals + body.sensorsGrid().values().stream().filter(Objects::nonNull).mapToInt(List::size).findFirst().orElseThrow(),
            1 + (directional ? 4 * nSignals : nSignals)
        ),
        nSignals,
        directional
    );
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
