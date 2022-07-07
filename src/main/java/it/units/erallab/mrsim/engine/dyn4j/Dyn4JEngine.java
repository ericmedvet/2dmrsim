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

package it.units.erallab.mrsim.engine.dyn4j;

import it.units.erallab.mrsim.core.Agent;
import it.units.erallab.mrsim.core.actions.CreateRigidBodyAt;
import it.units.erallab.mrsim.core.actions.CreateUnmovableBodyAt;
import it.units.erallab.mrsim.core.bodies.Body;
import it.units.erallab.mrsim.engine.AbstractEngine;
import it.units.erallab.mrsim.engine.IllegalActionException;
import org.dyn4j.dynamics.Settings;
import org.dyn4j.geometry.Vector2;
import org.dyn4j.world.World;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * @author "Eric Medvet" on 2022/07/07 for 2dmrsim
 */
public class Dyn4JEngine extends AbstractEngine {

  private final static double RIGID_FRICTION = 1d;
  private final static double RIGID_RESTITUTION = 0.5d;

  private final Settings settings;
  private final List<Body> bodies;
  private final World<org.dyn4j.dynamics.Body> world;

  public Dyn4JEngine(Settings settings) {
    this.settings = settings;
    bodies = new ArrayList<>();
    world = new World<>();
    world.setSettings(settings);
  }

  public Dyn4JEngine() {
    this(new Settings());
  }

  @Override
  protected double innerTick() {
    world.step(1);
    return t() + settings.getStepFrequency();
  }

  @Override
  protected Collection<Body> getBodies() {
    return bodies;
  }

  @Override
  protected void registerActionSolvers() {
    registerActionSolver(CreateRigidBodyAt.class, this::createRigidBodyAt);
    registerActionSolver(CreateUnmovableBodyAt.class, this::createUnmovableBodyAt);
    super.registerActionSolvers();
  }

  private RigidBody createRigidBodyAt(CreateRigidBodyAt action, Agent agent) throws IllegalActionException {
    if (action.scale() != 1) {
      throw new IllegalActionException(action, "Scale is not supported");
    }
    RigidBody rigidBody = new RigidBody(action.poly(), action.mass(), RIGID_FRICTION, RIGID_RESTITUTION);
    System.out.println(rigidBody.poly());
    rigidBody.getBody().rotate(action.rotation());
    rigidBody.getBody().translate(new Vector2(action.translation().x(), action.translation().y()));
    System.out.println(rigidBody.poly());
    world.addBody(rigidBody.getBody());
    bodies.add(rigidBody);
    return rigidBody;
  }

  private UnmovableBody createUnmovableBodyAt(CreateUnmovableBodyAt action, Agent agent) throws IllegalActionException {
    if (action.scale() != 1) {
      throw new IllegalActionException(action, "Scale is not supported");
    }
    UnmovableBody unmovableBody = new UnmovableBody(action.poly(), RIGID_FRICTION, RIGID_RESTITUTION);
    unmovableBody.getBody().rotate(action.rotation());
    unmovableBody.getBody().translate(new Vector2(action.translation().x(), action.translation().y()));
    world.addBody(unmovableBody.getBody());
    bodies.add(unmovableBody);
    return unmovableBody;
  }

}
