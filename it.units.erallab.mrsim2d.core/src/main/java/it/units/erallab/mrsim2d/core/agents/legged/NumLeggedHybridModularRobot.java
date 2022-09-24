package it.units.erallab.mrsim2d.core.agents.legged;

import it.units.erallab.mrsim2d.builder.Param;
import it.units.erallab.mrsim2d.core.Action;
import it.units.erallab.mrsim2d.core.ActionOutcome;
import it.units.erallab.mrsim2d.core.actions.ActuateRotationalJoint;
import it.units.erallab.mrsim2d.core.agents.WithTimedRealFunction;
import it.units.erallab.mrsim2d.core.functions.TimedRealFunction;
import it.units.erallab.mrsim2d.core.util.DoubleRange;

import java.util.List;
import java.util.stream.IntStream;

/**
 * @author "Eric Medvet" on 2022/09/24 for 2dmrsim
 */
public class NumLeggedHybridModularRobot extends AbstractLeggedHybridModularRobot implements WithTimedRealFunction {

  private final static DoubleRange ANGLE_RANGE = new DoubleRange(Math.toRadians(-90), Math.toRadians(90));

  private TimedRealFunction timedRealFunction;

  public NumLeggedHybridModularRobot(@Param("modules") List<Module> modules) {
    super(modules);
    setTimedRealFunction(TimedRealFunction.zeros(nOfInputs(modules), nOfOutputs(modules)));
  }

  public static int nOfInputs(List<Module> modules) {
    return 0;
  }

  public static int nOfOutputs(List<Module> modules) {
    return modules.stream().mapToInt(m -> m.legChunks().size()).sum();
  }

  @Override
  public List<? extends Action<?>> act(double t, List<ActionOutcome<?, ?>> previousActionOutcomes) {
    double[] values = timedRealFunction.apply(t, new double[0]);
    if (values.length != rotationalJoints.size()) {
      throw new IllegalArgumentException("Unexpected function ouptut size: %d found vs. %d expected".formatted(
          values.length,
          rotationalJoints.size()
      ));
    }
    return IntStream.range(0, values.length)
        .mapToObj(i -> (Action<?>) new ActuateRotationalJoint(
            rotationalJoints.get(i),
            ANGLE_RANGE.clip(values[i])
        ))
        .toList();
  }

  @Override
  public TimedRealFunction getTimedRealFunction() {
    return timedRealFunction;
  }

  @Override
  public void setTimedRealFunction(TimedRealFunction timedRealFunction) {
    if (timedRealFunction.nOfInputs() != nOfInputs(modules)) {
      throw new IllegalArgumentException(String.format(
          "Invalid function input size: %d found vs. %d expected",
          timedRealFunction.nOfInputs(),
          nOfInputs(modules)
      ));
    }
    if (timedRealFunction.nOfOutputs() != nOfOutputs(modules)) {
      throw new IllegalArgumentException(String.format(
          "Invalid function output size: %d found vs. %d expected",
          timedRealFunction.nOfInputs(),
          nOfOutputs(modules)
      ));
    }
    this.timedRealFunction = timedRealFunction;
  }
}
