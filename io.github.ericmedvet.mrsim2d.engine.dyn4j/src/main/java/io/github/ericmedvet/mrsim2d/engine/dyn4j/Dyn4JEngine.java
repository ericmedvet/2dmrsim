/*-
 * ========================LICENSE_START=================================
 * mrsim2d-engine-dyn4j
 * %%
 * Copyright (C) 2020 - 2025 Eric Medvet
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

package io.github.ericmedvet.mrsim2d.engine.dyn4j;

import io.github.ericmedvet.jnb.datastructure.DoubleRange;
import io.github.ericmedvet.mrsim2d.core.Agent;
import io.github.ericmedvet.mrsim2d.core.actions.*;
import io.github.ericmedvet.mrsim2d.core.bodies.Anchor;
import io.github.ericmedvet.mrsim2d.core.bodies.Anchorable;
import io.github.ericmedvet.mrsim2d.core.bodies.Body;
import io.github.ericmedvet.mrsim2d.core.engine.AbstractEngine;
import io.github.ericmedvet.mrsim2d.core.engine.IllegalActionException;
import io.github.ericmedvet.mrsim2d.core.util.PolyUtils;
import java.util.Arrays;
import java.util.Collection;
import java.util.EnumSet;
import java.util.List;
import java.util.stream.Collectors;
import org.dyn4j.dynamics.BodyFixture;
import org.dyn4j.dynamics.ContinuousDetectionMode;
import org.dyn4j.dynamics.Settings;
import org.dyn4j.dynamics.joint.DistanceJoint;
import org.dyn4j.dynamics.joint.Joint;
import org.dyn4j.dynamics.joint.WeldJoint;
import org.dyn4j.geometry.Ray;
import org.dyn4j.geometry.Vector2;
import org.dyn4j.world.DetectFilter;
import org.dyn4j.world.World;
import org.dyn4j.world.result.RaycastResult;

public class Dyn4JEngine extends AbstractEngine {

  private static final Configuration DEFAULT_CONFIGURATION = new Configuration(
      getDefaultSettings(),
      1,
      0.5,
      0.1,
      0.1,
      1,
      0.5,
      1,
      0.5,
      0.1,
      0.1,
      0.35,
      EnumSet.allOf(Voxel.SpringScaffolding.class),
      8d,
      0.3d,
      0.5d,
      10,
      0.1
  );
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
      EnumSet<Voxel.SpringScaffolding> voxelSpringScaffoldings,
      double softLinkSpringF,
      double softLinkSpringD,
      double softLinkRestDistanceRatio,
      double attractionMaxMagnitude,
      double anchorSideDistance
  ) {}

  private static Settings getDefaultSettings() {
    Settings settings = new Settings();
    settings.setContinuousDetectionMode(ContinuousDetectionMode.ALL);
    settings.setVelocityConstraintSolverIterations(20);
    settings.setPositionConstraintSolverIterations(20);
    return settings;
  }

  private double actuateRotationalJoint(
      ActuateRotationalJoint action,
      Agent agent
  ) throws IllegalActionException {
    if (action.body() instanceof RotationalJoint rotationalJoint) {
      double angle = rotationalJoint.jointTargetAngle();
      double targetAngle = rotationalJoint
          .jointActiveAngleRange()
          .denormalize(action.range().normalize(action.value()));
      rotationalJoint.setJointTargetAngle(targetAngle);
      return Math.abs(DoubleRange.SYMMETRIC_UNIT.normalize((angle - targetAngle) / Math.PI));
    }
    throw new IllegalActionException(
        action,
        String.format(
            "Unsupported body type %s",
            action.body().getClass().getSimpleName()
        )
    );
  }

  private double actuateVoxel(ActuateVoxel action, Agent agent) throws IllegalActionException {
    if (action.body() instanceof Voxel voxel) {
      double sideRestL = Math.sqrt(voxel.restArea());
      double energy = Arrays.stream(io.github.ericmedvet.mrsim2d.core.bodies.Voxel.Side.values()).mapToDouble(side -> {
        double diff = sideRestL - voxel.side(side).length();
        if (diff > 0 && action.values().get(side) > 0) { // shorter and further contract
          return diff * action.values().get(side);
        }
        if (diff < 0 && action.values().get(side) < 0) { // longer and further extend
          return diff * action.values().get(side);
        }
        return 0d;
      }).sum();
      voxel.actuate(action.values());
      return energy;
    }
    throw new IllegalActionException(
        action,
        String.format(
            "Unsupported voxel type %s",
            action.body().getClass().getSimpleName()
        )
    );
  }

  private Double attractAnchor(AttractAnchor action, Agent agent) throws IllegalActionException {
    if (action.source().anchorable() == action.destination().anchorable()) {
      throw new IllegalActionException(action, "Cannot attract an anchor of the same body");
    }
    if (action.source().point().distance(action.destination().point()) < super.configuration().attractionRange()) {
      if (action.source() instanceof BodyAnchor src) {
        if (action.destination() instanceof BodyAnchor dst) {
          double f = new DoubleRange(0, configuration.attractionMaxMagnitude).denormalize(action.magnitude());
          Vector2 force = new Vector2(dst.point().diff(src.point()).direction());
          src.getBody().applyForce(force.copy().multiply(f));
          dst.getBody().applyForce(force.copy().multiply(-f));
          return DoubleRange.UNIT.clip(f);
        }
      }
    } else {
      return null;
    }
    throw new IllegalActionException(
        action,
        String.format(
            "Unsupported anchor types: src=%s, dst=%s ",
            action.source().getClass().getSimpleName(),
            action.destination().getClass().getSimpleName()
        )
    );
  }

  private Anchor.Link createLink(CreateLink action, Agent agent) throws IllegalActionException {
    if (action.source()
        .links()
        .stream()
        .anyMatch(l -> l.destination().anchorable().equals(action.destination().anchorable()))) {
      // this anchor is already attached to dst anchorable: ignore
      return null;
    }
    if (action.source() instanceof BodyAnchor src) {
      if (action.destination() instanceof BodyAnchor dst) {
        Joint<org.dyn4j.dynamics.Body> joint;
        if (Anchor.Link.Type.RIGID.equals(action.type())) {
          joint = new WeldJoint<>(
              src.getBody(),
              dst.getBody(),
              new Vector2(src.point().x(), src.point().y())
          );
        } else if (Anchor.Link.Type.SOFT.equals(action.type())) {
          double d = PolyUtils.minAnchorDistance(
              action.source(),
              action.destination()
          ) * configuration.softLinkRestDistanceRatio;
          DistanceJoint<org.dyn4j.dynamics.Body> springJoint = new DistanceJoint<>(
              src.getBody(),
              dst.getBody(),
              Utils.point(src.point()),
              Utils.point(dst.point())
          );
          springJoint.setRestDistance(d);
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
        return new Anchor.Link(src, dst, action.type());
      }
    }
    throw new IllegalActionException(
        action,
        String.format(
            "Unsupported anchor types: src=%s, dst=%s ",
            action.source().getClass().getSimpleName(),
            action.destination().getClass().getSimpleName()
        )
    );
  }

  private RigidBody createRigidBody(CreateRigidBody action, Agent agent) {
    RigidBody rigidBody = new RigidBody(
        action.poly(),
        action.mass(),
        action.anchorsDensity(),
        configuration.rigidBodyFriction,
        configuration.rigidBodyRestitution,
        configuration.rigidBodyLinearDamping,
        configuration.rigidBodyAngularDamping,
        configuration.anchorSideDistance
    );
    rigidBody.getBodies().forEach(world::addBody);
    bodies.add(rigidBody);
    return rigidBody;
  }

  private RotationalJoint createRotationalJoint(CreateRotationalJoint action, Agent agent) {
    RotationalJoint rotationalJoint = new RotationalJoint(
        action.length(),
        action.width(),
        action.mass(),
        action.motor(),
        action.activeAngleRange(),
        configuration.rigidBodyFriction,
        configuration.rigidBodyRestitution,
        configuration.rigidBodyLinearDamping,
        configuration.rigidBodyAngularDamping,
        configuration.anchorSideDistance
    );
    rotationalJoint.getBodies().forEach(world::addBody);
    rotationalJoint.getJoints().forEach(world::addJoint);
    bodies.add(rotationalJoint);
    return rotationalJoint;
  }

  private UnmovableBody createUnmovableBody(CreateUnmovableBody action, Agent agent) {
    UnmovableBody unmovableBody = new UnmovableBody(
        action.poly(),
        action.anchorsDensity(),
        configuration.unmovableBodyFriction,
        configuration.unmovableBodyRestitution,
        configuration.anchorSideDistance
    );
    unmovableBody.getBodies().forEach(world::addBody);
    bodies.add(unmovableBody);
    return unmovableBody;
  }

  private Voxel createVoxel(CreateVoxel action, Agent agent) {
    Voxel voxel = new Voxel(
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

  private Collection<Body> findInContactBodies(FindInContactBodies action, Agent agent) throws IllegalActionException {
    if (action.body() instanceof MultipartBody multipartBody) {
      return multipartBody.getBodies()
          .stream()
          .map(b -> world.getInContactBodies(b, false))
          .flatMap(Collection::stream)
          .filter(
              b -> !multipartBody.getBodies().contains(b) && b.getUserData() != null && b.getUserData() instanceof Body
          )
          .map(b -> (Body) b.getUserData())
          .collect(Collectors.toList());
    }
    throw new IllegalActionException(
        action,
        String.format(
            "Unsupported body type %s",
            action.body().getClass().getSimpleName()
        )
    );
  }

  @Override
  protected Collection<Body> getBodies() {
    return bodies;
  }

  @Override
  protected double innerTick() {
    // control rotational joint
    bodies.forEach(b -> {
      if (b instanceof Actuable actuable) {
        actuable.actuate(t(), t() - configuration.innerSettings().getStepFrequency());
      }
    });
    // tick
    world.step(1);
    return t() + configuration.innerSettings().getStepFrequency();
  }

  @Override
  protected void registerActionSolvers() {
    registerActionSolver(CreateRigidBody.class, this::createRigidBody);
    registerActionSolver(CreateUnmovableBody.class, this::createUnmovableBody);
    registerActionSolver(TranslateBody.class, this::translateBody);
    registerActionSolver(RotateBody.class, this::rotateBody);
    registerActionSolver(CreateVoxel.class, this::createVoxel);
    registerActionSolver(CreateRotationalJoint.class, this::createRotationalJoint);
    registerActionSolver(CreateLink.class, this::createLink);
    registerActionSolver(RemoveLink.class, this::removeLink);
    registerActionSolver(RemoveBody.class, this::removeBody);
    registerActionSolver(ActuateVoxel.class, this::actuateVoxel);
    registerActionSolver(ActuateRotationalJoint.class, this::actuateRotationalJoint);
    registerActionSolver(AttractAnchor.class, this::attractAnchor);
    registerActionSolver(SenseDistanceToBody.class, this::senseDistanceToBody);
    registerActionSolver(FindInContactBodies.class, this::findInContactBodies);
    super.registerActionSolvers();
  }

  private Body removeBody(RemoveBody action, Agent agent) throws IllegalActionException {
    // detach
    if (action.body() instanceof Anchorable anchorable) {
      perform(new DetachAllAnchorsFromAnchorable(anchorable), agent);
    }
    // remove
    if (action.body() instanceof MultipartBody multipartBody) {
      multipartBody.getJoints().forEach(world::removeJoint);
      multipartBody.getBodies().forEach(world::removeBody);
      bodies.remove(action.body());
      return action.body();
    }
    throw new IllegalActionException(
        action,
        String.format(
            "Unsupported body type %s",
            action.body().getClass().getSimpleName()
        )
    );
  }

  private Anchor.Link removeLink(RemoveLink action, Agent agent) throws IllegalActionException {
    if (action.link().source() instanceof BodyAnchor srcAnchor) {
      if (action.link().destination() instanceof BodyAnchor dstAnchor) {
        // remove joint from world
        world.removeJoint(srcAnchor.getJointMap().get(action.link()));
        // remove link from maps
        srcAnchor.getJointMap().remove(action.link());
        dstAnchor.getJointMap().remove(action.link().reversed());
        return action.link();
      }
    }
    throw new IllegalActionException(
        action,
        String.format(
            "Unsupported anchor types: src=%s, dst=%s ",
            action.link().source().getClass().getSimpleName(),
            action.link().destination().getClass().getSimpleName()
        )
    );
  }

  private Body rotateBody(RotateBody action, Agent agent) throws IllegalActionException {
    if (action.body() instanceof MultipartBody multipartBody) {
      multipartBody
          .getBodies()
          .forEach(
              b -> b.rotate(
                  action.angle(),
                  action.point().x(),
                  action.point().y()
              )
          );
      return action.body();
    }
    throw new IllegalActionException(
        action,
        String.format(
            "Untranslatable body type: %s",
            action.body().getClass().getName()
        )
    );
  }

  private Double senseDistanceToBody(SenseDistanceToBody action, Agent agent) {
    Ray ray = new Ray(
        Utils.point(action.body().poly().center()),
        action.direction() + action.body().angle()
    );
    List<RaycastResult<org.dyn4j.dynamics.Body, BodyFixture>> results = world.raycast(
        ray,
        action.distanceRange(),
        new DetectFilter<>(true, true, new BodyOwnerFilter(action.body()))
    );
    return results.stream()
        .mapToDouble(r -> r.getRaycast().getDistance())
        .min()
        .orElse(action.distanceRange());
  }

  private Body translateBody(TranslateBody action, Agent agent) throws IllegalActionException {
    double tx = action.translation().x();
    double ty = action.translation().y();
    if (action.body() instanceof MultipartBody multipartBody) {
      multipartBody.getBodies().forEach(b -> b.translate(tx, ty));
      return action.body();
    }
    throw new IllegalActionException(
        action,
        String.format(
            "Untranslatable body type: %s",
            action.body().getClass().getName()
        )
    );
  }
}
