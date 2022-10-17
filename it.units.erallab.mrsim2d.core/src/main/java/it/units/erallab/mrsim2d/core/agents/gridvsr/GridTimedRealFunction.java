package it.units.erallab.mrsim2d.core.agents.gridvsr;

import it.units.erallab.mrsim2d.core.functions.TimedRealFunction;
import it.units.erallab.mrsim2d.core.util.Grid;
import it.units.erallab.mrsim2d.core.util.Parametrized;
import it.units.erallab.mrsim2d.core.util.Utils;

import java.util.List;
import java.util.Objects;
import java.util.function.BiFunction;

public class GridTimedRealFunction implements BiFunction<Double, Grid<double[]>, Grid<double[]>>, Parametrized {

  private final TimedRealFunction timedRealFunction;
  private final int nOfInputs;

  public GridTimedRealFunction(GridBody body, TimedRealFunction timedRealFunction) {
    if (timedRealFunction.nOfInputs() != nOfInputs(body)) {
      throw new IllegalArgumentException("Wrong number of inputs: body requires %d, function takes %d".formatted(
          nOfInputs(body),
          timedRealFunction.nOfInputs()
      ));
    }
    if (timedRealFunction.nOfOutputs() != nOfOutputs(body)) {
      throw new IllegalArgumentException("Wrong number of ouputs: body requires %d, function takes %d".formatted(
          nOfOutputs(body),
          timedRealFunction.nOfOutputs()
      ));
    }
    this.timedRealFunction = timedRealFunction;
    this.nOfInputs = nOfInputs(body);
  }

  public static int nOfInputs(GridBody body) {
    return body.sensorsGrid().values().stream().filter(Objects::nonNull).mapToInt(List::size).sum();
  }

  public static int nOfOutputs(GridBody body) {
    return (int) body.sensorsGrid().values().stream().filter(Objects::nonNull).count();
  }

  @Override
  public Grid<double[]> apply(Double t, Grid<double[]> inputsGrid) {
    //build inputs
    double[] inputs = Utils.concat(inputsGrid.values().stream().filter(Objects::nonNull).toList());
    if (inputs.length != nOfInputs) {
      throw new IllegalArgumentException(String.format(
          "Wrong number of inputs: %d expected, %d found",
          nOfInputs,
          inputs.length
      ));
    }
    //compute outputs
    double[] outputs = timedRealFunction.apply(t, inputs);
    //split outputs
    Grid<double[]> outputsGrid = Grid.create(inputsGrid.w(), inputsGrid.h(), new double[]{0d});
    int c = 0;
    for (Grid.Entry<double[]> e : inputsGrid) {
      if (e.value() != null) {
        outputsGrid.set(e.key(), new double[]{outputs[c]});
        c = c + 1;
      }
    }
    return outputsGrid;
  }

  @Override
  public double[] getParams() {
    if (timedRealFunction instanceof Parametrized parametrized) {
      return parametrized.getParams();
    }
    return new double[0];
  }

  @Override
  public void setParams(double[] params) {
    if (timedRealFunction instanceof Parametrized parametrized) {
      parametrized.setParams(params);
    } else if (params.length > 0) {
      throw new IllegalArgumentException(
          "Cannot set params because the function %s has no params".formatted(
              timedRealFunction
          ));
    }
  }
}
