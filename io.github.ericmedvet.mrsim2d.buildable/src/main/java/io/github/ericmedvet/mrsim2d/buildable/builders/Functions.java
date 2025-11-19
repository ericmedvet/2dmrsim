/*-
 * ========================LICENSE_START=================================
 * mrsim2d-buildable
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
package io.github.ericmedvet.mrsim2d.buildable.builders;

import io.github.ericmedvet.jnb.core.Cacheable;
import io.github.ericmedvet.jnb.core.Discoverable;
import io.github.ericmedvet.jnb.core.Param;
import io.github.ericmedvet.jnb.datastructure.FormattedNamedFunction;
import io.github.ericmedvet.jnb.datastructure.Grid;
import io.github.ericmedvet.jnb.datastructure.NamedFunction;
import io.github.ericmedvet.jsdynsym.core.numerical.NumericalDynamicalSystem;
import io.github.ericmedvet.jviz.core.drawer.Video;
import io.github.ericmedvet.mrsim2d.core.NumMultiBrained;
import io.github.ericmedvet.mrsim2d.core.agents.gridvsr.AbstractGridVSR;
import io.github.ericmedvet.mrsim2d.core.agents.gridvsr.GridBody;
import io.github.ericmedvet.mrsim2d.core.engine.Engine;
import io.github.ericmedvet.mrsim2d.core.tasks.AgentsObservation;
import io.github.ericmedvet.mrsim2d.core.tasks.AgentsOutcome;
import io.github.ericmedvet.mrsim2d.core.tasks.Task;
import io.github.ericmedvet.mrsim2d.viewer.Drawer;
import io.github.ericmedvet.mrsim2d.viewer.TaskVideoBuilder;
import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.IntStream;

@Discoverable(prefixTemplate = "sim|s.function|f")
public class Functions {

  private Functions() {
  }

  @SuppressWarnings("unused")
  @Cacheable
  public static <X> FormattedNamedFunction<X, List<NumericalDynamicalSystem<?>>> numBrains(
      @Param(value = "of", dNPM = "f.identity()") Function<X, NumMultiBrained> beforeF,
      @Param(value = "name", dS = "n.brains") String name,
      @Param(value = "format", dS = "%s") String format
  ) {
    Function<NumMultiBrained, List<NumericalDynamicalSystem<?>>> f = NumMultiBrained::brains;
    return FormattedNamedFunction.from(f, format, name).compose(beforeF);
  }

  @SuppressWarnings("unused")
  @Cacheable
  public static <X, A, S extends AgentsObservation, O extends AgentsOutcome<S>> Function<X, O> taskRun(
      @Param(value = "of", dNPM = "f.identity()") Function<X, A> beforeF,
      @Param(value = "name", iS = "run[{task.name}]") String name,
      @Param("task") Task<A, S, O> task,
      @Param("duration") double duration,
      @Param(value = "engine", dNPM = "sim.engine()") Supplier<Engine> engineSupplier
  ) {
    Function<A, O> f = a -> task.run(a, duration, engineSupplier.get());
    return NamedFunction.from(f, name).compose(beforeF);
  }

  @SuppressWarnings("unused")
  @Cacheable
  public static <X, A, S extends AgentsObservation, O extends AgentsOutcome<S>> NamedFunction<X, List<O>> taskRuns(
      @Param(value = "of", dNPM = "f.identity()") Function<X, A> beforeF,
      @Param(value = "name", iS = "run[{task.name};x{repetitions}]") String name,
      @Param("task") Task<A, S, O> task,
      @Param("repetitions") int repetitions,
      @Param("duration") double duration,
      @Param(value = "engine", dNPM = "sim.engine()") Supplier<Engine> engineSupplier
  ) {
    Function<A, List<O>> f = a -> IntStream.range(0, repetitions)
        .boxed()
        .map(i -> task.run(a, duration, engineSupplier.get()))
        .toList();
    return NamedFunction.from(f, "name").compose(beforeF);
  }

  @SuppressWarnings("unused")
  @Cacheable
  public static <X, A> NamedFunction<X, Video> taskVideo(
      @Param(value = "of", dNPM = "f.identity()") Function<X, A> beforeF,
      @Param(value = "name", iS = "video[{title}]") String name,
      @Param("task") Task<A, ?, ?> task,
      @Param(value = "title", iS = "{task.name}") String title,
      @Param(value = "drawer", dNPM = "sim.drawer()") Function<String, Drawer> drawerBuilder,
      @Param(value = "engine", dNPM = "sim.engine()") Supplier<Engine> engineSupplier,
      @Param(value = "startTime", dD = 0) double startTime,
      @Param(value = "endTime", dD = 10) double endTime,
      @Param(value = "frameRate", dD = 30) double frameRate
  ) {
    Function<A, Video> f = new TaskVideoBuilder<>(
        task,
        drawerBuilder,
        engineSupplier,
        title,
        startTime,
        endTime,
        frameRate
    );
    return NamedFunction.from(f, name).compose(beforeF);
  }

  @SuppressWarnings("unused")
  @Cacheable
  public static <X> FormattedNamedFunction<X, Grid<GridBody.VoxelType>> vsrBody(
      @Param(value = "of", dNPM = "f.identity()") Function<X, AbstractGridVSR> beforeF,
      @Param(value = "name", dS = "body") String name,
      @Param(value = "nullify", dB = true) boolean nullifyNone,
      @Param(value = "format", dS = "%s") String format
  ) {
    Function<AbstractGridVSR, Grid<GridBody.VoxelType>> f = vsr -> vsr.getElementGrid()
        .map(GridBody.Element::type)
        .map(t -> nullifyNone ? (t.equals(GridBody.VoxelType.NONE) ? null : t) : t);
    return FormattedNamedFunction.from(f, format, name).compose(beforeF);
  }

}
