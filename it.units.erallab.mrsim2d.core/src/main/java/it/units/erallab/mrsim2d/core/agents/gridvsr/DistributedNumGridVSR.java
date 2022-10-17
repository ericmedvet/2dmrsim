package it.units.erallab.mrsim2d.core.agents.gridvsr;

import it.units.erallab.mrsim2d.core.util.Grid;

import java.util.Arrays;
import java.util.function.BiFunction;
import java.util.stream.Stream;

public class DistributedNumGridVSR extends NumGridVSR {

  int nSignals;
  boolean directional;
  Grid<double[]> signalsGrid;

  Grid<double[]> fullInputsGrid;
  Grid<double[]> fullOutputsGrid;

  // TODO add grid of functions -> two constructors for homo and hetero (maybe build methods)

  // from grid of timedrealfunction to function of grids
  // from single timedrealfunction to grid and then call the one above

  public DistributedNumGridVSR(GridBody body, BiFunction<Double, Grid<double[]>, Grid<double[]>> timedFunction, int nSignals, boolean directional) {
    super(body, timedFunction);
    this.nSignals = nSignals;
    this.directional = directional;
    int outputSize = directional ? nSignals * 4 : nSignals;
    signalsGrid = voxelGrid.map(v -> v != null ? new double[outputSize] : null);
    fullInputsGrid = Grid.create(inputsGrid, d -> new double[d.length + 4 * nSignals]);
    fullOutputsGrid = Grid.create(outputGrid, d -> new double[1 + outputSize]);
  }

  @Override
  protected void computeActuationValues(double t) {
    // create actual input grid (concat sensed values and communication signals)
    for (Grid.Key key : inputsGrid.keys()) {
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
    if (x < 0 || y < 0 || x >= signalsGrid.w() || y >= signalsGrid.h()) {
      return new double[nSignals];
    }
    double[] allSignals = signalsGrid.get(x, y);
    return directional ? Arrays.stream(allSignals, c * nSignals, (c + 1) * nSignals).toArray() : allSignals;
  }

}
