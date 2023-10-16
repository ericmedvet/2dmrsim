
package io.github.ericmedvet.mrsim2d.core.agents.gridvsr;


import io.github.ericmedvet.jsdynsym.core.DoubleRange;
import io.github.ericmedvet.jsdynsym.grid.Grid;
import io.github.ericmedvet.mrsim2d.core.Action;
import io.github.ericmedvet.mrsim2d.core.ActionOutcome;
import io.github.ericmedvet.mrsim2d.core.Sensor;
import io.github.ericmedvet.mrsim2d.core.actions.ActuateVoxel;
import io.github.ericmedvet.mrsim2d.core.actions.Sense;
import io.github.ericmedvet.mrsim2d.core.bodies.Body;
import io.github.ericmedvet.mrsim2d.core.bodies.Voxel;

import java.util.*;
public abstract class NumGridVSR extends AbstractGridVSR {

  protected final static DoubleRange INPUT_RANGE = DoubleRange.SYMMETRIC_UNIT;
  protected final static DoubleRange OUTPUT_RANGE = DoubleRange.SYMMETRIC_UNIT;
  private final static Voxel.Side[] SIDES = new Voxel.Side[]{Voxel.Side.N, Voxel.Side.E, Voxel.Side.S, Voxel.Side.W};
  private final Grid<List<Sensor<? super Body>>> sensorsGrid;
  private final Grid<double[]> inputsGrid;
  private final Grid<double[]> outputGrid;
  private final GridBody body;

  public NumGridVSR(
      GridBody body,
      double voxelSideLength,
      double voxelMass
  ) {
    super(body.grid().map(GridBody.SensorizedElement::element), voxelSideLength, voxelMass);
    this.sensorsGrid = body.grid().map(GridBody.SensorizedElement::sensors);
    this.body = body;
    inputsGrid = body.grid().map(e -> e.element().type().equals(GridBody.VoxelType.NONE) ? null : new double[e.sensors()
        .size()]);
    outputGrid = bodyGrid.map(v -> v != null ? new double[SIDES.length] : null);
  }

  public NumGridVSR(GridBody body) {
    this(body, VOXEL_SIDE_LENGTH, VOXEL_MASS);
  }

  protected abstract Grid<double[]> computeActuationValues(double t, Grid<double[]> inputsGrid);

  private static EnumMap<Voxel.Side, Double> sideMap(double[] values) {
    return new EnumMap<>(Map.of(
        Voxel.Side.N, values[0],
        Voxel.Side.E, values[1],
        Voxel.Side.S, values[2],
        Voxel.Side.W, values[3]
    ));
  }

  @Override
  public List<? extends Action<?>> act(double t, List<ActionOutcome<?, ?>> previousActionOutcomes) {
    //read inputs from last request
    if (!previousActionOutcomes.isEmpty()) {
      int c = 0;
      for (Grid.Key key : inputsGrid.keys()) {
        double[] inputs = inputsGrid.get(key);
        if (inputs != null) {
          for (int i = 0; i < inputs.length; i++) {
            ActionOutcome<?, ?> outcome = previousActionOutcomes.get(c);
            if (outcome.action() instanceof Sense<?>) {
              @SuppressWarnings("unchecked") ActionOutcome<? extends Sense<Voxel>, Double> o = (ActionOutcome<? extends Sense<Voxel>, Double>) outcome;
              inputs[i] = INPUT_RANGE.denormalize(
                  o.action().range().normalize(o.outcome().orElse(0d))
              );
              c = c + 1;
            }
          }
        }
      }
    }
    //compute actuation
    computeActuationValues(t, inputsGrid).entries()
        .forEach(e -> outputGrid.set(e.key(), Arrays.stream(e.value()).map(OUTPUT_RANGE::clip).toArray()));
    //generate next sense actions
    List<Action<?>> actions = new ArrayList<>();
    actions.addAll(bodyGrid.entries().stream()
        .filter(e -> e.value() != null)
        .map(e -> sensorsGrid.get(e.key()).stream()
            .map(f -> f.apply(e.value()))
            .toList())
        .flatMap(Collection::stream)
        .toList());
    //generate actuation actions
    actions.addAll(bodyGrid.entries().stream()
        .filter(e -> e.value() instanceof Voxel)
        .map(e -> new ActuateVoxel(
            (Voxel) e.value(),
            sideMap(outputGrid.get(e.key()))
        ))
        .toList());
    return actions;
  }

  public GridBody getBody() {
    return body;
  }

}
