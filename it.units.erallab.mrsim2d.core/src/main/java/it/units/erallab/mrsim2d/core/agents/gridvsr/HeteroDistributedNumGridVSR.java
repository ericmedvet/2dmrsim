package it.units.erallab.mrsim2d.core.agents.gridvsr;

import it.units.erallab.mrsim2d.core.functions.TimedRealFunction;
import it.units.erallab.mrsim2d.core.util.Grid;
import it.units.erallab.mrsim2d.core.util.Parametrized;
import it.units.malelab.jnb.core.BuilderMethod;
import it.units.malelab.jnb.core.Param;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.BiFunction;
import java.util.stream.Stream;

public class HeteroDistributedNumGridVSR extends NumGridVSR {

  protected final Grid<TimedRealFunction> timedRealFunctionsGrid;
  int nSignals;
  boolean directional;
  Grid<double[]> signalsGrid;

  Grid<double[]> fullInputsGrid;
  Grid<double[]> fullOutputsGrid;

  public HeteroDistributedNumGridVSR(GridBody body, Grid<TimedRealFunction> timedRealFunctionsGrid, int nSignals, boolean directional) {
    super(body);
    int communicationSize = directional ? nSignals * 4 : nSignals;
    int nOfOutputs = communicationSize + 1;
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
    this.nSignals = nSignals;
    this.directional = directional;
    this.timedRealFunctionsGrid = timedRealFunctionsGrid;
    signalsGrid = voxelGrid.map(v -> v != null ? new double[communicationSize] : null);
    fullInputsGrid = body.sensorsGrid().map(d -> d != null ? new double[d.size() + 4 * nSignals] : null);
    fullOutputsGrid = voxelGrid.map(d -> d != null ? new double[1 + communicationSize] : null);
  }

  @BuilderMethod
  public HeteroDistributedNumGridVSR(
      @Param("body") GridBody body,
      @Param("function") BiFunction<Integer, Integer, ? extends TimedRealFunction> timedRealFunctionBuilder,
      @Param("signals") int nSignals,
      @Param("directional") boolean directional
  ) {
    this(body,
        body.sensorsGrid().map(v -> v != null ?
            timedRealFunctionBuilder.apply(4 * nSignals + v.size(), 1 + (directional ? 4 * nSignals : nSignals)) : null),
        nSignals,
        directional
    );
  }

  @Override
  protected Grid<Double> computeActuationValues(double t, Grid<double[]> inputsGrid) {
    // create actual input grid (concat sensed values and communication signals)
    for (Grid.Key key : inputsGrid.keys()) {
      if (inputsGrid.get(key) == null) {
        continue;
      }
      double[] sensoryInputs = inputsGrid.get(key);
      double[] signals0 = getLastSignals(key.x(), key.y() + 1, 0);
      double[] signals1 = getLastSignals(key.x() + 1, key.y(), 1);
      double[] signals2 = getLastSignals(key.x(), key.y() - 1, 2);
      double[] signals3 = getLastSignals(key.x() - 1, key.y(), 3);
      double[] fullInputs = Stream.of(sensoryInputs, signals0, signals1, signals2, signals3)
          .flatMapToDouble(Arrays::stream)
          .toArray();
      fullInputsGrid.set(key, fullInputs);
    }
    // process values
    for (Grid.Key key : fullInputsGrid.keys()) {
      if (fullInputsGrid.get(key) == null) {
        continue;
      }
      double[] inputs = fullInputsGrid.get(key);
      if (inputs.length != timedRealFunctionsGrid.get(key).nOfInputs()) {
        throw new IllegalArgumentException(String.format(
            "Wrong number of inputs in position (%d,%d): %d expected, %d found",
            key.x(),
            key.y(),
            timedRealFunctionsGrid.get(key).nOfInputs(),
            inputs.length
        ));
      }
      fullOutputsGrid.set(key, timedRealFunctionsGrid.get(key).apply(t, inputs));
    }
    // split actuation and communication for next step
    Grid<Double> outputsGrid = Grid.create(inputsGrid.w(), inputsGrid.h(), 0d);
    for (Grid.Key key : fullOutputsGrid.keys()) {
      if (fullOutputsGrid.get(key) == null) {
        continue;
      }
      double[] fullOutputs = fullOutputsGrid.get(key);
      double actuationValue = fullOutputs[0];
      double[] signals = Arrays.stream(fullOutputs, 1, fullOutputs.length).toArray();
      outputsGrid.set(key, actuationValue);
      signalsGrid.set(key, signals);
    }
    return outputsGrid;
  }

  private double[] getLastSignals(int x, int y, int c) {
    if (x < 0 || y < 0 || x >= signalsGrid.w() || y >= signalsGrid.h() || signalsGrid.get(x, y) == null) {
      return new double[nSignals];
    }
    double[] allSignals = signalsGrid.get(x, y);
    return directional ? Arrays.stream(allSignals, c * nSignals, (c + 1) * nSignals).toArray() : allSignals;
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
