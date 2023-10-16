/*-
 * ========================LICENSE_START=================================
 * mrsim2d-core
 * %%
 * Copyright (C) 2020 - 2023 Eric Medvet
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
import io.github.ericmedvet.mrsim2d.core.engine.ActionException;
import io.github.ericmedvet.mrsim2d.core.util.Pair;
import java.util.*;

public record AttractAnchorable(Collection<Anchor> anchors, Anchorable anchorable, double magnitude)
    implements SelfDescribedAction<Collection<Pair<Anchor, Anchor>>> {
  @Override
  public Collection<Pair<Anchor, Anchor>> perform(ActionPerformer performer, Agent agent)
      throws ActionException {
    // discard already attached
    Collection<Anchor> srcAnchors =
        anchors.stream()
            .filter(
                a ->
                    a.links().stream()
                        .map(l -> l.destination().anchorable())
                        .filter(dst -> dst == anchorable)
                        .toList()
                        .isEmpty())
            .toList();
    // match anchor pairs
    Collection<Anchor> dstAnchors = new LinkedHashSet<>(anchorable.anchors());
    Collection<Pair<Anchor, Anchor>> pairs = new ArrayList<>();
    srcAnchors.forEach(
        src -> {
          Optional<Anchor> closest =
              dstAnchors.stream()
                  .min(Comparator.comparingDouble(a -> a.point().distance(src.point())));
          if (closest.isPresent()) {
            pairs.add(new Pair<>(src, closest.get()));
            dstAnchors.remove(closest.get());
          }
        });
    // attract
    pairs.forEach(p -> performer.perform(new AttractAnchor(p.first(), p.second(), magnitude)));
    return pairs;
  }
}
