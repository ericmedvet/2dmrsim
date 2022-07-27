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

import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author "Eric Medvet" on 2022/07/07 for 2dmrsim
 */
public interface Anchorable extends Body {
  List<Anchor> anchors();

  default boolean isAnchoredTo(Anchor otherAnchor) {
    return anchors().stream().anyMatch(a -> a.isAnchoredTo(otherAnchor));
  }

  default boolean isAnchoredTo(Anchorable otherAnchorable) {
    return anchors().stream().anyMatch(a -> a.isAnchoredTo(otherAnchorable));
  }

  default Set<Anchor> attachedAnchors() {
    return anchors().stream().map(Anchor::attachedAnchors).flatMap(Collection::stream).collect(Collectors.toSet());
  }

  default Set<Anchorable> attachedAnchorables() {
    return anchors().stream().map(Anchor::attachedAnchorables).flatMap(Collection::stream).collect(Collectors.toSet());
  }
}
