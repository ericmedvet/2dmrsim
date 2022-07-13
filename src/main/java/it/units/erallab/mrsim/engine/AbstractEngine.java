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
import it.units.erallab.mrsim.core.bodies.*;
import it.units.erallab.mrsim.core.geometry.Point;
import it.units.erallab.mrsim.util.AtomicDouble;
import it.units.erallab.mrsim.util.Pair;
import it.units.erallab.mrsim.util.PolyUtils;
import it.units.erallab.mrsim.util.Profiled;

import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Logger;
import java.util.stream.Collectors;

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
    ActionOutcome<A, O> outcome;
    if (actionSolver == null) {
      L.finer(String.format("Ignoring unsupported action: %s", action.getClass().getSimpleName()));
      nOfUnsupportedActions.incrementAndGet();
      outcome = new ActionOutcome<>(agent, action, Optional.empty());
    } else {
      try {
        O o = actionSolver.solve(action, agent);
        outcome = new ActionOutcome<>(agent, action, o == null ? Optional.empty() : Optional.of(o));
      } catch (ActionException e) {
        L.finer(String.format("Ignoring illegal action %s due to %s", action.getClass().getSimpleName(), e));
        nOfIllegalActions.incrementAndGet();
        outcome = new ActionOutcome<>(agent, action, Optional.empty());
      } catch (RuntimeException e) {
        L.warning(String.format("Ignoring action %s throwing exception: %s", action.getClass().getSimpleName(), e));
        nOfIllegalActions.incrementAndGet();
        outcome = new ActionOutcome<>(agent, action, Optional.empty());
      }
    }
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
    registerActionSolver(CreateAndTranslateRigidBody.class, this::createAndTranslateRigidBody);
    registerActionSolver(CreateAndTranslateUnmovableBody.class, this::createAndTranslateUnmovableBody);
    registerActionSolver(CreateAndTranslateVoxel.class, this::createAndTranslateVoxel);
    registerActionSolver(TranslateAgent.class, this::translateAgent);
    registerActionSolver(AddAndTranslateAgent.class, this::addAndTranslateAgent);
    registerActionSolver(AttachClosestAnchors.class, this::attachClosestAnchors);
    registerActionSolver(AttachAnchor.class, this::attachAnchor);
    registerActionSolver(DetachAnchors.class, this::detachAnchors);
    registerActionSolver(DetachAnchorsFromAnchorable.class, this::detachAnchorsFromAnchorable);
    registerActionSolver(DetachAllAnchorsFromAnchorable.class, this::detachAllAnchorsFromAnchorable);
    registerActionSolver(DetachAnchorFromAnchorable.class, this::detachAnchorFromAnchorable);
    registerActionSolver(AttractAnchorable.class, this::attractAnchorable);
    registerActionSolver(AttractAndLinkAnchor.class, this::attractAndLinkAnchor);
    registerActionSolver(AttractAndLinkAnchorable.class, this::attractAndLinkAnchorable);
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

  protected RigidBody createAndTranslateRigidBody(
      CreateAndTranslateRigidBody action,
      Agent agent
  ) throws ActionException {
    RigidBody rigidBody = perform(
        new CreateRigidBody(action.poly(), action.mass()),
        agent
    ).outcome().orElseThrow(() -> new ActionException(action, "Undoable creation"));
    perform(new TranslateBody(rigidBody, action.translation()), agent);
    return rigidBody;
  }

  protected UnmovableBody createAndTranslateUnmovableBody(
      CreateAndTranslateUnmovableBody action,
      Agent agent
  ) throws ActionException {
    UnmovableBody unmovableBody = perform(
        new CreateUnmovableBody(action.poly()),
        agent
    ).outcome().orElseThrow(() -> new ActionException(action, "Undoable creation"));
    perform(new TranslateBody(unmovableBody, action.translation()), agent);
    return unmovableBody;
  }

  protected Voxel createAndTranslateVoxel(
      CreateAndTranslateVoxel action,
      Agent agent
  ) throws ActionException {
    Voxel voxel = perform(
        new CreateVoxel(action.sideLength(), action.mass(), action.material()),
        agent
    ).outcome().orElseThrow(() -> new ActionException(action, "Undoable creation"));
    perform(new TranslateBody(voxel, action.translation()), agent);
    return voxel;
  }

  private Anchor.Link attachAnchor(AttachAnchor action, Agent agent) {
    // find already attached anchors
    Collection<Anchor> attachedAnchors = action.anchor().links().stream()
        .map(Anchor.Link::destination)
        .filter(a -> a.anchorable() == action.anchorable())
        .collect(Collectors.toSet());
    //find closest anchor on destination
    Anchor destination = action.anchorable().anchors().stream()
        .filter(a -> !attachedAnchors.contains(a))
        .min(Comparator.comparingDouble(a -> a.point().distance(action.anchor().point())))
        .orElse(null);
    //create link
    if (destination != null) {
      return perform(new CreateLink(action.anchor(), destination, action.type()), agent).outcome().orElse(null);
    }
    return null;
  }

  private Anchor.Link detachAnchorFromAnchorable(DetachAnchorFromAnchorable action, Agent agent) {
    //find anchor
    Optional<Anchor.Link> optionalLink = action.anchor()
        .links()
        .stream()
        .filter(l -> l.destination().anchorable() == action.anchorable())
        .findFirst();
    return optionalLink
        .flatMap(l -> perform(new RemoveLink(optionalLink.get()), agent).outcome())
        .orElse(null);
  }

  protected Collection<Anchor.Link> attachClosestAnchors(AttachClosestAnchors action, Agent agent) {
    Point targetCenter = Point.average(action.targetAnchorable()
        .anchors()
        .stream()
        .map(Anchor::point)
        .toArray(Point[]::new));
    return action.sourceAnchorable().anchors().stream()
        .sorted(Comparator.comparingDouble(a -> a.point().distance(targetCenter)))
        .limit(action.nOfAnchors())
        .map(a -> perform(new AttachAnchor(a, action.targetAnchorable(), action.type()), agent).outcome().orElseThrow())
        .toList();
  }

  protected Collection<Anchor.Link> detachAnchorsFromAnchorable(
      DetachAnchorsFromAnchorable action,
      Agent agent
  ) {
    return action.sourceAnchorable().anchors().stream()
        .map(a -> perform(new DetachAnchorFromAnchorable(a, action.targetAnchorable()), agent).outcome())
        .filter(Optional::isPresent)
        .map(Optional::get)
        .toList();
  }

  protected Collection<Anchor.Link> detachAllAnchorsFromAnchorable(DetachAllAnchorsFromAnchorable action, Agent agent) {
    Set<Anchorable> anchorables = action.anchorable().anchors().stream()
        .map(a -> a.links().stream().map(l -> l.destination().anchorable()).collect(Collectors.toSet()))
        .flatMap(Collection::stream)
        .collect(Collectors.toSet());
    return anchorables.stream()
        .map(target -> perform(new DetachAnchorsFromAnchorable(action.anchorable(), target), agent).outcome()
            .orElseThrow())
        .flatMap(Collection::stream)
        .collect(Collectors.toSet());
  }

  protected EmbodiedAgent addAndTranslateAgent(AddAndTranslateAgent action, Agent agent) throws ActionException {
    EmbodiedAgent embodiedAgent = (EmbodiedAgent) perform(
        new AddAgent(action.agent()), agent)
        .outcome().orElseThrow(() -> new ActionException(action, "Undoable addition")
        );
    perform(new TranslateAgent(embodiedAgent, action.translation()), agent);
    return embodiedAgent;
  }

  protected EmbodiedAgent translateAgent(TranslateAgent action, Agent agent) {
    action.agent().bodyParts().forEach(b -> perform(new TranslateBody(b, action.translation()), agent));
    return action.agent();
  }

  protected Collection<Pair<Anchor, Anchor>> attractAnchorable(AttractAnchorable action, Agent agent) {
    //discard already attached
    Collection<Anchor> srcAnchors = action.anchors().stream()
        .filter(a -> a.links().stream()
            .map(l -> l.destination().anchorable())
            .filter(dst -> dst == action.anchorable()).toList().isEmpty())
        .toList();
    //match anchor pairs
    Collection<Anchor> dstAnchors = new HashSet<>(action.anchorable().anchors());
    Collection<Pair<Anchor, Anchor>> pairs = new ArrayList<>();
    srcAnchors.forEach(src -> {
      Optional<Anchor> closest = dstAnchors.stream()
          .min(Comparator.comparingDouble(a -> a.point().distance(src.point())));
      if (closest.isPresent()) {
        pairs.add(new Pair<>(src, closest.get()));
        dstAnchors.remove(closest.get());
      }
    });
    //attract
    pairs.forEach(p -> perform(new AttractAnchor(p.first(), p.second(), action.magnitude())));
    return pairs;
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

  protected Map<Pair<Anchor, Anchor>, AttractAndLinkAnchor.Outcome> attractAndLinkAnchorable(
      AttractAndLinkAnchorable action,
      Agent agent
  ) {
    //discard already attached
    Collection<Anchor> srcAnchors = action.anchors().stream()
        .filter(a -> a.links().stream()
            .map(l -> l.destination().anchorable())
            .filter(dst -> dst == action.anchorable()).toList().isEmpty())
        .toList();
    //match anchor pairs
    Collection<Anchor> dstAnchors = new HashSet<>(action.anchorable().anchors());
    Collection<Pair<Anchor, Anchor>> pairs = new ArrayList<>();
    srcAnchors.forEach(src -> dstAnchors.stream()
        .min(Comparator.comparingDouble(a -> a.point().distance(src.point())))
        .ifPresent(dstAnchor -> {
          pairs.add(new Pair<>(src, dstAnchor));
          dstAnchors.remove(dstAnchor);
        }));
    //attract and link
    Map<Pair<Anchor, Anchor>, AttractAndLinkAnchor.Outcome> map = new HashMap<>();
    for (Pair<Anchor, Anchor> pair : pairs) {
      perform(new AttractAndLinkAnchor(
          pair.first(),
          pair.second(),
          action.magnitude(),
          action.type()
      ), agent)
          .outcome()
          .ifPresent(outcome -> map.put(pair, outcome));
    }
    return map;
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

  protected Collection<Anchor.Link> detachAnchors(DetachAnchors action, Agent agent) {
    Collection<Anchor.Link> toRemoveLinks = action.anchors().stream()
        .map(Anchor::links)
        .flatMap(Collection::stream)
        .toList();
    return toRemoveLinks.stream()
        .map(l -> perform(new RemoveLink(l), agent).outcome())
        .filter(Optional::isPresent)
        .map(Optional::get)
        .toList();
  }

}
