/*-
 * ========================LICENSE_START=================================
 * mrsim2d-core
 * %%
 * Copyright (C) 2020 - 2023 Eric Medvet
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * =========================LICENSE_END==================================
 */

package io.github.ericmedvet.mrsim2d.core.agents.independentvoxel;

import io.github.ericmedvet.mrsim2d.core.ActionPerformer;
import io.github.ericmedvet.mrsim2d.core.EmbodiedAgent;
import io.github.ericmedvet.mrsim2d.core.actions.CreateVoxel;
import io.github.ericmedvet.mrsim2d.core.bodies.Body;
import io.github.ericmedvet.mrsim2d.core.bodies.Voxel;
import io.github.ericmedvet.mrsim2d.core.engine.ActionException;
import java.util.List;
import java.util.Optional;

public abstract class AbstractIndependentVoxel implements EmbodiedAgent {

  protected static final double VOXEL_SIDE_LENGTH = 1d;
  protected static final double VOXEL_MASS = 1d;

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
  public void assemble(ActionPerformer actionPerformer) throws ActionException {
    CreateVoxel action = new CreateVoxel(voxelSideLength, voxelMass, material);
    Optional<Voxel> optional = actionPerformer.perform(action, this).outcome();
    if (optional.isPresent()) {
      voxel = optional.get();
    } else {
      throw new ActionException(action, "Voxel creation failed");
    }
  }

  @Override
  public List<Body> bodyParts() {
    return List.of(voxel);
  }

  public Voxel voxel() {
    return voxel;
  }
}
