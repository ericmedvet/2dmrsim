/*
 * Copyright 2022 eric
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

package io.github.ericmedvet.mrsim2d.buildable.builders;

import io.github.ericmedvet.jnb.core.NamedBuilder;
import io.github.ericmedvet.jnb.core.Param;
import io.github.ericmedvet.jnb.core.ParamMap;
import io.github.ericmedvet.mrsim2d.core.Snapshot;
import io.github.ericmedvet.mrsim2d.core.engine.Engine;
import io.github.ericmedvet.mrsim2d.core.geometry.BoundingBox;
import io.github.ericmedvet.mrsim2d.core.geometry.Point;
import io.github.ericmedvet.mrsim2d.core.tasks.Task;
import io.github.ericmedvet.mrsim2d.core.util.DoubleRange;
import io.github.ericmedvet.mrsim2d.viewer.Drawer;
import io.github.ericmedvet.mrsim2d.viewer.Drawers;
import io.github.ericmedvet.mrsim2d.viewer.EmbodiedAgentsExtractor;
import io.github.ericmedvet.mrsim2d.viewer.drawers.ComponentsDrawer;
import io.github.ericmedvet.mrsim2d.viewer.drawers.EngineProfilingDrawer;
import io.github.ericmedvet.mrsim2d.viewer.drawers.InfoDrawer;
import io.github.ericmedvet.mrsim2d.viewer.drawers.StackedMultipliedDrawer;
import io.github.ericmedvet.mrsim2d.viewer.drawers.actions.AttractAnchor;
import io.github.ericmedvet.mrsim2d.viewer.drawers.actions.SenseDistanceToBody;
import io.github.ericmedvet.mrsim2d.viewer.drawers.actions.SenseRotatedVelocity;
import io.github.ericmedvet.mrsim2d.viewer.drawers.bodies.*;
import io.github.ericmedvet.mrsim2d.viewer.framers.AllAgentsFramer;

import java.util.*;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.random.RandomGenerator;

public class Misc {

  private Misc() {
  }

  public enum MiniAgentInfo {NONE, VELOCITY, BRAINS}

  @SuppressWarnings("unused")
  public static RandomGenerator defaultRG(@Param(value = "seed", dI = 0) int seed) {
    return seed >= 0 ? new Random(seed) : new Random();
  }

  @SuppressWarnings("unused")
  public static Function<String, Drawer> drawer(
      @Param(value = "enlargement", dD = 1.5) double enlargement,
      @Param(value = "followTime", dD = 2) double followTime,
      @Param(value = "profilingTime", dD = 1) double profilingTime,
      @Param(value = "miniWorldEnlargement", dD = 10) double miniWorldEnlargement,
      @Param(value = "miniWorld") boolean miniWorld,
      @Param(value = "miniAgents", dS = "brains") MiniAgentInfo miniAgentInfo,
      @Param(value = "engineProfiling") boolean engineProfiling,
      @Param(value = "actions") boolean actions
  ) {
    return s -> {
      Drawer baseDrawer = new ComponentsDrawer(
          List.of(
              new UnmovableBodyDrawer().andThen(new AnchorableBodyDrawer()),
              new RotationalJointDrawer().andThen(new AnchorableBodyDrawer()),
              new SoftBodyDrawer().andThen(new AnchorableBodyDrawer()),
              new RigidBodyDrawer().andThen(new AnchorableBodyDrawer())
          ), Snapshot::bodies
      ).onLastSnapshot();
      Drawer actionsDrawer = new ComponentsDrawer(
          List.of(
              new AttractAnchor(),
              new SenseDistanceToBody(),
              new SenseRotatedVelocity()
          ), Snapshot::actionOutcomes
      );
      Drawer worldDrawer = Drawer.transform(
          new AllAgentsFramer(enlargement).largest(followTime),
          actions ? Drawer.of(baseDrawer, actionsDrawer) : baseDrawer
      );
      List<Drawer> drawers = new ArrayList<>(List.of(
          Drawer.clear(),
          worldDrawer
      ));
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
            Drawer.HorizontalPosition.RIGHT
        ));
      }
      if (miniWorld) {
        drawers.add(Drawer.clip(
                new BoundingBox(new Point(0.5d, 0.85d), new Point(0.99d, 0.99d)),
                Drawer.of(
                    Drawer.clear(),
                    Drawer.transform(
                        new AllAgentsFramer(miniWorldEnlargement).largest(followTime),
                        Drawer.of(
                            new ComponentsDrawer(
                                List.of(
                                    new UnmovableBodyDrawer(),
                                    new RotationalJointDrawer(),
                                    new SoftBodyDrawer(),
                                    new RigidBodyDrawer()
                                ), Snapshot::bodies
                            ).onLastSnapshot()
                        )
                    )
                )
            )
        );
      }
      if (engineProfiling) {
        drawers.add(new EngineProfilingDrawer(
            profilingTime,
            Drawer.VerticalPosition.BOTTOM,
            Drawer.HorizontalPosition.LEFT
        ));
      }
      drawers.add(new InfoDrawer(s));
      return Drawer.of(Collections.unmodifiableList(drawers));
    };
  }

  @SuppressWarnings("unused")
  public static Supplier<Engine> engine() {
    return () -> ServiceLoader.load(Engine.class).findFirst().orElseThrow();
  }

  @SuppressWarnings("unused")
  public static DoubleRange range(
      @Param("min") double min,
      @Param("max") double max
  ) {
    return new DoubleRange(min, max);
  }

  @SuppressWarnings("unused")
  public static <T> Supplier<T> supplier(
      @Param("of") T target,
      @Param(value = "", injection = Param.Injection.MAP) ParamMap map,
      @Param(value = "", injection = Param.Injection.BUILDER) NamedBuilder<?> builder
  ) {
    //noinspection unchecked
    return () -> (T) builder.build(map.npm("of"));
  }

  @SuppressWarnings("unused")
  public static <A, O> Function<A, O> taskRunner(
      @Param("task") Task<A, O> task,
      @Param(value = "engine", dNPM = "sim.engine()") Supplier<Engine> engineSupplier
  ) {
    return a -> task.run(a, engineSupplier.get());
  }


}
