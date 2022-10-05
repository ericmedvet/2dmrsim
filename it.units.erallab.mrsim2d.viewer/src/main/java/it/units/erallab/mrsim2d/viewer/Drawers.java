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

package it.units.erallab.mrsim2d.viewer;

import it.units.erallab.mrsim2d.core.Snapshot;
import it.units.erallab.mrsim2d.core.geometry.BoundingBox;
import it.units.erallab.mrsim2d.core.geometry.Point;
import it.units.erallab.mrsim2d.viewer.drawers.ComponentsDrawer;
import it.units.erallab.mrsim2d.viewer.drawers.EngineProfilingDrawer;
import it.units.erallab.mrsim2d.viewer.drawers.InfoDrawer;
import it.units.erallab.mrsim2d.viewer.drawers.StackedComponentsDrawer;
import it.units.erallab.mrsim2d.viewer.drawers.actions.AttractAnchor;
import it.units.erallab.mrsim2d.viewer.drawers.actions.SenseDistanceToBody;
import it.units.erallab.mrsim2d.viewer.drawers.actions.SenseRotatedVelocity;
import it.units.erallab.mrsim2d.viewer.drawers.bodies.*;
import it.units.erallab.mrsim2d.viewer.framers.AllAgentsFramer;

import java.util.List;

/**
 * @author "Eric Medvet" on 2022/07/07 for 2dmrsim
 */
public class Drawers {

  private Drawers() {
  }

  public static Drawer basic(String string) {
    return Drawer.of(
        Drawer.clear(),
        world(),
        new InfoDrawer(string),
        new EngineProfilingDrawer()
    );
  }

  public static Drawer basicWithAgentMignature(String string) {
    return Drawer.of(
        Drawer.clear(),
        world(),
        new StackedComponentsDrawer<>(
            Drawers::simpleAgent,
            s -> List.of(s, s),
            new BoundingBox(new Point(0.9d, 0.01d), new Point(0.99d, 0.1d)),
            StackedComponentsDrawer.Direction.VERTICAL
        ),
        new InfoDrawer(string)
    );
  }

  public static Drawer basicWithMiniWorld(String string) {
    return Drawer.of(
        Drawer.clear(),
        world(),
        Drawer.clip(
            new BoundingBox(new Point(0.5d, 0.01d), new Point(0.95d, 0.2d)),
            miniWorld()
        ),
        new InfoDrawer(string),
        new EngineProfilingDrawer()
    );
  }

  public static Drawer basic() {
    return basic("");
  }

  public static Drawer world() {
    return Drawer.transform(
        new AllAgentsFramer(2.5d).largest(2d),
        Drawer.of(
            new ComponentsDrawer(
                List.of(
                    new UnmovableBodyDrawer().andThen(new AnchorableBodyDrawer()),
                    new RotationalJointDrawer().andThen(new AnchorableBodyDrawer()),
                    new SoftBodyDrawer().andThen(new AnchorableBodyDrawer()),
                    new RigidBodyDrawer().andThen(new AnchorableBodyDrawer())
                ), Snapshot::bodies
            ).onLastSnapshot(),
            new ComponentsDrawer(
                List.of(
                    //new CreateLink(), // both slow, because they add continuously drawers...
                    //new RemoveLink(),
                    new AttractAnchor(),
                    new SenseDistanceToBody(),
                    new SenseRotatedVelocity()
                ), Snapshot::actionOutcomes
            )
        )
    );
  }

  public static Drawer miniWorld() {
    return Drawer.transform(
        new AllAgentsFramer(10d).largest(2d),
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
    );
  }

  public static Drawer simpleAgent() {
    return Drawer.transform(
        new AllAgentsFramer(1d).largest(2d),
        Drawer.of(
            new ComponentsDrawer(
                List.of(
                    new RotationalJointDrawer(),
                    new SoftBodyDrawer(),
                    new RigidBodyDrawer()
                ), Snapshot::bodies
            ).onLastSnapshot()
        )
    );
  }

}
