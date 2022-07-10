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

import it.units.erallab.mrsim.viewer.drawers.AgentsDrawer;
import it.units.erallab.mrsim.viewer.drawers.BodiesDrawer;
import it.units.erallab.mrsim.viewer.drawers.InfoDrawer;
import it.units.erallab.mrsim.viewer.drawers.body.AnchorableBodyDrawer;
import it.units.erallab.mrsim.viewer.drawers.body.RigidBodyDrawer;
import it.units.erallab.mrsim.viewer.drawers.body.SoftBodyDrawer;
import it.units.erallab.mrsim.viewer.drawers.body.UnmovableBodyDrawer;
import it.units.erallab.mrsim.viewer.framers.AllAgentsFramer;
import it.units.erallab.mrsim.viewer.framers.AllBodiesFramer;
import it.units.erallab.mrsim.viewer.framers.StaticFramer;

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
        new InfoDrawer(string)
    );
  }

  public static Drawer basic() {
    return basic("");
  }

  public static Drawer world() {
    return Drawer.transform(
        new AllAgentsFramer(2.5d),
        Drawer.of(
            new BodiesDrawer(List.of(
                new UnmovableBodyDrawer(),
                new RigidBodyDrawer(),
                new SoftBodyDrawer().andThen(new AnchorableBodyDrawer())
            )),
            new AgentsDrawer(List.of(
            ))
        )
    );
  }

}
