/*
 * Copyright 2022 eric
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package it.units.erallab.mrsim2d.core.agents.legged;

import it.units.erallab.mrsim2d.core.Action;
import it.units.erallab.mrsim2d.core.ActionOutcome;
import it.units.erallab.mrsim2d.core.NumBrained;
import it.units.erallab.mrsim2d.core.Sensor;
import it.units.erallab.mrsim2d.core.actions.ActuateRotationalJoint;
import it.units.erallab.mrsim2d.core.actions.Sense;
import it.units.erallab.mrsim2d.core.bodies.Body;
import it.units.erallab.mrsim2d.core.functions.TimedRealFunction;
import it.units.erallab.mrsim2d.core.util.DoubleRange;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.IntStream;


/**
 * @author "Eric Medvet" on 2022/09/24 for 2dmrsim
 */
public class NumLeggedHybridRobot extends AbstractLeggedHybridRobot implements NumBrained {

  private final static DoubleRange ANGLE_RANGE = new DoubleRange(Math.toRadians(-90), Math.toRadians(90));
  private final static DoubleRange INPUT_RANGE = DoubleRange.SYMMETRIC_UNIT;
  private final static DoubleRange OUTPUT_RANGE = DoubleRange.SYMMETRIC_UNIT;

  private final TimedRealFunction timedRealFunction;
  private final List<Sensor<?>> headSensors;

  private double[] inputs;
  private double[] outputs;

  public NumLeggedHybridRobot(
      List<Leg> legs,
      double trunkLength,
      double trunkWidth,
      double trunkMass,
      double headMass,
      List<Sensor<?>> headSensors,
      TimedRealFunction timedRealFunction
  ) {
    super(legs, trunkLength, trunkWidth, trunkMass, headMass);
    this.timedRealFunction = timedRealFunction;
    this.headSensors = headSensors;
  }

  public static int nOfInputs(List<Leg> legs, List<Sensor<?>> headSensors) {
    return headSensors.size() + legs.stream()
        .mapToInt(l -> l.legChunks().stream().mapToInt(
            lc -> lc.jointSensors().size()).sum() + l.downConnectorSensors().size()
        )
        .sum();
  }

  public static int nOfOutputs(List<Leg> legs) {
    return legs.stream().mapToInt(m -> m.legChunks().size()).sum();
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
      inputs = new double[timedRealFunction.nOfInputs()];
    }
    //compute actuation
    outputs = Arrays.stream(timedRealFunction.apply(t, inputs)).map(OUTPUT_RANGE::clip).toArray();
    //generate next sense actions
    List<Action<?>> actions = new ArrayList<>();
    for (int il = 0; il < legs.size(); il = il + 1) {
      Leg leg = legs.get(il);
      LegBody legBody = legBodies.get(il);
      leg.downConnectorSensors().forEach(s -> actions.add(((Sensor<Body>) s).apply(legBody.downConnector())));
      for (int ic = 0; ic < leg.legChunks().size(); ic = ic + 1) {
        LegChunk legChunk = leg.legChunks().get(ic);
        LegChunkBody legChunkBody = legBody.legChunks().get(ic);
        legChunk.jointSensors().forEach(s -> actions.add(((Sensor<Body>) s).apply(legChunkBody.joint())));
      }
    }
    headSensors.forEach(s -> actions.add(((Sensor<Body>) s).apply(head)));
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
  public TimedRealFunction brain() {
    return timedRealFunction;
  }

  @Override
  public BrainIO brainIO() {
    return new BrainIO(new RangedValues(inputs, INPUT_RANGE), new RangedValues(outputs, OUTPUT_RANGE));
  }

}
