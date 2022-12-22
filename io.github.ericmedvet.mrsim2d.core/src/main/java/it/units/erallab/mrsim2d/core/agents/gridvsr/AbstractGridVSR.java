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

package it.units.erallab.mrsim2d.core.agents.gridvsr;

import it.units.erallab.mrsim2d.core.ActionPerformer;
import it.units.erallab.mrsim2d.core.EmbodiedAgent;
import it.units.erallab.mrsim2d.core.actions.AttachClosestAnchors;
import it.units.erallab.mrsim2d.core.actions.CreateAndTranslateVoxel;
import it.units.erallab.mrsim2d.core.bodies.Anchor;
import it.units.erallab.mrsim2d.core.bodies.Body;
import it.units.erallab.mrsim2d.core.bodies.Voxel;
import it.units.erallab.mrsim2d.core.geometry.Point;
import it.units.erallab.mrsim2d.core.util.Grid;

import java.util.List;
import java.util.Objects;

/**
 * @author "Eric Medvet" on 2022/07/09 for 2dmrsim
 */
public abstract class AbstractGridVSR implements EmbodiedAgent {

  protected final static double VOXEL_SIDE_LENGTH = 1d;
  protected final static double VOXEL_MASS = 1d;
  protected final static Anchor.Link.Type LINK_TYPE = Anchor.Link.Type.RIGID;
  protected final Grid<Voxel> voxelGrid;
  private final Grid<Voxel.Material> materialGrid;
  private final double voxelSideLength;
  private final double voxelMass;

  public AbstractGridVSR(Grid<Voxel.Material> materialGrid, double voxelSideLength, double voxelMass) {
    this.materialGrid = materialGrid;
    this.voxelSideLength = voxelSideLength;
    this.voxelMass = voxelMass;
    voxelGrid = Grid.create(materialGrid.w(), materialGrid.h());
  }

  @Override
  public void assemble(ActionPerformer actionPerformer) {
    //create and translate
    materialGrid.entries().stream()
        .filter(e -> e.value() != null)
        .forEach(e -> voxelGrid.set(e.key(), actionPerformer.perform(new CreateAndTranslateVoxel(
            voxelSideLength,
            voxelMass,
            e.value(),
            new Point(e.key().x() * voxelSideLength, e.key().y() * voxelSideLength)
        ), this).outcome().orElseThrow()));
    //attach
    for (Grid.Key key : voxelGrid.keys()) {
      Voxel srcVoxel = voxelGrid.get(key);
      if (srcVoxel != null) {
        Voxel onEast = voxelGrid.get(key.x() + 1, key.y());
        Voxel onSouth = voxelGrid.get(key.x(), key.y() + 1);
        if (onEast != null) {
          actionPerformer.perform(new AttachClosestAnchors(2, srcVoxel, onEast, LINK_TYPE), this);
        }
        if (onSouth != null) {
          actionPerformer.perform(new AttachClosestAnchors(2, srcVoxel, onSouth, LINK_TYPE), this);
        }
      }
    }
  }

  @SuppressWarnings({"unchecked", "rawtypes"})
  @Override
  public List<Body> bodyParts() {
    return (List) voxelGrid.values().stream().filter(Objects::nonNull).toList();
  }
}
