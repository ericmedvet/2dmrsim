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
import it.units.erallab.mrsim.core.actions.*;
import it.units.erallab.mrsim.core.bodies.Anchor;
import it.units.erallab.mrsim.core.bodies.Anchorable;
import it.units.erallab.mrsim.core.bodies.Body;
import it.units.erallab.mrsim.core.bodies.Voxel;
import it.units.erallab.mrsim.core.geometry.Point;
import it.units.erallab.mrsim.engine.AbstractEngine;
import it.units.erallab.mrsim.engine.IllegalActionException;
import org.dyn4j.dynamics.Settings;
import org.dyn4j.dynamics.joint.DistanceJoint;
import org.dyn4j.dynamics.joint.Joint;
import org.dyn4j.dynamics.joint.WeldJoint;
import org.dyn4j.geometry.Vector2;
import org.dyn4j.world.World;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.EnumSet;

/**
 * @author "Eric Medvet" on 2022/07/07 for 2dmrsim
 */
public class Dyn4JEngine extends AbstractEngine {

  private final static Configuration DEFAULT_CONFIGURATION = new Configuration(
      new Settings(),
      1, 0.5, 0.1, 0.1,
      1, 0.5,
      1, 0.5, 0.1, 0.1, 0.35, EnumSet.allOf(it.units.erallab.mrsim.engine.dyn4j.Voxel.SpringScaffolding.class),
      8d, 0.3d, 0.5d
  );

  public record Configuration(
      Settings innerSettings,
      double rigidBodyFriction,
      double rigidBodyRestitution,
      double rigidBodyLinearDamping,
      double rigidBodyAngularDamping,
      double unmovableBodyFriction,
      double unmovableBodyRestitution,
      double voxelFriction,
      double voxelRestitution,
      double voxelLinearDamping,
      double voxelAngularDamping,
      double voxelVertexMassSideLengthRatio,
      EnumSet<it.units.erallab.mrsim.engine.dyn4j.Voxel.SpringScaffolding> voxelSpringScaffoldings,
      double softLinkSpringF,
      double softLinkSpringD,
      double softLinkRestDistanceRatio

  ) {}

  private final Configuration configuration;
  private final World<org.dyn4j.dynamics.Body> world;

  public Dyn4JEngine(Configuration configuration) {
    this.configuration = configuration;
    world = new World<>();
    world.setSettings(configuration.innerSettings());
  }

  public Dyn4JEngine() {
    this(DEFAULT_CONFIGURATION);
  }

  @Override
  protected double innerTick() {
    world.step(1);
    return t() + configuration.innerSettings().getStepFrequency();
  }

  @Override
  protected Collection<Body> getBodies() {
    return bodies;
  }

  @Override
  protected void registerActionSolvers() {
    registerActionSolver(CreateRigidBody.class, this::createRigidBody);
    registerActionSolver(CreateUnmovableBody.class, this::createUnmovableBody);
    registerActionSolver(TranslateBody.class, this::translateBody);
    registerActionSolver(CreateVoxel.class, this::createVoxel);
    registerActionSolver(AttachAnchor.class, this::attachAnchor);
    registerActionSolver(DetachAnchor.class, this::detachAnchor);
    registerActionSolver(RemoveBody.class, this::removeBody);
    registerActionSolver(ActuateVoxel.class, this::actuateVoxel);
    super.registerActionSolvers();
  }

  private RigidBody createRigidBody(CreateRigidBody action, Agent agent) {
    RigidBody rigidBody = new RigidBody(
        action.poly(),
        action.mass(),
        configuration.rigidBodyFriction,
        configuration.rigidBodyRestitution,
        configuration.rigidBodyLinearDamping,
        configuration.rigidBodyAngularDamping
    );
    rigidBody.getBodies().forEach(world::addBody);
    bodies.add(rigidBody);
    return rigidBody;
  }

  private UnmovableBody createUnmovableBody(CreateUnmovableBody action, Agent agent) {
    UnmovableBody unmovableBody = new UnmovableBody(
        action.poly(),
        configuration.unmovableBodyFriction,
        configuration.unmovableBodyRestitution
    );
    unmovableBody.getBodies().forEach(world::addBody);
    bodies.add(unmovableBody);
    return unmovableBody;
  }

  private Body translateBody(TranslateBody action, Agent agent) throws IllegalActionException {
    Point t = new Point(
        action.translation().x(),
        action.translation().y()
    );
    if (action.body() instanceof MultipartBody multipartBody) {
      multipartBody.getBodies().forEach(b -> b.translate(t.x(), t.y()));
      return action.body();
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
        configuration.voxelFriction,
        configuration.voxelRestitution,
        action.material().softness(),
        configuration.voxelLinearDamping,
        configuration.voxelAngularDamping,
        configuration.voxelVertexMassSideLengthRatio,
        action.material().areaRatioRange(),
        configuration.voxelSpringScaffoldings
    );
    voxel.getBodies().forEach(world::addBody);
    voxel.getJoints().forEach(world::addJoint);
    bodies.add(voxel);
    return voxel;
  }

  private Anchor.Link attachAnchor(AttachAnchor action, Agent agent) throws IllegalActionException {
    if (action.anchor().links().stream().anyMatch(l -> l.destination().anchorable().equals(action.anchorable()))) {
      //this anchor is already attached to dst anchorable: ignore
      return null;
    }
    if (action.anchor() instanceof BodyAnchor src) {
      BodyAnchor dst = action.anchorable().anchors().stream()
          .filter(a -> a instanceof BodyAnchor)
          .map(a -> (BodyAnchor) a)
          .min(Comparator.comparingDouble(a -> a.point().distance(src.point())))
          .orElse(null);
      if (dst != null) {
        Joint<org.dyn4j.dynamics.Body> joint;
        if (Anchor.Link.Type.RIGID.equals(action.type())) {
          joint = new WeldJoint<>(
              src.getBody(),
              dst.getBody(),
              new Vector2(
                  src.point().x(),
                  src.point().y()
              )
          );
        } else if (Anchor.Link.Type.SOFT.equals(action.type())) {
          DistanceJoint<org.dyn4j.dynamics.Body> springJoint = new DistanceJoint<>(
              src.getBody(),
              dst.getBody(),
              new Vector2(src.point().x(), src.point().y()),
              new Vector2(dst.point().x(), dst.point().y())
          );
          springJoint.setRestDistance(src.point().distance(dst.point()) * configuration.softLinkRestDistanceRatio);
          springJoint.setCollisionAllowed(true);
          springJoint.setFrequency(configuration.softLinkSpringF);
          springJoint.setDampingRatio(configuration.softLinkSpringD);
          joint = springJoint;
        } else {
          throw new IllegalActionException(action, String.format("Unsupported link type: %s", action.type()));
        }
        world.addJoint(joint);
        Anchor.Link link = new Anchor.Link(src, dst, action.type());
        src.getJointMap().put(link, joint);
        dst.getJointMap().put(link.reversed(), joint);
      }
      return new Anchor.Link(src, dst, action.type());
    }
    return null;
  }

  private Collection<Anchor.Link> detachAnchor(DetachAnchor action, Agent agent) {
    Collection<Anchor.Link> removedLinks = new ArrayList<>();
    if (action.anchor() instanceof BodyAnchor srcAnchor) {

      for (Anchor.Link link : srcAnchor.links()) {
        if (link.destination() instanceof BodyAnchor dstAnchor) {
          if (dstAnchor.anchorable() == action.anchorable()) {
            removedLinks.add(link);
            //remove joint from world
            world.removeJoint(srcAnchor.getJointMap().get(link));
            //remove link from maps
            srcAnchor.getJointMap().remove(link);
            dstAnchor.getJointMap().remove(link.reversed());
          }
        }
      }
    }
    return removedLinks;
  }

  private Body removeBody(RemoveBody action, Agent agent) throws IllegalActionException {
    //detach
    if (action.body() instanceof Anchorable anchorable) {
      perform(new DetachAllAnchors(anchorable), agent);
    }
    //remove
    if (action.body() instanceof MultipartBody multipartBody) {
      multipartBody.getJoints().forEach(world::removeJoint);
      multipartBody.getBodies().forEach(world::removeBody);
      bodies.remove(action.body());
      return action.body();
    }
    throw new IllegalActionException(
        action,
        String.format("Unsupported body type %s", action.body().getClass().getSimpleName())
    );
  }

  private Voxel actuateVoxel(ActuateVoxel action, Agent agent) throws IllegalActionException {
    if (action.voxel() instanceof it.units.erallab.mrsim.engine.dyn4j.Voxel voxel) {
      voxel.actuate(action.values());
      return voxel;
    }
    throw new IllegalActionException(
        action,
        String.format("Unsupported voxel type %s", action.voxel().getClass().getSimpleName())
    );
  }

}
