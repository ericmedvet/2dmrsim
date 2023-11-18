/*-
 * ========================LICENSE_START=================================
 * mrsim2d-viewer
 * %%
 * Copyright (C) 2020 - 2023 Eric Medvet
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

package io.github.ericmedvet.mrsim2d.viewer;

import io.github.ericmedvet.mrsim2d.core.Snapshot;
import io.github.ericmedvet.mrsim2d.core.geometry.BoundingBox;
import io.github.ericmedvet.mrsim2d.core.geometry.Point;
import io.github.ericmedvet.mrsim2d.viewer.drawers.*;
import io.github.ericmedvet.mrsim2d.viewer.drawers.actions.AttractAnchor;
import io.github.ericmedvet.mrsim2d.viewer.drawers.actions.SenseDistanceToBody;
import io.github.ericmedvet.mrsim2d.viewer.drawers.actions.SenseRotatedVelocity;
import io.github.ericmedvet.mrsim2d.viewer.drawers.bodies.*;
import io.github.ericmedvet.mrsim2d.viewer.framers.AllAgentsFramer;
import java.util.List;

public class Drawers {

  private Drawers() {}

  public static Drawer basic(String string) {
    return Drawer.of(Drawer.clear(), world(), new InfoDrawer(string), new EngineProfilingDrawer());
  }

  public static Drawer basic() {
    return basic("");
  }

  public static Drawer basicWithAgentMiniature(String string) {
    return Drawer.of(
        Drawer.clear(),
        world(),
        new StackedMultipliedDrawer<>(
            Drawers::simpleAgentWithVelocities,
            new EmbodiedAgentsExtractor(),
            0.2,
            0.05,
            StackedMultipliedDrawer.Direction.VERTICAL,
            Drawer.VerticalPosition.TOP,
            Drawer.HorizontalPosition.RIGHT),
        new InfoDrawer(string),
        new EngineProfilingDrawer());
  }

  public static Drawer basicWithMiniWorld(String string) {
    return Drawer.of(
        Drawer.clear(),
        world(),
        Drawer.clip(new BoundingBox(new Point(0.5d, 0.01d), new Point(0.95d, 0.2d)), miniWorld()),
        new InfoDrawer(string),
        new EngineProfilingDrawer());
  }

  public static Drawer miniWorld() {
    return Drawer.transform(
        new AllAgentsFramer(10d).largest(2d),
        Drawer.of(new ComponentsDrawer(
                List.of(
                    new UnmovableBodyDrawer(),
                    new RotationalJointDrawer(),
                    new SoftBodyDrawer(),
                    new RigidBodyDrawer()),
                Snapshot::bodies)
            .onLastSnapshot()));
  }

  public static Drawer simpleAgent() {
    return Drawer.transform(
        new AllAgentsFramer(1.1d).largest(2d),
        Drawer.of(new ComponentsDrawer(
                List.of(new RotationalJointDrawer(), new SoftBodyDrawer(), new RigidBodyDrawer()),
                Snapshot::bodies)
            .onLastSnapshot()));
  }

  public static Drawer simpleAgentWithBrainsIO() {
    return Drawer.of(
        Drawer.clip(new BoundingBox(new Point(0, 0), new Point(0.30, 1)), simpleAgent()),
        Drawer.clip(new BoundingBox(new Point(0.30, 0), new Point(1, 1)), new NumMultiBrainedIODrawer()));
  }

  public static Drawer simpleAgentWithVelocities() {
    FirstAgentVelocityExtractor velocityExtractor = new FirstAgentVelocityExtractor(2d);
    return Drawer.of(
        Drawer.clip(new BoundingBox(new Point(0, 0), new Point(0.30, 1)), simpleAgent()),
        Drawer.clip(
            new BoundingBox(new Point(0.34, 0), new Point(0.65, 1)),
            new LinePlotter(
                velocityExtractor.andThen(p -> p.map(Point::x).orElse(0d)), 10, "vx=%+4.1f")),
        Drawer.clip(
            new BoundingBox(new Point(0.67, 0), new Point(1, 1)),
            new LinePlotter(
                velocityExtractor.andThen(p -> p.map(Point::y).orElse(0d)), 10, "vy=%+4.1f")));
  }

  public static Drawer world() {
    return Drawer.transform(
        new AllAgentsFramer(1.5d).largest(2d),
        Drawer.of(
            new ComponentsDrawer(
                    List.of(
                        new UnmovableBodyDrawer().andThen(new AnchorableBodyDrawer()),
                        new RotationalJointDrawer().andThen(new AnchorableBodyDrawer()),
                        new SoftBodyDrawer().andThen(new AnchorableBodyDrawer()),
                        new RigidBodyDrawer().andThen(new AnchorableBodyDrawer())),
                    Snapshot::bodies)
                .onLastSnapshot(),
            new NFCDrawer(),
            new ComponentsDrawer(
                List.of(
                    // new CreateLink(), // both slow, because they add continuously drawers...
                    // new RemoveLink(),
                    new AttractAnchor(), new SenseDistanceToBody(), new SenseRotatedVelocity()),
                Snapshot::actionOutcomes)));
  }
}
