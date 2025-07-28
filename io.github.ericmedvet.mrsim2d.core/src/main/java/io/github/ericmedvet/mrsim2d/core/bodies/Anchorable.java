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

import java.util.Collection;
import java.util.List;

public interface Anchorable extends Body {
  List<Anchor> anchors();

  default Collection<Anchorable> attachedAnchorables() {
    return anchors().stream()
        .map(Anchor::attachedAnchorables)
        .flatMap(Collection::stream)
        .distinct()
        .toList();
  }

  default Collection<Anchor> attachedAnchors() {
    return anchors().stream()
        .map(Anchor::attachedAnchors)
        .flatMap(Collection::stream)
        .distinct()
        .toList();
  }

  default Collection<Anchor> attachedTo(Anchor otherAnchor) {
    return anchors().stream().filter(a -> a.isAnchoredTo(otherAnchor)).toList();
  }

  default Collection<Anchor> attachedTo(Anchorable otherAnchorable) {
    return anchors().stream().filter(a -> a.isAnchoredTo(otherAnchorable)).toList();
  }

  default boolean isAnchoredTo(Anchor otherAnchor) {
    return anchors().stream().anyMatch(a -> a.isAnchoredTo(otherAnchor));
  }

  default boolean isAnchoredTo(Anchorable otherAnchorable) {
    return anchors().stream().anyMatch(a -> a.isAnchoredTo(otherAnchorable));
  }
}
