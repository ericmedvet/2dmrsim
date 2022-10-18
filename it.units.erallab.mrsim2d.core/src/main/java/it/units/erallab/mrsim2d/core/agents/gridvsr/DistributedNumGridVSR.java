package it.units.erallab.mrsim2d.core.agents.gridvsr;

import it.units.erallab.mrsim2d.core.functions.TimedRealFunction;
import it.units.erallab.mrsim2d.core.util.Grid;

import java.util.Arrays;
import java.util.function.BiFunction;
import java.util.stream.Stream;

public class DistributedNumGridVSR extends NumGridVSR {

  int nSignals;
  boolean directional;
  private final BiFunction<Double, Grid<double[]>, Grid<double[]>> function;
  Grid<double[]> signalsGrid;

  Grid<double[]> fullInputsGrid;
  Grid<double[]> fullOutputsGrid;

  public DistributedNumGridVSR(GridBody body, Grid<TimedRealFunction> timedRealFunctionsGrid, int nSignals, boolean directional) {
    this(body, new HeteroGridTimedRealFunction(body, nSignals, directional, timedRealFunctionsGrid), nSignals, directional);
  }

  public DistributedNumGridVSR(GridBody body, TimedRealFunction timedRealFunction, int nSignals, boolean directional) {
    this(body, new HomoGridTimedRealFunction(body, nSignals, directional, timedRealFunction), nSignals, directional);
  }

  private DistributedNumGridVSR(GridBody body, HeteroGridTimedRealFunction gridTimedRealFunction, int nSignals, boolean directional) {
    super(body);
    this.nSignals = nSignals;
    this.directional = directional;
    this.function = gridTimedRealFunction;
    int outputSize = directional ? nSignals * 4 : nSignals;
    signalsGrid = voxelGrid.map(v -> v != null ? new double[outputSize] : null);
    fullInputsGrid = body.sensorsGrid().map(d -> d != null ? new double[d.size() + 4 * nSignals] : null);
    fullOutputsGrid = voxelGrid.map(d -> d != null ? new double[1 + outputSize] : null);
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
    function.apply(t, fullInputsGrid).entries().forEach(e -> fullOutputsGrid.set(e.key(), e.value()));
    // split actuation and communication for next step
    Grid<Double> outputsGrid = Grid.create(inputsGrid.w(), inputsGrid.h(), 0d);
    for (Grid.Key key : fullOutputsGrid.keys()) {
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

}
