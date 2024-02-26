/*-
 * ========================LICENSE_START=================================
 * mrsim2d-buildable
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
import io.github.ericmedvet.mrsim2d.core.engine.Engine;

module io.github.ericmedvet.mrsim2d.buildable {
  uses Engine;

  requires io.github.ericmedvet.mrsim2d.core;
  requires io.github.ericmedvet.mrsim2d.viewer;
  requires io.github.ericmedvet.jnb.core;
  requires io.github.ericmedvet.jsdynsym.buildable;
  requires io.github.ericmedvet.jsdynsym.core;
  requires io.github.ericmedvet.jnb.datastructure;

  opens io.github.ericmedvet.mrsim2d.buildable.builders to
      io.github.ericmedvet.jnb.core;

  exports io.github.ericmedvet.mrsim2d.buildable.builders;
}
