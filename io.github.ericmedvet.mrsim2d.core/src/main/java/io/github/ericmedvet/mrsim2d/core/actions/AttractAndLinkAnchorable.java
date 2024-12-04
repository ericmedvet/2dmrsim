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

import io.github.ericmedvet.jnb.datastructure.Pair;
import io.github.ericmedvet.mrsim2d.core.ActionPerformer;
import io.github.ericmedvet.mrsim2d.core.Agent;
import io.github.ericmedvet.mrsim2d.core.SelfDescribedAction;
import io.github.ericmedvet.mrsim2d.core.bodies.Anchor;
import io.github.ericmedvet.mrsim2d.core.bodies.Anchorable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;

public record AttractAndLinkAnchorable(
        Collection<Anchor> anchors, Anchorable anchorable, double magnitude, Anchor.Link.Type type)
        implements SelfDescribedAction<Map<Pair<Anchor, Anchor>, AttractAndLinkAnchor.Outcome>> {

    @Override
    public Map<Pair<Anchor, Anchor>, AttractAndLinkAnchor.Outcome> perform(ActionPerformer performer, Agent agent) {
        // discard already attached
        Collection<Anchor> srcAnchors = anchors.stream()
                .filter(a -> a.links().stream()
                        .map(l -> l.destination().anchorable())
                        .filter(dst -> dst == anchorable)
                        .toList()
                        .isEmpty())
                .toList();
        // match anchor pairs
        Collection<Anchor> dstAnchors = new LinkedHashSet<>(anchorable.anchors());
        Collection<Pair<Anchor, Anchor>> pairs = new ArrayList<>();
        srcAnchors.forEach(src -> dstAnchors.stream()
                .min(Comparator.comparingDouble(a -> a.point().distance(src.point())))
                .ifPresent(dstAnchor -> {
                    pairs.add(new Pair<>(src, dstAnchor));
                    dstAnchors.remove(dstAnchor);
                }));
        // attract and link
        Map<Pair<Anchor, Anchor>, AttractAndLinkAnchor.Outcome> map = new LinkedHashMap<>();
        for (Pair<Anchor, Anchor> pair : pairs) {
            performer
                    .perform(new AttractAndLinkAnchor(pair.first(), pair.second(), magnitude, type), agent)
                    .outcome()
                    .ifPresent(outcome -> map.put(pair, outcome));
        }
        return map;
    }
}
