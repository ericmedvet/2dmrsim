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
import it.units.erallab.mrsim.core.bodies.Voxel;
import it.units.erallab.mrsim.util.TimedRealFunction;

import java.util.List;

/**
 * @author "Eric Medvet" on 2022/07/13 for 2dmrsim
 */
public class NumIndependentVoxel extends AbstractIndependentVoxel {

  private final TimedRealFunction function;

  public NumIndependentVoxel(
      Voxel.Material material,
      double voxelSideLength,
      double voxelMass,
      TimedRealFunction function
  ) {
    super(material, voxelSideLength, voxelMass);
    this.function = function;
  }

  public NumIndependentVoxel(TimedRealFunction function) {
    this(new Voxel.Material(), VOXEL_SIDE_LENGTH, VOXEL_MASS, function);
  }

  @Override
  public List<Action<?>> act(double t, List<ActionOutcome<?, ?>> previousActionOutcomes) {
    //read inputs from last request
    //compute actuation
    //generate actuation actions
    return null;
  }
}
