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

package io.github.ericmedvet.mrsim2d.core.actions;

import io.github.ericmedvet.mrsim2d.core.Action;
import io.github.ericmedvet.mrsim2d.core.bodies.Anchor;
import io.github.ericmedvet.mrsim2d.core.util.Pair;

import java.util.Collection;
import java.util.Map;

/**
 * @author "Eric Medvet" on 2022/07/13 for 2dmrsim
 */
public record AttractAndLinkClosestAnchorable(
    Collection<Anchor> anchors, double magnitude, Anchor.Link.Type type
) implements Action<Map<Pair<Anchor, Anchor>, AttractAndLinkAnchor.Outcome>> {
}
