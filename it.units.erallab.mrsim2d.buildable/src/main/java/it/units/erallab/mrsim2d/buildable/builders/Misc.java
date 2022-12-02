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

package it.units.erallab.mrsim2d.buildable.builders;

import it.units.erallab.mrsim2d.core.Snapshot;
import it.units.erallab.mrsim2d.core.engine.Engine;
import it.units.erallab.mrsim2d.core.geometry.BoundingBox;
import it.units.erallab.mrsim2d.core.geometry.Point;
import it.units.erallab.mrsim2d.core.tasks.Task;
import it.units.erallab.mrsim2d.core.util.DoubleRange;
import it.units.erallab.mrsim2d.viewer.Drawer;
import it.units.erallab.mrsim2d.viewer.Drawers;
import it.units.erallab.mrsim2d.viewer.EmbodiedAgentsExtractor;
import it.units.erallab.mrsim2d.viewer.drawers.ComponentsDrawer;
import it.units.erallab.mrsim2d.viewer.drawers.EngineProfilingDrawer;
import it.units.erallab.mrsim2d.viewer.drawers.InfoDrawer;
import it.units.erallab.mrsim2d.viewer.drawers.StackedMultipliedDrawer;
import it.units.erallab.mrsim2d.viewer.drawers.actions.AttractAnchor;
import it.units.erallab.mrsim2d.viewer.drawers.actions.SenseDistanceToBody;
import it.units.erallab.mrsim2d.viewer.drawers.actions.SenseRotatedVelocity;
import it.units.erallab.mrsim2d.viewer.drawers.bodies.*;
import it.units.erallab.mrsim2d.viewer.framers.AllAgentsFramer;
import it.units.malelab.jnb.core.NamedBuilder;
import it.units.malelab.jnb.core.Param;
import it.units.malelab.jnb.core.ParamMap;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.ServiceLoader;
import java.util.function.Function;
import java.util.function.Supplier;

public class Misc {

  private Misc() {
  }

  @SuppressWarnings("unused")
  public static Function<String, Drawer> drawer(
      @Param(value = "enlargement", dD = 1.5) double enlargement,
      @Param(value = "followTime", dD = 2) double followTime,
      @Param(value = "profilingTime", dD = 1) double profilingTime,
      @Param(value = "miniWorldEnlargement", dD = 10) double miniWorldEnlargement,
      @Param(value = "miniWorld") boolean miniWorld,
      @Param(value = "miniAgents", dB = true) boolean miniAgents,
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
      if (miniAgents) {
        drawers.add(new StackedMultipliedDrawer<>(
            Drawers::simpleAgentWithVelocities,
            new EmbodiedAgentsExtractor(),
            0.2,
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
