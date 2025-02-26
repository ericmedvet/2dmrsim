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
import io.github.ericmedvet.mrsim2d.core.agents.independentvoxel.NumIndependentVoxel;
import io.github.ericmedvet.mrsim2d.core.geometry.Path;
import io.github.ericmedvet.mrsim2d.core.geometry.Point;
import io.github.ericmedvet.mrsim2d.core.geometry.Terrain;
import io.github.ericmedvet.mrsim2d.core.tasks.locomotion.Locomotion;
import io.github.ericmedvet.mrsim2d.viewer.Drawers;
import io.github.ericmedvet.mrsim2d.viewer.RealtimeViewer;
import java.util.List;
import java.util.function.Supplier;

public class Tester {
  public static void main(String[] args) {
    Path p = new Path(new Point(20, 0));
    p = p.moveBy(0, -10).moveBy(1.05, 0).moveBy(0, 10).moveBy(9, 0);
    p = p.moveBy(30, 0);
    Locomotion locomotion = new Locomotion(30, Terrain.fromPath(p, 25, 10, 100), true, 18.75, .25);
    Supplier<EmbodiedAgent> supplier = () -> new NumIndependentVoxel(
        List.of(),
        NumIndependentVoxel.AreaActuation.SIDES,
        true,
        0,
        NumericalStatelessSystem.from(0, 8, (d, a) -> new double[]{0, 0, 0, 0, 1, 1, 1, 1})
    );
    locomotion.run(supplier, new Dyn4JEngine(), new RealtimeViewer(Drawers.basic()));
  }
}
