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

import io.github.ericmedvet.mrsim2d.core.ActionPerformer;
import io.github.ericmedvet.mrsim2d.core.Agent;
import io.github.ericmedvet.mrsim2d.core.SelfDescribedAction;
import io.github.ericmedvet.mrsim2d.core.bodies.Anchor;
import io.github.ericmedvet.mrsim2d.core.bodies.Anchorable;
import java.util.Collection;
import java.util.Comparator;

public record AttachAnchor(Anchor anchor, Anchorable anchorable, Anchor.Link.Type type)
    implements SelfDescribedAction<Anchor.Link> {

  @Override
  public Anchor.Link perform(ActionPerformer performer, Agent agent) {
    // find already attached anchors
    Collection<Anchor> attachedAnchors = anchor.links().stream()
        .map(Anchor.Link::destination)
        .filter(a -> a.anchorable() == anchorable)
        .distinct()
        .toList();
    // find closest anchor on destination
    Anchor destination = anchorable.anchors().stream()
        .filter(a -> !attachedAnchors.contains(a))
        .min(Comparator.comparingDouble(a -> a.point().distance(anchor.point())))
        .orElse(null);
    // create link
    if (destination != null) {
      return performer
          .perform(new CreateLink(anchor, destination, type), agent)
          .outcome()
          .orElse(null);
    }
    return null;
  }
}
