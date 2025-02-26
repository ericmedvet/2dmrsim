/*-
 * ========================LICENSE_START=================================
 * mrsim2d-core
 * %%
 * Copyright (C) 2020 - 2024 Eric Medvet
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

package io.github.ericmedvet.mrsim2d.core.actions;

import io.github.ericmedvet.jnb.datastructure.DoubleRange;
import io.github.ericmedvet.mrsim2d.core.ActionPerformer;
import io.github.ericmedvet.mrsim2d.core.Agent;
import io.github.ericmedvet.mrsim2d.core.SelfDescribedAction;
import io.github.ericmedvet.mrsim2d.core.bodies.Anchor;
import io.github.ericmedvet.mrsim2d.core.bodies.Anchorable;
import io.github.ericmedvet.mrsim2d.core.bodies.Voxel;
import java.util.Collection;
import java.util.List;
import java.util.function.DoubleUnaryOperator;

public record SenseSideAttachment(
    Voxel.Side side, Voxel body
) implements XMirrorableSense<Voxel>, SelfDescribedAction<Double> {

  @Override
  public Double perform(ActionPerformer performer, Agent agent) {
    // consider side anchors
    Collection<Anchor> anchors = body.anchorsOn(side);
    if (anchors.isEmpty()) {
      return 0d;
    }
    // find "most attached" other body
    List<Anchorable> anchorables = anchors.stream()
        .map(Anchor::attachedAnchorables)
        .flatMap(Collection::stream)
        .toList();
    if (anchorables.isEmpty()) {
      return 0d;
    }
    long maxAttachedAnchorsOfSameBody = anchorables.stream()
        .mapToLong(b -> anchors.stream().filter(a -> a.isAnchoredTo(b)).count())
        .max()
        .orElse(0);
    // return
    return (double) maxAttachedAnchorsOfSameBody / (double) anchors.size();
  }

  @Override
  public DoubleRange range() {
    return DoubleRange.UNIT;
  }

  @Override
  public Sense<Voxel> mirrored() {
    return new SenseSideAttachment(
        switch (side) {
          case E -> Voxel.Side.W;
          case W -> Voxel.Side.E;
          default -> side;
        },
        body
    );
  }

  @Override
  public DoubleUnaryOperator outcomeMirrorer() {
    return DoubleUnaryOperator.identity();
  }
}
