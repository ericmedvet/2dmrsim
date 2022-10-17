package it.units.erallab.mrsim2d.core.agents.gridvsr;

import it.units.erallab.mrsim2d.core.functions.TimedRealFunction;
import it.units.erallab.mrsim2d.core.util.Grid;
import it.units.erallab.mrsim2d.core.util.Parametrized;
import it.units.erallab.mrsim2d.core.util.Utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.function.BiFunction;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public class HeteroGridTimedRealFunction implements BiFunction<Double, Grid<double[]>, Grid<double[]>>, Parametrized {

  protected final Grid<TimedRealFunction> timedRealFunctionsGrid;
  private final Grid<Integer> nOfInputsGrid;
  private final int nOfOutputs;

  public HeteroGridTimedRealFunction(GridBody body, int nSignals, boolean directional, Grid<TimedRealFunction> timedRealFunctionsGrid) {
    nOfOutputs = 1 + (directional ? 4 * nSignals : nSignals);
    for (Grid.Key key : timedRealFunctionsGrid.keys()) {
      if (body.sensorsGrid().get(key) == null) {
        continue;
      }
      int nOfInputs = body.sensorsGrid().get(key).size() + 4 * nSignals;
      if (timedRealFunctionsGrid.get(key).nOfInputs() != nOfInputs) {
        throw new IllegalArgumentException("Wrong number of inputs in position (%d,%d): body requires %d, function takes %d".formatted(
            key.x(),
            key.y(),
            nOfInputs,
            timedRealFunctionsGrid.get(key).nOfInputs()
        ));
      }
      if (timedRealFunctionsGrid.get(key).nOfOutputs() != nOfOutputs) {
        throw new IllegalArgumentException("Wrong number of outputs in position (%d,%d): body requires %d, function takes %d".formatted(
            key.x(),
            key.y(),
            nOfOutputs,
            timedRealFunctionsGrid.get(key).nOfOutputs()
        ));
      }
    }
    this.timedRealFunctionsGrid = timedRealFunctionsGrid;
    nOfInputsGrid = Grid.create(timedRealFunctionsGrid, TimedRealFunction::nOfInputs);
  }

  @Override
  public Grid<double[]> apply(Double t, Grid<double[]> inputsGrid) {
    Grid<double[]> outputsGrid = Grid.create(inputsGrid, i -> new double[nOfOutputs]);
    for (Grid.Key key : inputsGrid.keys()) {
      if (inputsGrid.get(key) == null) {
        continue;
      }
      double[] inputs = inputsGrid.get(key);
      if (inputs.length != nOfInputsGrid.get(key)) {
        throw new IllegalArgumentException(String.format(
            "Wrong number of inputs in position (%d,%d): %d expected, %d found",
            key.x(),
            key.y(),
            nOfInputsGrid.get(key),
            inputs.length
        ));
      }
      outputsGrid.set(key, timedRealFunctionsGrid.get(key).apply(t, inputs));
    }
    return outputsGrid;
  }

  @Override
  public double[] getParams() {
    List<double[]> parametersList = new ArrayList<>();
    for (Grid.Entry<TimedRealFunction> functionEntry : timedRealFunctionsGrid) {
      if (functionEntry.value() instanceof Parametrized parametrized) {
        parametersList.add(parametrized.getParams());
      }
    }
    return parametersList.stream().flatMapToDouble(Arrays::stream).toArray();
  }

  @Override
  public void setParams(double[] params) {
    int startIndex = 0;
    for (Grid.Entry<TimedRealFunction> functionEntry : timedRealFunctionsGrid) {
      if (functionEntry.value() instanceof Parametrized parametrized) {
        int tempParamsSize = parametrized.getParams().length;
        double[] tempParams = Arrays.stream(params, startIndex, startIndex + tempParamsSize).toArray();
        parametrized.setParams(tempParams);
        startIndex = startIndex + tempParamsSize;
      }
    }
  }
}
