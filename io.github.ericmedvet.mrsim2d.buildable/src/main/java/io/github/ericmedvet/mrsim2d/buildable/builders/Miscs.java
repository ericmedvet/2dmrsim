/*-
 * ========================LICENSE_START=================================
 * mrsim2d-buildable
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

package io.github.ericmedvet.mrsim2d.buildable.builders;

import io.github.ericmedvet.jnb.core.Discoverable;
import io.github.ericmedvet.jnb.core.Param;
import io.github.ericmedvet.mrsim2d.core.Snapshot;
import io.github.ericmedvet.mrsim2d.core.engine.Engine;
import io.github.ericmedvet.mrsim2d.core.geometry.BoundingBox;
import io.github.ericmedvet.mrsim2d.core.geometry.Point;
import io.github.ericmedvet.mrsim2d.core.tasks.AgentsObservation;
import io.github.ericmedvet.mrsim2d.core.tasks.AgentsOutcome;
import io.github.ericmedvet.mrsim2d.core.tasks.Task;
import io.github.ericmedvet.mrsim2d.engine.dyn4j.drawers.MultipartBodyDrawer;
import io.github.ericmedvet.mrsim2d.viewer.*;
import io.github.ericmedvet.mrsim2d.viewer.drawers.*;
import io.github.ericmedvet.mrsim2d.viewer.drawers.actions.AttractAnchor;
import io.github.ericmedvet.mrsim2d.viewer.drawers.actions.SenseDistanceToBody;
import io.github.ericmedvet.mrsim2d.viewer.drawers.actions.SenseRotatedVelocity;
import io.github.ericmedvet.mrsim2d.viewer.drawers.bodies.*;
import io.github.ericmedvet.mrsim2d.viewer.framers.AllAgentsFramer;
import io.github.ericmedvet.mrsim2d.viewer.framers.StaticFramer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.ServiceLoader;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.IntStream;

@Discoverable(prefixTemplate = "sim|s")
public class Miscs {

  private Miscs() {}

  public enum MiniAgentInfo {
    NONE,
    VELOCITY,
    BRAINS
  }

  @SuppressWarnings("unused")
  public static Framer<Snapshot> allAgentsFramer(
      @Param(value = "enlargement", dD = 1.5) double enlargement,
      @Param(value = "followTime", dD = 2) double followTime) {
    return new AllAgentsFramer(enlargement).largest(followTime);
  }

  @SuppressWarnings("unused")
  public static Function<String, Drawer> drawer(
      @Param(value = "framer", dNPM = "s.allAgentsFramer()") Framer<Snapshot> framer,
      @Param(value = "profilingTime", dD = 1) double profilingTime,
      @Param(value = "miniWorldEnlargement", dD = 10) double miniWorldEnlargement,
      @Param(value = "miniWorld") boolean miniWorld,
      @Param(
              value = "components",
              dSs = {"unmovable_bodies", "soft_bodies", "rigid_bodies", "rotational_joints"})
          List<Drawers.Component> components,
      @Param(value = "miniAgents", dS = "brains") MiniAgentInfo miniAgentInfo,
      @Param(value = "engineProfiling") boolean engineProfiling,
      @Param(value = "actions") boolean actions,
      @Param(value = "info", dB = true) boolean info,
      @Param(value = "nfc") boolean nfc,
      @Param(value = "parts") boolean parts) {
    return s -> {
      List<ComponentDrawer> componentDrawers = components.stream()
          .map(c -> switch (c) {
            case SOFT_BODIES -> new SoftBodyDrawer().andThen(new AnchorableBodyDrawer());
            case RIGID_BODIES -> new RigidBodyDrawer().andThen(new AnchorableBodyDrawer());
            case UNMOVABLE_BODIES -> new UnmovableBodyDrawer().andThen(new AnchorableBodyDrawer());
            case ROTATIONAL_JOINTS -> new RotationalJointDrawer().andThen(new AnchorableBodyDrawer());
          })
          .toList();
      Drawer baseDrawer = new ComponentsDrawer(componentDrawers, Snapshot::bodies).onLastSnapshot();
      Drawer nfcDrawer = new NFCDrawer();
      Drawer actionsDrawer = new ComponentsDrawer(
          List.of(new AttractAnchor(), new SenseDistanceToBody(), new SenseRotatedVelocity()),
          Snapshot::actionOutcomes);
      List<Drawer> thingsDrawers = new ArrayList<>();
      thingsDrawers.add(baseDrawer);
      if (actions) {
        thingsDrawers.add(actionsDrawer);
      }
      if (nfc) {
        thingsDrawers.add(nfcDrawer);
      }
      if (parts) {
        thingsDrawers.add(new ComponentsDrawer(List.of(new MultipartBodyDrawer()), Snapshot::bodies));
      }
      Drawer worldDrawer = Drawer.transform(framer, Drawer.of(Collections.unmodifiableList(thingsDrawers)));
      List<Drawer> drawers = new ArrayList<>(List.of(Drawer.clear(), worldDrawer));
      if (!miniAgentInfo.equals(MiniAgentInfo.NONE)) {
        drawers.add(new StackedMultipliedDrawer<>(
            switch (miniAgentInfo) {
              case BRAINS -> Drawers::simpleAgentWithBrainsIO;
              case VELOCITY -> Drawers::simpleAgentWithVelocities;
              default -> throw new IllegalStateException("Unexpected value: " + miniAgentInfo);
            },
            new EmbodiedAgentsExtractor(),
            0.25,
            0.05,
            StackedMultipliedDrawer.Direction.VERTICAL,
            Drawer.VerticalPosition.TOP,
            Drawer.HorizontalPosition.RIGHT));
      }
      if (miniWorld) {
        drawers.add(Drawer.clip(
            new BoundingBox(new Point(0.5d, 0.85d), new Point(0.99d, 0.99d)),
            Drawer.of(
                Drawer.clear(),
                Drawer.transform(
                    new AllAgentsFramer(miniWorldEnlargement).largest(2),
                    Drawer.of(new ComponentsDrawer(
                            List.of(
                                new UnmovableBodyDrawer(),
                                new RotationalJointDrawer(),
                                new SoftBodyDrawer(),
                                new RigidBodyDrawer()),
                            Snapshot::bodies)
                        .onLastSnapshot())))));
      }
      if (engineProfiling) {
        drawers.add(new EngineProfilingDrawer(
            profilingTime, Drawer.VerticalPosition.BOTTOM, Drawer.HorizontalPosition.LEFT));
      }
      if (info) {
        drawers.add(new InfoDrawer(s));
      }
      return Drawer.of(Collections.unmodifiableList(drawers));
    };
  }

  @SuppressWarnings("unused")
  public static Supplier<Engine> engine() {
    return () -> ServiceLoader.load(Engine.class).findFirst().orElseThrow();
  }

  @SuppressWarnings("unused")
  public static Framer<Snapshot> staticFramer(
      @Param("minX") double minX,
      @Param("maxX") double maxX,
      @Param("minY") double minY,
      @Param("maxY") double maxY) {
    return new StaticFramer(new BoundingBox(new Point(minX, minY), new Point(maxX, maxY)));
  }

  @SuppressWarnings("unused")
  public static <A, S extends AgentsObservation, O extends AgentsOutcome<S>> Function<A, List<O>> taskMultiRunner(
      @Param("task") Task<A, S, O> task,
      @Param("repetitions") int repetitions,
      @Param(value = "engine", dNPM = "sim.engine()") Supplier<Engine> engineSupplier) {
    return a -> IntStream.range(0, repetitions)
        .boxed()
        .map(i -> task.run(a, engineSupplier.get()))
        .toList();
  }

  @SuppressWarnings("unused")
  public static <A, S extends AgentsObservation, O extends AgentsOutcome<S>> Function<A, O> taskRunner(
      @Param("task") Task<A, S, O> task,
      @Param(value = "engine", dNPM = "sim.engine()") Supplier<Engine> engineSupplier) {
    return a -> task.run(a, engineSupplier.get());
  }

  @SuppressWarnings("unused")
  public static <A> TaskVideoBuilder<A> taskVideoBuilder(
      @Param("task") Task<A, ?, ?> task,
      @Param(value = "title", dS = "") String title,
      @Param(value = "drawer", dNPM = "sim.drawer()") Function<String, Drawer> drawerBuilder,
      @Param(value = "engine", dNPM = "sim.engine()") Supplier<Engine> engineSupplier,
      @Param(value = "startTime", dD = 0) double startTime,
      @Param(value = "endTime", dD = Double.POSITIVE_INFINITY) double endTime,
      @Param(value = "frameRate", dD = 30) double frameRate) {
    return new TaskVideoBuilder<>(task, drawerBuilder, engineSupplier, title, startTime, endTime, frameRate);
  }
}
