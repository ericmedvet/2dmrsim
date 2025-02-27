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

import io.github.ericmedvet.jsdynsym.core.numerical.NumericalStatelessSystem;
import io.github.ericmedvet.mrsim2d.core.EmbodiedAgent;
import io.github.ericmedvet.mrsim2d.core.Snapshot;
import io.github.ericmedvet.mrsim2d.core.agents.independentvoxel.NumIndependentVoxel;
import io.github.ericmedvet.mrsim2d.core.geometry.BoundingBox;
import io.github.ericmedvet.mrsim2d.core.geometry.Path;
import io.github.ericmedvet.mrsim2d.core.geometry.Point;
import io.github.ericmedvet.mrsim2d.core.geometry.Terrain;
import io.github.ericmedvet.mrsim2d.core.tasks.locomotion.Locomotion;
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
import java.util.function.Supplier;

public class Tester {
  public static void main(String[] args) {
    Path p = new Path(new Point(20, 0));
    p = p.moveBy(0, -10).moveBy(1.05, 0).moveBy(0, 10).moveBy(9, 0);
    p = p.moveBy(30, 0);
    Locomotion locomotion = new Locomotion(30, Terrain.fromPath(p, 25, 10, 100), true, 11.75, .25);
    Supplier<EmbodiedAgent> supplier = () -> new NumIndependentVoxel(
            List.of(),
            NumIndependentVoxel.AreaActuation.SIDES,
            true,
            0,
            NumericalStatelessSystem.from(0, 8, (d, a) -> new double[]{0, 0, 0, 0, -1, -1, -1, -1})
    );
    List<ComponentDrawer> componentDrawers = List.of(
              new SoftBodyDrawer().andThen(new AnchorableBodyDrawer()),
              new RigidBodyDrawer().andThen(new AnchorableBodyDrawer()),
              new UnmovableBodyDrawer().andThen(new AnchorableBodyDrawer()),
              new RotationalJointDrawer().andThen(new AnchorableBodyDrawer())
            );
    Drawer baseDrawer = new ComponentsDrawer(componentDrawers, Snapshot::bodies).onLastSnapshot();
    Drawer actionsDrawer = new ComponentsDrawer(
            List.of(new AttractAnchor(), new SenseDistanceToBody(), new SenseRotatedVelocity()),
            Snapshot::actionOutcomes
    );
    List<Drawer> thingsDrawers = new ArrayList<>();
    thingsDrawers.add(baseDrawer);
    thingsDrawers.add(actionsDrawer);
    thingsDrawers.add(new ComponentsDrawer(List.of(new MultipartBodyDrawer()), Snapshot::bodies));
    Drawer worldDrawer = Drawer.transform(new StaticFramer(new BoundingBox(new Point(-20, -1), new Point(40, 3))),
            Drawer.of(Collections.unmodifiableList(thingsDrawers)));
    List<Drawer> drawers = new ArrayList<>(List.of(Drawer.clear(), worldDrawer));
    Drawer drawer = Drawer.of(Collections.unmodifiableList(drawers));
    locomotion.run(supplier, new Dyn4JEngine(), new RealtimeViewer(drawer));
  }
}
