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

package io.github.ericmedvet.mrsim2d.core.agents.gridvsr;

import io.github.ericmedvet.jsdynsym.grid.Grid;
import io.github.ericmedvet.mrsim2d.core.ActionPerformer;
import io.github.ericmedvet.mrsim2d.core.EmbodiedAgent;
import io.github.ericmedvet.mrsim2d.core.actions.AttachClosestAnchors;
import io.github.ericmedvet.mrsim2d.core.actions.CreateRigidBody;
import io.github.ericmedvet.mrsim2d.core.actions.CreateVoxel;
import io.github.ericmedvet.mrsim2d.core.actions.TranslateBodyAt;
import io.github.ericmedvet.mrsim2d.core.bodies.Anchor;
import io.github.ericmedvet.mrsim2d.core.bodies.Anchorable;
import io.github.ericmedvet.mrsim2d.core.bodies.Body;
import io.github.ericmedvet.mrsim2d.core.geometry.Point;
import io.github.ericmedvet.mrsim2d.core.geometry.Poly;

import java.util.List;
import java.util.Objects;

/**
 * @author "Eric Medvet" on 2022/07/09 for 2dmrsim
 */
public abstract class AbstractGridVSR implements EmbodiedAgent {

  protected final static double VOXEL_SIDE_LENGTH = 1d;
  protected final static double VOXEL_MASS = 1d;
  protected final static Anchor.Link.Type LINK_TYPE = Anchor.Link.Type.RIGID;
  protected final Grid<Anchorable> bodyGrid;
  private final Grid<GridBody.Element> elementGrid;
  private final double voxelSideLength;
  private final double voxelMass;

  public AbstractGridVSR(Grid<GridBody.Element> elementGrid, double voxelSideLength, double voxelMass) {
    this.elementGrid = elementGrid;
    this.voxelSideLength = voxelSideLength;
    this.voxelMass = voxelMass;
    bodyGrid = Grid.create(elementGrid.w(), elementGrid.h());
  }

  @Override
  public void assemble(ActionPerformer actionPerformer) {
    //create and translate
    elementGrid.entries().stream()
        .filter(e -> !e.value().type().equals(GridBody.VoxelType.NONE))
        .forEach(e -> {
          Anchorable anchorable = switch (e.value().type()) {
            case SOFT -> actionPerformer.perform(new CreateVoxel(
                voxelSideLength,
                voxelMass,
                e.value().material()
            ), this).outcome().orElseThrow();
            case RIGID -> actionPerformer.perform(new CreateRigidBody(
                Poly.square(voxelSideLength),
                voxelMass,
                3d / voxelSideLength
            ), this).outcome().orElseThrow();
            default -> throw new IllegalStateException("Unexpected value: " + e.value().type());
          };
          actionPerformer.perform(new TranslateBodyAt(
              anchorable,
              new Point(e.key().x() * voxelSideLength, e.key().y() * voxelSideLength)
          ), this);
          bodyGrid.set(e.key(), anchorable);
        });
    //attach
    for (Grid.Key key : bodyGrid.keys()) {
      Anchorable srcBody = bodyGrid.get(key);
      if (srcBody != null) {
        if (bodyGrid.isValid(key.translated(1, 0))) {
          Anchorable other = bodyGrid.get(key.translated(1, 0));
          if (other != null) {
            actionPerformer.perform(new AttachClosestAnchors(2, srcBody, other, LINK_TYPE), this);
          }
        }
        if (bodyGrid.isValid(key.translated(0, 1))) {
          Anchorable other = bodyGrid.get(key.translated(0, 1));
          if (other != null) {
            actionPerformer.perform(new AttachClosestAnchors(2, srcBody, other, LINK_TYPE), this);
          }
        }
      }
    }
  }

  @SuppressWarnings({"unchecked", "rawtypes"})
  @Override
  public List<Body> bodyParts() {
    return (List) bodyGrid.values().stream().filter(Objects::nonNull).toList();
  }

  public Grid<GridBody.Element> getElementGrid() {
    return elementGrid;
  }
}
