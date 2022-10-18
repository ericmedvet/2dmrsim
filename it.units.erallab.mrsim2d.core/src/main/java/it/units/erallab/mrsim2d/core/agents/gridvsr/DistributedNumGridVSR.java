package it.units.erallab.mrsim2d.core.agents.gridvsr;

import it.units.erallab.mrsim2d.core.functions.TimedRealFunction;
import it.units.erallab.mrsim2d.core.util.Grid;

import java.util.Arrays;
import java.util.stream.Stream;

public class DistributedNumGridVSR extends AbstractNumGridVSR {

  int nSignals;
  boolean directional;
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
    super(body, gridTimedRealFunction);
    this.nSignals = nSignals;
    this.directional = directional;
    int outputSize = directional ? nSignals * 4 : nSignals;
    signalsGrid = voxelGrid.map(v -> v != null ? new double[outputSize] : null);
    fullInputsGrid = Grid.create(inputsGrid, d -> d != null ? new double[d.length + 4 * nSignals] : null);
    fullOutputsGrid = Grid.create(outputGrid, d -> d != null ? new double[1 + outputSize] : null);
  }

  @Override
  protected void computeActuationValues(double t) {
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
    timedFunction.apply(t, fullInputsGrid).entries().forEach(e -> fullOutputsGrid.set(e.key(), e.value()));
    // split actuation and communication for next step
    for (Grid.Key key : fullOutputsGrid.keys()) {
      double[] fullOutputs = fullOutputsGrid.get(key);
      double actuationValue = fullOutputs[0];
      double[] signals = Arrays.stream(fullOutputs, 1, fullOutputs.length).toArray();
      outputGrid.set(key, actuationValue);
      signalsGrid.set(key, signals);
    }
  }

  private double[] getLastSignals(int x, int y, int c) {
    if (x < 0 || y < 0 || x >= signalsGrid.w() || y >= signalsGrid.h() || signalsGrid.get(x, y) == null) {
      return new double[nSignals];
    }
    double[] allSignals = signalsGrid.get(x, y);
    return directional ? Arrays.stream(allSignals, c * nSignals, (c + 1) * nSignals).toArray() : allSignals;
  }

}
