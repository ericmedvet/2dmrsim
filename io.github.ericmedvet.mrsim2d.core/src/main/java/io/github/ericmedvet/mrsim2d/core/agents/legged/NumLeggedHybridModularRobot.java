
package io.github.ericmedvet.mrsim2d.core.agents.legged;

import io.github.ericmedvet.jsdynsym.core.DoubleRange;
import io.github.ericmedvet.jsdynsym.core.numerical.NumericalDynamicalSystem;
import io.github.ericmedvet.mrsim2d.core.Action;
import io.github.ericmedvet.mrsim2d.core.ActionOutcome;
import io.github.ericmedvet.mrsim2d.core.NumBrained;
import io.github.ericmedvet.mrsim2d.core.Sensor;
import io.github.ericmedvet.mrsim2d.core.actions.ActuateRotationalJoint;
import io.github.ericmedvet.mrsim2d.core.actions.Sense;
import io.github.ericmedvet.mrsim2d.core.bodies.Body;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.IntStream;

public class NumLeggedHybridModularRobot extends AbstractLeggedHybridModularRobot implements NumBrained {

  private final static DoubleRange ANGLE_RANGE = new DoubleRange(Math.toRadians(-90), Math.toRadians(90));
  private final static DoubleRange INPUT_RANGE = DoubleRange.SYMMETRIC_UNIT;
  private final static DoubleRange OUTPUT_RANGE = DoubleRange.SYMMETRIC_UNIT;

  private final NumericalDynamicalSystem<?> numericalDynamicalSystem;

  private double[] inputs;
  private double[] outputs;

  public NumLeggedHybridModularRobot(List<Module> modules, NumericalDynamicalSystem<?> numericalDynamicalSystem) {
    super(modules);
    numericalDynamicalSystem.checkDimension(nOfInputs(modules), nOfOutputs(modules));
    this.numericalDynamicalSystem = numericalDynamicalSystem;
  }

  public static int nOfInputs(List<Module> modules) {
    return modules.stream()
        .mapToInt(m -> m.trunkSensors().size() + m.downConnectorSensors().size() + m.rightConnectorSensors()
            .size() + m.legChunks().stream()
            .mapToInt(c -> c.jointSensors().size())
            .sum()
        )
        .sum();
  }

  public static int nOfOutputs(List<Module> modules) {
    return modules.stream().mapToInt(m -> m.legChunks().size()).sum();
  }

  @SuppressWarnings("unchecked")
  @Override
  public List<? extends Action<?>> act(double t, List<ActionOutcome<?, ?>> previousActionOutcomes) {
    //read inputs from last request
    inputs = previousActionOutcomes.stream()
        .filter(ao -> ao.action() instanceof Sense)
        .mapToDouble(ao -> {
          @SuppressWarnings("unchecked") ActionOutcome<Sense<?>, Double> so = (ActionOutcome<Sense<?>, Double>) ao;
          return INPUT_RANGE.denormalize(so.action().range().normalize(so.outcome().orElse(0d)));
        })
        .toArray();
    if (inputs.length == 0) {
      inputs = new double[numericalDynamicalSystem.nOfInputs()];
    }
    //compute actuation
    outputs = Arrays.stream(numericalDynamicalSystem.step(t, inputs)).map(OUTPUT_RANGE::clip).toArray();
    //generate next sense actions
    List<Action<?>> actions = new ArrayList<>();
    for (int im = 0; im < modules.size(); im = im + 1) {
      Module module = modules.get(im);
      ModuleBody moduleBody = moduleBodies.get(im);
      module.trunkSensors().forEach(s -> actions.add(((Sensor<Body>) s).apply(moduleBody.trunk())));
      module.downConnectorSensors().forEach(s -> actions.add(((Sensor<Body>) s).apply(moduleBody.downConnector())));
      module.rightConnectorSensors().forEach(s -> actions.add(((Sensor<Body>) s).apply(moduleBody.rightConnector())));
      for (int ic = 0; ic < module.legChunks().size(); ic = ic + 1) {
        LegChunk legChunk = module.legChunks().get(ic);
        LegChunkBody legChunkBody = moduleBody.legChunks().get(ic);
        legChunk.jointSensors().forEach(s -> actions.add(((Sensor<Body>) s).apply(legChunkBody.joint())));
      }
    }
    //generate actuation actions
    IntStream.range(0, outputs.length)
        .forEach(i -> actions.add(new ActuateRotationalJoint(
                rotationalJoints.get(i),
                ANGLE_RANGE.denormalize(OUTPUT_RANGE.normalize(outputs[i]))
            )
        ));
    return actions;
  }

  @Override
  public NumericalDynamicalSystem<?> brain() {
    return numericalDynamicalSystem;
  }

  @Override
  public BrainIO brainIO() {
    return new BrainIO(new RangedValues(inputs, INPUT_RANGE), new RangedValues(outputs, OUTPUT_RANGE));
  }

}
