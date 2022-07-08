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

import it.units.erallab.mrsim.core.Action;
import it.units.erallab.mrsim.core.ActionOutcome;
import it.units.erallab.mrsim.core.Agent;
import it.units.erallab.mrsim.core.Snapshot;
import it.units.erallab.mrsim.core.actions.*;
import it.units.erallab.mrsim.core.bodies.Body;
import it.units.erallab.mrsim.core.bodies.RigidBody;
import it.units.erallab.mrsim.core.bodies.Voxel;
import it.units.erallab.mrsim.util.AtomicDouble;
import it.units.erallab.mrsim.util.Pair;

import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Logger;

/**
 * @author "Eric Medvet" on 2022/07/06 for 2dmrsim
 */
public abstract class AbstractEngine implements Engine {

  @FunctionalInterface
  public interface ActionSolver<A extends Action<O>, O> {
    O solve(A action, Agent agent) throws ActionException;
  }

  protected final AtomicDouble t;
  protected final List<Body> bodies;
  protected final List<Pair<Agent, List<ActionOutcome<?>>>> agentPairs;
  private final Map<Class<? extends Action<?>>, ActionSolver<?, ?>> actionSolvers;
  private final static Logger L = Logger.getLogger(AbstractEngine.class.getName());

  private final AtomicInteger nOfTicks;
  private final AtomicDouble engineT;
  private final Instant startingInstant;
  private final AtomicInteger nOfActions;
  private final AtomicInteger nOfUnsupportedActions;
  private final AtomicInteger nOfIllegalActions;


  public AbstractEngine() {
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
    registerActionSolvers();
  }

  @SuppressWarnings({"unchecked", "rawtypes"})
  @Override
  public Snapshot tick() {
    Instant tickStartingInstant = Instant.now();
    nOfTicks.incrementAndGet();
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
    double newT = innerTick();
    t.set(newT);
    engineT.add(Duration.between(tickStartingInstant, Instant.now()).toNanos() / 1000000000d);
    return new EngineSnapshot(
        t.get(),
        agentPairs,
        getBodies(),
        engineT.get(),
        Duration.between(startingInstant, Instant.now()).toMillis() / 1000d,
        nOfTicks.get(),
        nOfActions.get(),
        nOfUnsupportedActions.get(),
        nOfIllegalActions.get()
    );
  }

  @SuppressWarnings("unchecked")
  @Override
  public <A extends Action<O>, O> Optional<O> perform(A action, Agent agent) {
    nOfActions.incrementAndGet();
    ActionSolver<A, O> actionSolver = (ActionSolver<A, O>) actionSolvers.get(action.getClass());
    if (actionSolver == null) {
      L.finer(String.format("Ignoring unsupported action: %s", action.getClass().getSimpleName()));
      nOfUnsupportedActions.incrementAndGet();
      return Optional.empty();
    }
    try {
      O outcome = actionSolver.solve(action, agent);
      return outcome == null ? Optional.empty() : Optional.of(outcome);
    } catch (ActionException e) {
      L.finer(String.format("Ignoring illegal action: %s", e));
      nOfIllegalActions.incrementAndGet();
      return Optional.empty();
    } catch (RuntimeException e) {
      L.warning(String.format("Ignoring action throwing exception: %s", e));
      nOfIllegalActions.incrementAndGet();
      return Optional.empty();
    }
  }

  protected abstract double innerTick();

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
    registerActionSolver(CreateAndTranslateVoxel.class, this::createAndTranslateVoxel);
  }

  protected Void addAgent(AddAgent action, Agent agent) {
    agentPairs.add(new Pair<>(action.agent(), List.of()));
    return null;
  }

  protected RigidBody createAndTranslateRigidBody(
      CreateAndTranslateRigidBody action,
      Agent agent
  ) throws ActionException {
    RigidBody rigidBody = perform(
        new CreateRigidBody(action.poly(), action.mass()),
        agent
    ).orElseThrow(() -> new ActionException(action, "Undoable creation"));
    perform(new TranslateBody(rigidBody, action.translation()), agent);
    return rigidBody;
  }

  protected Voxel createAndTranslateVoxel(
      CreateAndTranslateVoxel action,
      Agent agent
  ) throws ActionException {
    Voxel voxel = perform(
        new CreateVoxel(action.sideLength(), action.mass(), action.softness(), action.areaRatioActiveRange()),
        agent
    ).orElseThrow(() -> new ActionException(action, "Undoable creation"));
    perform(new TranslateBody(voxel, action.translation()), agent);
    return voxel;
  }

  @Override
  public double t() {
    return t.get();
  }
}
