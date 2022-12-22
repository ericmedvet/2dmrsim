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

package io.github.ericmedvet.mrsim2d.core.agents.independentvoxel;

import io.github.ericmedvet.mrsim2d.core.Action;
import io.github.ericmedvet.mrsim2d.core.ActionOutcome;
import io.github.ericmedvet.mrsim2d.core.NumBrained;
import io.github.ericmedvet.mrsim2d.core.Sensor;
import io.github.ericmedvet.mrsim2d.core.actions.ActuateVoxel;
import io.github.ericmedvet.mrsim2d.core.actions.AttractAndLinkClosestAnchorable;
import io.github.ericmedvet.mrsim2d.core.actions.DetachAnchors;
import io.github.ericmedvet.mrsim2d.core.actions.Sense;
import io.github.ericmedvet.mrsim2d.core.bodies.Anchor;
import io.github.ericmedvet.mrsim2d.core.bodies.Voxel;
import io.github.ericmedvet.mrsim2d.core.functions.TimedRealFunction;
import io.github.ericmedvet.mrsim2d.core.util.DoubleRange;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @author "Eric Medvet" on 2022/07/13 for 2dmrsim
 */
public class NumIndependentVoxel extends AbstractIndependentVoxel implements NumBrained {

  private final static DoubleRange INPUT_RANGE = DoubleRange.SYMMETRIC_UNIT;
  private final static DoubleRange OUTPUT_RANGE = DoubleRange.SYMMETRIC_UNIT;
  private final static double ATTACH_ACTION_THRESHOLD = 0.1d;
  private final static int N_OF_OUTPUTS = 8;

  private final List<Sensor<? super Voxel>> sensors;
  private final double[] inputs;
  private final TimedRealFunction timedRealFunction;
  private double[] outputs;

  public NumIndependentVoxel(
      Voxel.Material material,
      double voxelSideLength,
      double voxelMass,
      List<Sensor<? super Voxel>> sensors,
      TimedRealFunction timedRealFunction
  ) {
    super(material, voxelSideLength, voxelMass);
    this.sensors = sensors;
    inputs = new double[sensors.size()];
    timedRealFunction.checkDimension(nOfInputs(sensors), nOfOutputs());
    this.timedRealFunction = timedRealFunction;
  }

  public NumIndependentVoxel(List<Sensor<? super Voxel>> sensors, TimedRealFunction timedRealFunction) {
    this(new Voxel.Material(), VOXEL_SIDE_LENGTH, VOXEL_MASS, sensors, timedRealFunction);
  }

  public static int nOfInputs(List<Sensor<? super Voxel>> sensors) {
    return sensors.size();
  }

  public static int nOfOutputs() {
    return N_OF_OUTPUTS;
  }

  @Override
  public List<? extends Action<?>> act(double t, List<ActionOutcome<?, ?>> previousActionOutcomes) {
    //read inputs from last request
    double[] readInputs = previousActionOutcomes.stream()
        .filter(ao -> ao.action() instanceof Sense)
        .mapToDouble(ao -> {
          @SuppressWarnings("unchecked") ActionOutcome<Sense<? super Voxel>, Double> so = (ActionOutcome<Sense<?
              super Voxel>, Double>) ao;
          return INPUT_RANGE.denormalize(so.action().range().normalize(so.outcome().orElse(0d)));
        })
        .toArray();
    System.arraycopy(readInputs, 0, inputs, 0, readInputs.length);
    //compute actuation
    outputs = Arrays.stream(timedRealFunction.apply(t, inputs)).map(OUTPUT_RANGE::clip).toArray();
    //generate next sense actions
    List<Action<?>> actions = new ArrayList<>(sensors.stream().map(f -> f.apply(voxel)).toList());
    //generate actuation actions
    actions.add(new ActuateVoxel(voxel, outputs[0], outputs[1], outputs[2], outputs[3]));
    for (int i = 0; i < Voxel.Side.values().length; i++) {
      Voxel.Side side = Voxel.Side.values()[i];
      double m = outputs[i + 4];
      if (m > ATTACH_ACTION_THRESHOLD) {
        actions.add(new AttractAndLinkClosestAnchorable(voxel.anchorsOn(side), 1, Anchor.Link.Type.SOFT));
      } else if (m < -ATTACH_ACTION_THRESHOLD) {
        actions.add(new DetachAnchors(voxel.anchorsOn(side)));
      }
    }
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
