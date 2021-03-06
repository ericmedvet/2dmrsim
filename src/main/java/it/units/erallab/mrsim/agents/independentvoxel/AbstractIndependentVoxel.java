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
import it.units.erallab.mrsim.core.ActionPerformer;
import it.units.erallab.mrsim.core.EmbodiedAgent;
import it.units.erallab.mrsim.core.actions.CreateVoxel;
import it.units.erallab.mrsim.core.bodies.Body;
import it.units.erallab.mrsim.core.bodies.Voxel;
import it.units.erallab.mrsim.engine.ActionException;

import java.util.List;
import java.util.Optional;

/**
 * @author "Eric Medvet" on 2022/07/13 for 2dmrsim
 */
public abstract class AbstractIndependentVoxel implements EmbodiedAgent {

  protected final static double VOXEL_SIDE_LENGTH = 1d;
  protected final static double VOXEL_MASS = 1d;

  private final Voxel.Material material;
  private final double voxelSideLength;
  private final double voxelMass;

  protected Voxel voxel;

  public AbstractIndependentVoxel(Voxel.Material material, double voxelSideLength, double voxelMass) {
    this.material = material;
    this.voxelSideLength = voxelSideLength;
    this.voxelMass = voxelMass;
  }

  @Override
  public List<Body> bodyParts() {
    return List.of(voxel);
  }

  @Override
  public void assemble(ActionPerformer actionPerformer) throws ActionException {
    CreateVoxel action = new CreateVoxel(voxelSideLength, voxelMass, material);
    Optional<Voxel> optional = actionPerformer.perform(action, this).outcome();
    if (optional.isPresent()) {
      voxel = optional.get();
    } else {
      throw new ActionException(action, "Voxel creation failed");
    }
  }
}
