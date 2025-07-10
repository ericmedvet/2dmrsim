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

package io.github.ericmedvet.mrsim2d.core.engine;

import io.github.ericmedvet.jnb.datastructure.Pair;
import io.github.ericmedvet.mrsim2d.core.Action;
import io.github.ericmedvet.mrsim2d.core.ActionOutcome;
import io.github.ericmedvet.mrsim2d.core.Agent;
import io.github.ericmedvet.mrsim2d.core.EmbodiedAgent;
import io.github.ericmedvet.mrsim2d.core.EnergyConsumingAction;
import io.github.ericmedvet.mrsim2d.core.NFCMessage;
import io.github.ericmedvet.mrsim2d.core.SelfDescribedAction;
import io.github.ericmedvet.mrsim2d.core.Snapshot;
import io.github.ericmedvet.mrsim2d.core.actions.AddAgent;
import io.github.ericmedvet.mrsim2d.core.actions.AttractAnchor;
import io.github.ericmedvet.mrsim2d.core.actions.AttractAndLinkAnchor;
import io.github.ericmedvet.mrsim2d.core.actions.AttractAndLinkAnchorable;
import io.github.ericmedvet.mrsim2d.core.actions.AttractAndLinkClosestAnchorable;
import io.github.ericmedvet.mrsim2d.core.actions.CreateLink;
import io.github.ericmedvet.mrsim2d.core.actions.EmitNFCMessage;
import io.github.ericmedvet.mrsim2d.core.actions.SenseNFC;
import io.github.ericmedvet.mrsim2d.core.actions.SenseSinusoidal;
import io.github.ericmedvet.mrsim2d.core.bodies.Anchor;
import io.github.ericmedvet.mrsim2d.core.bodies.Anchorable;
import io.github.ericmedvet.mrsim2d.core.bodies.Body;
import io.github.ericmedvet.mrsim2d.core.geometry.Point;
import io.github.ericmedvet.mrsim2d.core.util.AtomicDouble;
import io.github.ericmedvet.mrsim2d.core.util.HashSpatialMap;
import io.github.ericmedvet.mrsim2d.core.util.PolyUtils;
import io.github.ericmedvet.mrsim2d.core.util.Profiled;
import io.github.ericmedvet.mrsim2d.core.util.SpatialMap;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.IdentityHashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.UnaryOperator;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public abstract class AbstractEngine implements Engine, Profiled {

  private static final Configuration DEFAULT_CONFIGURATION = new Configuration(
      2,
      1.5,
      5,
      0.5,
      Math.PI / 2d,
      8
  );
  private static final Logger L = Logger.getLogger(AbstractEngine.class.getName());
  protected final AtomicDouble t;
  protected final List<Body> bodies;
  protected final Map<Agent, List<ActionOutcome<?, ?>>> agentActionOutcomes;
  private final Map<Agent, Map<EnergyConsumingAction.Type, Double>> agentEnergyConsumptions;
  private final List<Agent> agents;
  private final Configuration configuration;
  private final Map<Class<? extends Action<?>>, ActionSolver<?, ?>> actionSolvers;
  private final Instant startingInstant;
  private final EnumMap<EngineSnapshot.TimeType, AtomicDouble> times;
  private final EnumMap<EngineSnapshot.CounterType, AtomicInteger> counters;
  private final List<ActionOutcome<?, ?>> lastTickPerformedActions;
  private final Map<Agent, UnaryOperator<? extends Action<?>>> agentActionsFilters;
  private SpatialMap<NFCMessage> lastNFCMessages;
  private SpatialMap<NFCMessage> newNFCMessages;

  public AbstractEngine(Configuration configuration) {
    this.configuration = configuration;
    bodies = new ArrayList<>();
    agents = new ArrayList<>();
    agentActionOutcomes = new IdentityHashMap<>();
    agentEnergyConsumptions = new IdentityHashMap<>();
    actionSolvers = new LinkedHashMap<>();
    t = new AtomicDouble(0d);
    lastTickPerformedActions = new ArrayList<>();
    lastNFCMessages = new HashSpatialMap<>(configuration.nfcDistanceRange);
    times = new EnumMap<>(EngineSnapshot.TimeType.class);
    counters = new EnumMap<>(EngineSnapshot.CounterType.class);
    agentActionsFilters = new IdentityHashMap<>();
    Arrays.stream(EngineSnapshot.TimeType.values())
        .forEach(t -> times.put(t, new AtomicDouble(0d)));
    Arrays.stream(EngineSnapshot.CounterType.values())
        .forEach(t -> counters.put(t, new AtomicInteger(0)));
    startingInstant = Instant.now();
    registerActionSolvers();
  }

  public AbstractEngine() {
    this(DEFAULT_CONFIGURATION);
  }

  protected AttractAndLinkAnchor.Outcome attractAndLinkAnchor(
      AttractAndLinkAnchor action,
      Agent agent
  ) {
    double d = PolyUtils.minAnchorDistance(action.source(), action.destination()) * configuration.attractLinkRangeRatio;
    if (action.source().point().distance(action.destination().point()) < d) {
      return new AttractAndLinkAnchor.Outcome(
          Optional.empty(),
          perform(new CreateLink(action.source(), action.destination(), action.type()), agent)
              .outcome()
      );
    } else {
      return new AttractAndLinkAnchor.Outcome(
          perform(
              new AttractAnchor(action.source(), action.destination(), action.magnitude()),
              agent
          )
              .outcome(),
          Optional.empty()
      );
    }
  }

  protected NFCMessage emitNFCMessage(EmitNFCMessage action, Agent agent) throws ActionException {
    if (action.channel() < 0 || action.channel() >= configuration.nfcChannels) {
      throw new ActionException(
          "Invalid channel: %d not in [0,%d]".formatted(
              action.channel(),
              configuration.nfcChannels - 1
          )
      );
    }
    Point source = action.body().poly().center().sum(action.displacement());
    NFCMessage message = new NFCMessage(
        source,
        action.direction(),
        action.channel(),
        action.value()
    );
    newNFCMessages.add(source, message);
    return message;
  }

  protected abstract Collection<Body> getBodies();

  protected abstract double innerTick();

  protected Agent addAgent(AddAgent action, Agent agent) throws ActionException {
    if (action.agent() instanceof EmbodiedAgent embodiedAgent) {
      embodiedAgent.assemble(this);
    }
    agents.add(action.agent());
    return action.agent();
  }

  @SuppressWarnings("unchecked")
  @Override
  public <A extends Action<O>, O> ActionOutcome<A, O> perform(A action, Agent agent) {
    Instant performStartingInstant = Instant.now();
    counters.get(EngineSnapshot.CounterType.ACTION).incrementAndGet();
    if (agent != null) {
      UnaryOperator<Action<?>> filter = (UnaryOperator<Action<?>>) agentActionsFilters.get(agent);
      if (filter != null) {
        action = (A) filter.apply(action);
      }
    }
    ActionSolver<A, O> actionSolver = (ActionSolver<A, O>) actionSolvers.get(action.getClass());
    O o = null;
    if (actionSolver == null) {
      // try composite action
      if (action instanceof SelfDescribedAction<?> selfDescribedAction) {
        try {
          o = (O) selfDescribedAction.perform(this, agent);
        } catch (ActionException e) {
          L.finer(
              String.format(
                  "Ignoring illegal action %s due to %s",
                  action.getClass().getSimpleName(),
                  e
              )
          );
          counters.get(EngineSnapshot.CounterType.ILLEGAL_ACTION).incrementAndGet();
        } catch (RuntimeException e) {
          L.warning(
              String.format(
                  "Ignoring action %s throwing exception: %s",
                  action.getClass().getSimpleName(),
                  e
              )
          );
          counters.get(EngineSnapshot.CounterType.ILLEGAL_ACTION).incrementAndGet();
        }
      } else {
        // keep note as unsupported action
        L.finer(
            String.format(
                "Ignoring unsupported action: %s",
                action.getClass().getSimpleName()
            )
        );
        counters.get(EngineSnapshot.CounterType.UNSUPPORTED_ACTION).incrementAndGet();
      }
    } else {
      try {
        o = actionSolver.solve(action, agent);
      } catch (ActionException e) {
        L.finer(
            String.format(
                "Ignoring illegal action %s due to %s",
                action.getClass().getSimpleName(),
                e
            )
        );
        counters.get(EngineSnapshot.CounterType.ILLEGAL_ACTION).incrementAndGet();
      } catch (RuntimeException e) {
        L.warning(
            String.format(
                "Ignoring action %s throwing exception: %s",
                action.getClass().getSimpleName(),
                e
            )
        );
        counters.get(EngineSnapshot.CounterType.ILLEGAL_ACTION).incrementAndGet();
      }
    }
    ActionOutcome<A, O> outcome = new ActionOutcome<>(
        agent,
        action,
        o == null ? Optional.empty() : Optional.of(o)
    );
    lastTickPerformedActions.add(outcome);
    times.get(EngineSnapshot.TimeType.PERFORM)
        .add(Duration.between(performStartingInstant, Instant.now()).toNanos() / 1000000000d);
    return outcome;
  }

  protected Map<Pair<Anchor, Anchor>, AttractAndLinkAnchor.Outcome> attractAndLinkClosestAnchorable(
      AttractAndLinkClosestAnchorable action,
      Agent agent
  ) throws IllegalActionException {
    // find owner
    Anchorable src = action.anchors()
        .stream()
        .findAny()
        .map(Anchor::anchorable)
        .orElseThrow(() -> new IllegalActionException(action, "Empty source anchorable"));
    // find closest
    Optional<Pair<Anchorable, Double>> closest = bodies.stream()
        .filter(b -> b != src && b instanceof Anchorable)
        .map(
            b -> new Pair<>(
                (Anchorable) b,
                action.anchors()
                    .stream()
                    .mapToDouble(a -> PolyUtils.distance(a.point(), b.poly()))
                    .sum()
            )
        )
        .min(Comparator.comparingDouble(Pair::second));
    // attract and link
    if (closest.isPresent() && closest.get().second() < configuration.bodyFindRange) {
      return perform(
          new AttractAndLinkAnchorable(
              action.anchors(),
              closest.get().first(),
              action.magnitude(),
              action.type()
          ),
          agent
      )
          .outcome()
          .orElse(Map.of());
    }
    return Map.of();
  }

  protected Configuration configuration() {
    return configuration;
  }

  protected void registerActionSolvers() {
    registerActionSolver(AddAgent.class, this::addAgent);
    registerActionSolver(AttractAndLinkAnchor.class, this::attractAndLinkAnchor);
    registerActionSolver(
        AttractAndLinkClosestAnchorable.class,
        this::attractAndLinkClosestAnchorable
    );
    registerActionSolver(SenseSinusoidal.class, this::senseSinusoidal);
    registerActionSolver(EmitNFCMessage.class, this::emitNFCMessage);
    registerActionSolver(SenseNFC.class, this::senseNFC);
  }

  @Override
  public <A extends Action<O>, O> void registerActionsFilter(
      Agent agent,
      UnaryOperator<A> operator
  ) {
    agentActionsFilters.put(agent, operator);
  }

  protected final <A extends Action<O>, O> void registerActionSolver(
      Class<A> actionClass,
      ActionSolver<A, O> actionSolver
  ) {
    actionSolvers.put(actionClass, actionSolver);
  }

  protected Double senseNFC(SenseNFC action, Agent agent) {
    double sum = lastNFCMessages
        .get(
            action.body().poly().center().sum(action.displacement()),
            configuration.nfcDistanceRange
        )
        .stream()
        .filter(
            m -> m.channel() == action.channel() && Math.abs(
                m.direction() - action.direction()
            ) >= configuration.nfcAngleRange
        )
        .mapToDouble(NFCMessage::value)
        .sum();
    return action.range().clip(sum);
  }

  @Override
  public Snapshot snapshot() {
    return new EngineSnapshot(
        t.get(),
        getBodies(),
        agentEnergyConsumptions.entrySet()
            .stream()
            .collect(
                Collectors.toMap(
                    Entry::getKey,
                    e -> e.getValue()
                        .entrySet()
                        .stream()
                        .collect(
                            Collectors.toMap(
                                Entry::getKey,
                                Entry::getValue
                            )
                        )
                )
            ),
        lastTickPerformedActions,
        lastNFCMessages.all(),
        times.entrySet()
            .stream()
            .map(e -> Map.entry(e.getKey(), e.getValue().get()))
            .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue)),
        counters.entrySet()
            .stream()
            .map(e -> Map.entry(e.getKey(), e.getValue().get()))
            .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue))
    );
  }

  @Override
  public Snapshot tick() {
    Instant tickStartingInstant = Instant.now();
    lastTickPerformedActions.clear();
    newNFCMessages = new HashSpatialMap<>(configuration.nfcDistanceRange);
    counters.get(EngineSnapshot.CounterType.TICK).incrementAndGet();
    for (Agent agent : agents) {
      List<ActionOutcome<?, ?>> previousOutcomes = agentActionOutcomes.getOrDefault(
          agent,
          List.of()
      );
      List<? extends Action<?>> actions = agent.act(t.get(), previousOutcomes);
      //noinspection unchecked,rawtypes
      List<ActionOutcome<?, ?>> outcomes = (List) actions.stream()
          .map(action -> perform(action, agent))
          .toList();
      Map<EnergyConsumingAction.Type, Double> agentEnergies = new EnumMap<>(
          EnergyConsumingAction.Type.class
      );
      outcomes.forEach(outcome -> {
        //noinspection rawtypes
        if (outcome.action() instanceof EnergyConsumingAction ecAction) {
          //noinspection unchecked
          Map<EnergyConsumingAction.Type, Double> energies = ecAction.energy(
              outcome.outcome()
                  .orElseThrow(
                      () -> new RuntimeException(
                          "Energy consuming action wrongly returns an empty outcome"
                      )
                  )
          );
          energies.forEach(
              (type, value) -> agentEnergies.compute(
                  type,
                  (t, oldV) -> oldV == null ? value : (oldV + value)
              )
          );
        }
      });
      agentActionOutcomes.put(agent, outcomes);
      agentEnergyConsumptions.put(agent, agentEnergies);
    }
    lastNFCMessages = newNFCMessages;
    Instant innerTickStartingInstant = Instant.now();
    double oldT = t.get();
    double newT = innerTick();
    double deltaT = newT - oldT;
    t.set(newT);
    times.get(EngineSnapshot.TimeType.INNER_TICK)
        .add(Duration.between(innerTickStartingInstant, Instant.now()).toNanos() / 1000000000d);
    // update energies
    agentEnergyConsumptions
        .forEach(
            (agent, energies) -> energies
                .forEach((t, e) -> energies.put(t, e * deltaT))
        );
    // save profile times
    times.get(EngineSnapshot.TimeType.TICK)
        .add(Duration.between(tickStartingInstant, Instant.now()).toNanos() / 1000000000d);
    times.get(EngineSnapshot.TimeType.WALL)
        .set(Duration.between(startingInstant, Instant.now()).toMillis() / 1000d);
    times.get(EngineSnapshot.TimeType.ENVIRONMENT).set(t.get());
    return snapshot();
  }

  @Override
  public void removeActionsFilter(Agent agent) {
    agentActionsFilters.remove(agent);
  }

  @FunctionalInterface
  protected interface ActionSolver<A extends Action<O>, O> {

    O solve(A action, Agent agent) throws ActionException;
  }

  protected double senseSinusoidal(SenseSinusoidal action, Agent agent) {
    return Math.sin(2d * Math.PI * action.f() * t() + action.phi());
  }

  @Override
  public double t() {
    return t.get();
  }

  public record Configuration(
      double attractionRange,
      double attractLinkRangeRatio,
      double bodyFindRange,
      double nfcDistanceRange,
      double nfcAngleRange,
      int nfcChannels
  ) {

  }

  @Override
  public Map<String, Number> values() {
    return Stream.of(
        times.entrySet()
            .stream()
            .map(
                e -> Map.entry(
                    "time_" + e.getKey(),
                    e.getValue().get()
                )
            ),
        counters.entrySet()
            .stream()
            .map(
                e -> Map.entry(
                    "counter_" + e.getKey(),
                    e.getValue().get()
                )
            )
    )
        .flatMap(m -> m)
        .map(e -> Map.entry(e.getKey().toLowerCase(), e.getValue()))
        .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
  }
}
