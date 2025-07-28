
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
import io.github.ericmedvet.mrsim2d.core.engine.Engine;
import io.github.ericmedvet.mrsim2d.engine.dyn4j.Dyn4JEngine;

module io.github.ericmedvet.mrsim2d.engine.dyn4j {
  requires org.dyn4j;
  requires io.github.ericmedvet.mrsim2d.core;
  requires io.github.ericmedvet.mrsim2d.viewer;
  requires io.github.ericmedvet.jnb.datastructure;
  requires java.desktop;
  requires io.github.ericmedvet.jsdynsym.control;
  requires io.github.ericmedvet.jsdynsym.core;

  provides Engine with Dyn4JEngine;

  exports io.github.ericmedvet.mrsim2d.engine.dyn4j.drawers;
}
