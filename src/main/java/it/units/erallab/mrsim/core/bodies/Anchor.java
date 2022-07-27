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

package it.units.erallab.mrsim.core.bodies;

import it.units.erallab.mrsim.core.geometry.Point;

import java.util.Collection;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author "Eric Medvet" on 2022/07/07 for 2dmrsim
 */
public interface Anchor {

  record Link(Anchor source, Anchor destination, Type type) {
    public enum Type {RIGID, SOFT}

    public Link reversed() {
      return new Link(destination, source, type);
    }
  }


  Point point();

  Anchorable anchorable();

  Collection<Anchor.Link> links();

  default boolean isAnchoredTo(Anchor otherAnchor) {
    return links().stream().anyMatch(l -> l.destination().equals(otherAnchor));
  }

  default boolean isAnchoredTo(Anchorable otherAnchorable) {
    return links().stream().anyMatch(l -> l.destination().anchorable().equals(otherAnchorable));
  }

  default Set<Anchor> attachedAnchors() {
    return links().stream().map(l -> l.destination).collect(Collectors.toSet());
  }

  default Set<Anchorable> attachedAnchorables() {
    return links().stream().map(l -> l.destination.anchorable()).collect(Collectors.toSet());
  }

}
