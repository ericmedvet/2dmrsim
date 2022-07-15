/*
 * Copyright 2022 Eric Medvet <eric.medvet@gmail.com> (as eric)
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

package it.units.erallab.mrsim.agents.independentvoxel;

import it.units.erallab.mrsim.core.Action;
import it.units.erallab.mrsim.core.ActionOutcome;
import it.units.erallab.mrsim.core.actions.ActuateVoxel;
import it.units.erallab.mrsim.core.actions.AttractAndLinkClosestAnchorable;
import it.units.erallab.mrsim.core.actions.DetachAnchors;
import it.units.erallab.mrsim.core.actions.Sense;
import it.units.erallab.mrsim.core.bodies.Anchor;
import it.units.erallab.mrsim.core.bodies.Voxel;
import it.units.erallab.mrsim.util.TimedRealFunction;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @author "Eric Medvet" on 2022/07/13 for 2dmrsim
 */
public class NumIndependentVoxel extends AbstractIndependentVoxel {

  private final static double ATTACH_ACTION_THRESHOLD = 0.1d;

  private final List<Function<Voxel, Sense<? super Voxel>>> sensors;
  private final TimedRealFunction function;

  private final double[] inputs;

  public NumIndependentVoxel(
      Voxel.Material material,
      double voxelSideLength,
      double voxelMass,
      List<Function<Voxel, Sense<? super Voxel>>> sensors,
      TimedRealFunction function
  ) {
    super(material, voxelSideLength, voxelMass);
    this.sensors = sensors;
    this.function = function;
    inputs = new double[sensors.size()];
    if (function.nOfInputs() != inputs.length) {
      throw new IllegalArgumentException(String.format(
          "Invalid function input size: %d found vs. %d expected",
          function.nOfInputs(),
          inputs.length
      ));
    }
    if (function.nOfOutputs() != 8) {
      throw new IllegalArgumentException(String.format(
          "Invalid function output size: %d found vs. 8 expected",
          function.nOfInputs()
      ));
    }
  }

  public NumIndependentVoxel(List<Function<Voxel, Sense<? super Voxel>>> sensors, TimedRealFunction function) {
    this(new Voxel.Material(), VOXEL_SIDE_LENGTH, VOXEL_MASS, sensors, function);
  }

  @SuppressWarnings("unchecked")
  @Override
  public List<Action<?>> act(double t, List<ActionOutcome<?, ?>> previousActionOutcomes) {
    //read inputs from last request
    double[] readInputs = previousActionOutcomes.stream()
        .filter(ao -> ao.action() instanceof Sense)
        .mapToDouble(ao -> {
          ActionOutcome<Sense<? super Voxel>, Double> so = (ActionOutcome<Sense<? super Voxel>, Double>) ao;
          return so.action().range().normalize(so.outcome().orElse(0d));
        })
        .toArray();
    System.arraycopy(readInputs, 0, inputs, 0, readInputs.length);
    //compute actuation
    double[] outputs = function.apply(t, inputs);
    System.out.printf(
        "%10.10s -> %s%n",
        hashCode(),
        Arrays.stream(inputs).mapToObj(d -> String.format("%5.2f", d)).collect(
            Collectors.joining(" "))
    ); // TODO remove
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
}
