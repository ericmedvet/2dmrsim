
package io.github.ericmedvet.mrsim2d.core.agents.gridvsr;

import io.github.ericmedvet.jsdynsym.core.numerical.NumericalDynamicalSystem;
import io.github.ericmedvet.jsdynsym.grid.Grid;
import io.github.ericmedvet.mrsim2d.core.NumMultiBrained;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

public class DistributedNumGridVSR extends NumGridVSR implements NumMultiBrained {

  private final Grid<NumericalDynamicalSystem<?>> numericalDynamicalSystemGrid;
  private final int nSignals;
  private final boolean directional;
  private final Grid<double[]> signalsGrid;

  private final Grid<double[]> fullInputsGrid;
  private final Grid<double[]> fullOutputsGrid;

  public DistributedNumGridVSR(
      GridBody body,
      Grid<NumericalDynamicalSystem<?>> numericalDynamicalSystemGrid,
      int nSignals,
      boolean directional
  ) {
    super(body);
    int communicationSize = directional ? nSignals * 4 : nSignals;
    for (Grid.Key key : numericalDynamicalSystemGrid.keys()) {
      if (numericalDynamicalSystemGrid.get(key) != null) {
        numericalDynamicalSystemGrid.get(key).checkDimension(
            nOfInputs(body, key, nSignals, directional),
            nOfOutputs(body, key, nSignals, directional)
        );
      }
    }
    this.nSignals = nSignals;
    this.directional = directional;
    this.numericalDynamicalSystemGrid = numericalDynamicalSystemGrid;
    signalsGrid = bodyGrid.map(v -> v != null ? new double[communicationSize] : null);
    fullInputsGrid = Grid.create(
        body.grid().w(),
        body.grid().h(),
        k -> new double[nOfInputs(body, k, nSignals, directional)]
    );
    fullOutputsGrid = Grid.create(
        body.grid().w(),
        body.grid().h(),
        k -> new double[nOfOutputs(body, k, nSignals, directional)]
    );
  }

  public static int nOfInputs(GridBody body, Grid.Key key, int nSignals, boolean directional) {
    return body.grid().get(key).sensors().size() + 4 * nSignals;
  }

  public static int nOfOutputs(GridBody body, Grid.Key key, int nSignals, boolean directional) {
    int communicationSize = directional ? nSignals * 4 : nSignals;
    return communicationSize + 1;
  }

  @Override
  public List<BrainIO> brainIOs() {
    return getBody().grid().stream()
        .filter(e -> !e.value().element().type().equals(GridBody.VoxelType.NONE))
        .map(e -> new BrainIO(
            new RangedValues(fullInputsGrid.get(e.key()), INPUT_RANGE),
            new RangedValues(fullOutputsGrid.get(e.key()), OUTPUT_RANGE)
        ))
        .toList();
  }

  @Override
  public List<NumericalDynamicalSystem<?>> brains() {
    return numericalDynamicalSystemGrid.values().stream().filter(Objects::nonNull).toList();
  }

  @Override
  protected Grid<double[]> computeActuationValues(double t, Grid<double[]> inputsGrid) {
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
    for (Grid.Key key : numericalDynamicalSystemGrid.keys()) {
      if (numericalDynamicalSystemGrid.get(key) == null) {
        continue;
      }
      double[] inputs = fullInputsGrid.get(key);
      if (inputs.length != numericalDynamicalSystemGrid.get(key).nOfInputs()) {
        throw new IllegalArgumentException(String.format(
            "Wrong number of inputs in position (%d,%d): %d expected, %d found",
            key.x(),
            key.y(),
            numericalDynamicalSystemGrid.get(key).nOfInputs(),
            inputs.length
        ));
      }
      fullOutputsGrid.set(key, numericalDynamicalSystemGrid.get(key).step(t, inputs));
    }
    // split actuation and communication for next step
    Grid<double[]> outputsGrid = Grid.create(inputsGrid.w(), inputsGrid.h(), new double[4]);
    for (Grid.Key key : fullOutputsGrid.keys()) {
      if (fullOutputsGrid.get(key) == null) {
        continue;
      }
      double[] fullOutputs = fullOutputsGrid.get(key);
      double actuationValue = fullOutputs[0];
      double[] signals = Arrays.stream(fullOutputs, 1, fullOutputs.length).toArray();
      outputsGrid.set(key, new double[]{
          actuationValue,
          actuationValue,
          actuationValue,
          actuationValue
      });
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
