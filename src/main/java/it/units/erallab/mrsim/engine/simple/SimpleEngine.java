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

package it.units.erallab.mrsim.engine.simple;

import it.units.erallab.mrsim.core.Agent;
import it.units.erallab.mrsim.core.actions.CreateRigidBody;
import it.units.erallab.mrsim.core.bodies.Body;
import it.units.erallab.mrsim.core.bodies.RigidBody;
import it.units.erallab.mrsim.core.geometry.Point;
import it.units.erallab.mrsim.core.geometry.Poly;
import it.units.erallab.mrsim.engine.AbstractEngine;
import it.units.erallab.mrsim.engine.IllegalActionException;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * @author "Eric Medvet" on 2022/07/06 for 2dmrsim
 */
public class SimpleEngine extends AbstractEngine {

  private final static double DELTA_T = 0.1;

  @Override
  protected double innerTick() {
    return t.get() + DELTA_T;
  }

  @Override
  protected Collection<Body> getBodies() {
    return bodies;
  }

  @Override
  protected void registerActionSolvers() {
    super.registerActionSolvers();
    registerActionSolver(CreateRigidBody.class, this::createRigidBody);
  }

  private RigidBody createRigidBody(CreateRigidBody action, Agent agent) {
    RigidBody rigidBody = new RigidBody() {
      @Override
      public Poly poly() {
        return action.poly();
      }

      @Override
      public double mass() {
        return action.mass();
      }

      @Override
      public Point centerLinearVelocity() {
        return new Point(0, 0);
      }

      @Override
      public double angle() {
        return 0;
      }
    };
    bodies.add(rigidBody);
    return rigidBody;
  }

}
