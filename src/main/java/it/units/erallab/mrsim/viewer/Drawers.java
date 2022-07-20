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

package it.units.erallab.mrsim.viewer;

import it.units.erallab.mrsim.core.Snapshot;
import it.units.erallab.mrsim.viewer.drawers.ComponentsDrawer;
import it.units.erallab.mrsim.viewer.drawers.EngineProfilingDrawer;
import it.units.erallab.mrsim.viewer.drawers.InfoDrawer;
import it.units.erallab.mrsim.viewer.drawers.actions.AttractAnchor;
import it.units.erallab.mrsim.viewer.drawers.actions.SenseDistanceToBody;
import it.units.erallab.mrsim.viewer.drawers.actions.SenseRotatedVelocity;
import it.units.erallab.mrsim.viewer.drawers.bodies.AnchorableBodyDrawer;
import it.units.erallab.mrsim.viewer.drawers.bodies.RigidBodyDrawer;
import it.units.erallab.mrsim.viewer.drawers.bodies.SoftBodyDrawer;
import it.units.erallab.mrsim.viewer.drawers.bodies.UnmovableBodyDrawer;
import it.units.erallab.mrsim.viewer.framers.AllAgentsFramer;

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

  public static Drawer basic() {
    return basic("");
  }

  public static Drawer world() {
    return Drawer.transform(
        new AllAgentsFramer(2.5d).largest(2d),
        Drawer.of(
            new ComponentsDrawer(
                List.of(
                    new UnmovableBodyDrawer(),
                    new RigidBodyDrawer(),
                    new SoftBodyDrawer().andThen(new AnchorableBodyDrawer())
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

}
