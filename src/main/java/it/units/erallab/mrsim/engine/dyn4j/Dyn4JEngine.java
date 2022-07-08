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
import it.units.erallab.mrsim.core.actions.CreateRigidBody;
import it.units.erallab.mrsim.core.actions.CreateUnmovableBody;
import it.units.erallab.mrsim.core.actions.CreateVoxel;
import it.units.erallab.mrsim.core.actions.TranslateBody;
import it.units.erallab.mrsim.core.bodies.Body;
import it.units.erallab.mrsim.core.bodies.Voxel;
import it.units.erallab.mrsim.core.geometry.Point;
import it.units.erallab.mrsim.engine.AbstractEngine;
import it.units.erallab.mrsim.engine.IllegalActionException;
import org.dyn4j.dynamics.Settings;
import org.dyn4j.world.World;

import java.util.Arrays;
import java.util.Collection;

/**
 * @author "Eric Medvet" on 2022/07/07 for 2dmrsim
 */
public class Dyn4JEngine extends AbstractEngine {
  private final Settings settings;
  private final World<org.dyn4j.dynamics.Body> world;

  public Dyn4JEngine(Settings settings) {
    this.settings = settings;
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
    registerActionSolver(CreateRigidBody.class, this::createRigidBody);
    registerActionSolver(CreateUnmovableBody.class, this::createUnmovableBody);
    registerActionSolver(TranslateBody.class, this::translate);
    registerActionSolver(CreateVoxel.class, this::createVoxel);
    super.registerActionSolvers();
  }

  private RigidBody createRigidBody(CreateRigidBody action, Agent agent) {
    RigidBody rigidBody = new RigidBody(
        action.poly(),
        action.mass(),
        RigidBody.FRICTION,
        RigidBody.RESTITUTION,
        RigidBody.LINEAR_DAMPING,
        RigidBody.ANGULAR_DAMPING
    );
    world.addBody(rigidBody.getBody());
    bodies.add(rigidBody);
    return rigidBody;
  }

  private UnmovableBody createUnmovableBody(CreateUnmovableBody action, Agent agent) {
    UnmovableBody unmovableBody = new UnmovableBody(action.poly(), UnmovableBody.FRICTION, UnmovableBody.RESTITUTION);
    world.addBody(unmovableBody.getBody());
    bodies.add(unmovableBody);
    return unmovableBody;
  }

  private Body translate(TranslateBody action, Agent agent) throws IllegalActionException {
    Point t = new Point(
        action.translation().x() - action.body().poly().boundingBox().min().x(),
        action.translation().y() - action.body().poly().boundingBox().min().y()
    );
    if (action.body() instanceof RigidBody rigidBody) {
      rigidBody.getBody().translate(t.x(), t.y());
      return rigidBody;
    }
    if (action.body() instanceof it.units.erallab.mrsim.engine.dyn4j.Voxel voxel) {
      voxel.translate(t);
      return voxel;
    }
    throw new IllegalActionException(
        action,
        String.format("Untranslatable body type: %s", action.body().getClass().getName())
    );
  }

  private Voxel createVoxel(CreateVoxel action, Agent agent) {
    it.units.erallab.mrsim.engine.dyn4j.Voxel voxel = new it.units.erallab.mrsim.engine.dyn4j.Voxel(
        action.sideLength(),
        action.mass(),
        it.units.erallab.mrsim.engine.dyn4j.Voxel.FRICTION,
        it.units.erallab.mrsim.engine.dyn4j.Voxel.RESTITUTION,
        action.softness(),
        it.units.erallab.mrsim.engine.dyn4j.Voxel.LINEAR_DAMPING,
        it.units.erallab.mrsim.engine.dyn4j.Voxel.ANGULAR_DAMPING,
        it.units.erallab.mrsim.engine.dyn4j.Voxel.VERTEX_MASS_SIDE_LENGTH_RATIO,
        action.areaRatioActiveRange(),
        it.units.erallab.mrsim.engine.dyn4j.Voxel.SPRING_SCAFFOLDINGS
    );
    Arrays.stream(voxel.getVertexBodies()).sequential().forEach(world::addBody);
    voxel.getSpringJoints().forEach(world::addJoint);
    bodies.add(voxel);
    return voxel;
  }

}
