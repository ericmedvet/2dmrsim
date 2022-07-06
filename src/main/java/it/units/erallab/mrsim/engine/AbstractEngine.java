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

package it.units.erallab.mrsim.engine;

import com.google.common.util.concurrent.AtomicDouble;
import it.units.erallab.mrsim.core.*;
import it.units.erallab.mrsim.core.action.AddAgent;
import it.units.erallab.mrsim.core.body.Body;
import it.units.erallab.mrsim.util.Pair;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

/**
 * @author "Eric Medvet" on 2022/07/06 for 2dmrsim
 */
public abstract class AbstractEngine implements Environment {

  protected final AtomicDouble t;
  protected final List<Pair<Agent, List<ActionOutcome<?>>>> agentPairs;

  public AbstractEngine() {
    agentPairs = new ArrayList<>();
    t = new AtomicDouble(0d);
  }

  @SuppressWarnings({"unchecked", "rawtypes"})
  @Override
  public Snapshot tick() {
    for (int i = 0; i < agentPairs.size(); i++) {
      List<ActionOutcome<?>> outcomes = new ArrayList<>();
      for (Action<?> action : agentPairs.get(i).first().act(t.get(), agentPairs.get(i).second())) {
        outcomes.add(new ActionOutcome<>(action, (Optional) perform(action, agentPairs.get(i).first())));
      }
      Pair<Agent, List<ActionOutcome<?>>> pair = new Pair<>(
          agentPairs.get(i).first(),
          outcomes
      );
      agentPairs.set(i, pair);
    }
    return new Snapshot(
        innerTick(),
        agentPairs,
        getBodies()
    );
  }

  @Override
  public <O> Optional<O> perform(Action<O> action, Agent agent) {
    try {
      O o = innerPerform(action, agent);
      return o == null ? Optional.empty() : Optional.of(o);
    } catch (UnsupportedActionException e) {
      // TODO add logging
      return Optional.empty();
    }
  }

  protected abstract double innerTick();

  protected abstract Collection<Body<?>> getBodies();

  protected <O> O innerPerform(Action<O> action, Agent agent) throws UnsupportedActionException {
    if (action instanceof AddAgent a) {
      agentPairs.add(new Pair<>(a.agent(), List.of()));
      return null;
    }
    throw new UnsupportedActionException(action);
  }

}
