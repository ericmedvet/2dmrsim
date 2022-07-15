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

package it.units.erallab.mrsim.core.actions;

import it.units.erallab.mrsim.core.ActionPerformer;
import it.units.erallab.mrsim.core.Agent;
import it.units.erallab.mrsim.core.SelfDescribedAction;
import it.units.erallab.mrsim.core.bodies.Anchor;
import it.units.erallab.mrsim.core.bodies.Anchorable;
import it.units.erallab.mrsim.engine.ActionException;

import java.util.Collection;
import java.util.Comparator;
import java.util.stream.Collectors;

/**
 * @author "Eric Medvet" on 2022/07/08 for 2dmrsim
 */
public record AttachAnchor(
    Anchor anchor,
    Anchorable anchorable,
    Anchor.Link.Type type
) implements SelfDescribedAction<Anchor.Link> {

  @Override
  public Anchor.Link perform(ActionPerformer performer, Agent agent) throws ActionException {
    // find already attached anchors
    Collection<Anchor> attachedAnchors = anchor.links().stream()
        .map(Anchor.Link::destination)
        .filter(a -> a.anchorable() == anchorable)
        .collect(Collectors.toSet());
    //find closest anchor on destination
    Anchor destination = anchorable.anchors().stream()
        .filter(a -> !attachedAnchors.contains(a))
        .min(Comparator.comparingDouble(a -> a.point().distance(anchor.point())))
        .orElse(null);
    //create link
    if (destination != null) {
      return performer.perform(new CreateLink(anchor, destination, type), agent).outcome().orElse(null);
    }
    return null;
  }
}
