/*-
 * ========================LICENSE_START=================================
 * mrsim2d-core
 * %%
 * Copyright (C) 2020 - 2025 Eric Medvet
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

package io.github.ericmedvet.mrsim2d.core.bodies;

import io.github.ericmedvet.mrsim2d.core.geometry.Point;
import java.util.Collection;

public interface Anchor {

  record Link(Anchor source, Anchor destination, Type type) {
    public enum Type {
      RIGID, SOFT
    }

    public Link reversed() {
      return new Link(destination, source, type);
    }
  }

  Anchorable anchorable();

  Collection<Anchor.Link> links();

  Point point();

  default Collection<Anchorable> attachedAnchorables() {
    return links().stream().map(l -> l.destination.anchorable()).distinct().toList();
  }

  default Collection<Anchor> attachedAnchors() {
    return links().stream().map(l -> l.destination).toList();
  }

  default boolean isAnchoredTo(Anchor otherAnchor) {
    return links().stream().anyMatch(l -> l.destination().equals(otherAnchor));
  }

  default boolean isAnchoredTo(Anchorable otherAnchorable) {
    return links().stream().anyMatch(l -> l.destination().anchorable().equals(otherAnchorable));
  }
}
