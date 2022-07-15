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

import it.units.erallab.mrsim.core.*;
import it.units.erallab.mrsim.core.actions.*;
import it.units.erallab.mrsim.core.bodies.Anchor;
import it.units.erallab.mrsim.core.bodies.Anchorable;
import it.units.erallab.mrsim.core.bodies.Body;
import it.units.erallab.mrsim.util.AtomicDouble;
import it.units.erallab.mrsim.util.Pair;
import it.units.erallab.mrsim.util.PolyUtils;
import it.units.erallab.mrsim.util.Profiled;

import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Logger;

/**
 * @author "Eric Medvet" on 2022/07/06 for 2dmrsim
 */
public abstract class AbstractEngine implements Engine, Profiled {

  private final static Configuration DEFAULT_CONFIGURATION = new Configuration(
      2,
      1.5,
      5
  );

  public record Configuration(
      double attractionRange,
      double attractLinkRangeRatio,
      double bodyFindRange
  ) {}

  @FunctionalInterface
  protected interface ActionSolver<A extends Action<O>, O> {
    O solve(A action, Agent agent) throws ActionException;
  }

  private final Configuration configuration;

  protected final AtomicDouble t;
  protected final List<Body> bodies;
  protected final List<Pair<Agent, List<ActionOutcome<?, ?>>>> agentPairs;
  private final Map<Class<? extends Action<?>>, ActionSolver<?, ?>> actionSolvers;
  private final static Logger L = Logger.getLogger(AbstractEngine.class.getName());

  private final AtomicInteger nOfTicks;
  private final AtomicDouble engineT;
  private final Instant startingInstant;
  private final AtomicInteger nOfActions;
  private final AtomicInteger nOfUnsupportedActions;
  private final AtomicInteger nOfIllegalActions;
  private final List<ActionOutcome<?, ?>> lastTickPerformedActions;


  public AbstractEngine(Configuration configuration) {
    this.configuration = configuration;
    bodies = new ArrayList<>();
    agentPairs = new ArrayList<>();
    actionSolvers = new LinkedHashMap<>();
    t = new AtomicDouble(0d);
    nOfTicks = new AtomicInteger(0);
    engineT = new AtomicDouble(0d);
    startingInstant = Instant.now();
    nOfActions = new AtomicInteger(0);
    nOfUnsupportedActions = new AtomicInteger(0);
    nOfIllegalActions = new AtomicInteger(0);
    lastTickPerformedActions = new ArrayList<>();
    registerActionSolvers();
  }

  public AbstractEngine() {
    this(DEFAULT_CONFIGURATION);
  }

  protected Configuration configuration() {
    return configuration;
  }

  @Override
  public Snapshot tick() {
    Instant tickStartingInstant = Instant.now();
    nOfTicks.incrementAndGet();
    for (int i = 0; i < agentPairs.size(); i++) {
      List<ActionOutcome<?, ?>> outcomes = new ArrayList<>();
      for (Action<?> action : agentPairs.get(i).first().act(t.get(), agentPairs.get(i).second())) {
        outcomes.add(perform(action, agentPairs.get(i).first()));
      }
      Pair<Agent, List<ActionOutcome<?, ?>>> pair = new Pair<>(agentPairs.get(i).first(), outcomes);
      agentPairs.set(i, pair);
    }
    double newT = innerTick();
    t.set(newT);
    engineT.add(Duration.between(tickStartingInstant, Instant.now()).toNanos() / 1000000000d);
    EngineSnapshot snapshot = new EngineSnapshot(
        t.get(),
        List.copyOf(getBodies()),
        agentPairs.stream().map(Pair::first).toList(),
        List.copyOf(lastTickPerformedActions),
        engineT.get(),
        Duration.between(startingInstant, Instant.now()).toMillis() / 1000d,
        nOfTicks.get(),
        nOfActions.get(),
        nOfUnsupportedActions.get(),
        nOfIllegalActions.get()
    );
    lastTickPerformedActions.clear();
    return snapshot;
  }

  @Override
  public Map<String, Double> values() {
    return Map.ofEntries(
        Map.entry("engineT", engineT.get()),
        Map.entry("t", t.get()),
        Map.entry("wallT", Duration.between(startingInstant, Instant.now()).toMillis() / 1000d)
    );
  }

  @SuppressWarnings("unchecked")
  @Override
  public <A extends Action<O>, O> ActionOutcome<A, O> perform(A action, Agent agent) {
    nOfActions.incrementAndGet();
    ActionSolver<A, O> actionSolver = (ActionSolver<A, O>) actionSolvers.get(action.getClass());
    O o = null;
    if (actionSolver == null) {
      //try composite action
      if (action instanceof SelfDescribedAction<?> selfDescribedAction) {
        try {
          o = (O) selfDescribedAction.perform(this, agent);
        } catch (ActionException e) {
          L.finer(String.format("Ignoring illegal action %s due to %s", action.getClass().getSimpleName(), e));
          nOfIllegalActions.incrementAndGet();
        } catch (RuntimeException e) {
          L.warning(String.format("Ignoring action %s throwing exception: %s", action.getClass().getSimpleName(), e));
          nOfIllegalActions.incrementAndGet();
        }
      }
      //keep note as unsupported action
      L.finer(String.format("Ignoring unsupported action: %s", action.getClass().getSimpleName()));
      nOfUnsupportedActions.incrementAndGet();
    } else {
      try {
        o = actionSolver.solve(action, agent);
      } catch (ActionException e) {
        L.finer(String.format("Ignoring illegal action %s due to %s", action.getClass().getSimpleName(), e));
        nOfIllegalActions.incrementAndGet();
      } catch (RuntimeException e) {
        L.warning(String.format("Ignoring action %s throwing exception: %s", action.getClass().getSimpleName(), e));
        nOfIllegalActions.incrementAndGet();
      }
    }
    ActionOutcome<A,O> outcome = new ActionOutcome<>(agent, action, o == null ? Optional.empty() : Optional.of(o));
    lastTickPerformedActions.add(outcome);
    return outcome;
  }

  protected abstract double innerTick();

  @Override
  public double t() {
    return t.get();
  }

  protected abstract Collection<Body> getBodies();

  protected final <A extends Action<O>, O> void registerActionSolver(
      Class<A> actionClass,
      ActionSolver<A, O> actionSolver
  ) {
    actionSolvers.put(actionClass, actionSolver);
  }

  protected void registerActionSolvers() {
    registerActionSolver(AddAgent.class, this::addAgent);
    registerActionSolver(AttractAndLinkAnchor.class, this::attractAndLinkAnchor);
    registerActionSolver(AttractAndLinkClosestAnchorable.class, this::attractAndLinkClosestAnchorable);
  }

  protected Agent addAgent(AddAgent action, Agent agent) throws ActionException {
    if (action.agent() instanceof EmbodiedAgent embodiedAgent) {
      embodiedAgent.assemble(this);
      agentPairs.add(new Pair<>(action.agent(), List.of()));
    } else {
      agentPairs.add(new Pair<>(action.agent(), List.of()));
    }
    return action.agent();
  }

  protected AttractAndLinkAnchor.Outcome attractAndLinkAnchor(AttractAndLinkAnchor action, Agent agent) {
    double d = PolyUtils.minAnchorDistance(action.source(), action.destination()) * configuration.attractLinkRangeRatio;
    if (action.source().point().distance(action.destination().point()) < d) {
      return new AttractAndLinkAnchor.Outcome(
          Optional.empty(),
          perform(new CreateLink(action.source(), action.destination(), action.type()), agent).outcome()
      );
    } else {
      return new AttractAndLinkAnchor.Outcome(
          perform(new AttractAnchor(action.source(), action.destination(), action.magnitude()), agent).outcome(),
          Optional.empty()
      );
    }
  }

  protected Map<Pair<Anchor, Anchor>, AttractAndLinkAnchor.Outcome> attractAndLinkClosestAnchorable(
      AttractAndLinkClosestAnchorable action,
      Agent agent
  ) throws IllegalActionException {
    //find owner
    Anchorable src = action.anchors()
        .stream()
        .findAny()
        .map(Anchor::anchorable)
        .orElseThrow(() -> new IllegalActionException(action, "Empty source anchorable"));
    //find closest
    Optional<Pair<Anchorable, Double>> closest = bodies.stream()
        .filter(b -> b != src && b instanceof Anchorable)
        .map(b -> new Pair<>(
            (Anchorable) b,
            action.anchors().stream().mapToDouble(a -> PolyUtils.distance(a.point(), b.poly())).sum()
        ))
        .min(Comparator.comparingDouble(Pair::second));
    //attract and link
    if (closest.isPresent() && closest.get().second() < configuration.bodyFindRange) {
      return perform(new AttractAndLinkAnchorable(
          action.anchors(),
          closest.get().first(),
          action.magnitude(),
          action.type()
      ), agent).outcome().orElse(Map.of());
    }
    return Map.of();
  }

}
