/*-
 * ========================LICENSE_START=================================
 * mrsim2d-buildable
 * %%
 * Copyright (C) 2020 - 2024 Eric Medvet
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

import io.github.ericmedvet.jnb.core.Discoverable;
import io.github.ericmedvet.jnb.core.Param;
import io.github.ericmedvet.jnb.datastructure.Grid;
import io.github.ericmedvet.mrsim2d.core.agents.gridvsr.GridBody.VoxelType;
import io.github.ericmedvet.mrsim2d.core.geometry.Terrain;
import io.github.ericmedvet.mrsim2d.core.tasks.balancing.Balancing;
import io.github.ericmedvet.mrsim2d.core.tasks.jumping.Jumping;
import io.github.ericmedvet.mrsim2d.core.tasks.locomotion.Locomotion;
import io.github.ericmedvet.mrsim2d.core.tasks.locomotion.PrebuiltIndependentLocomotion;
import io.github.ericmedvet.mrsim2d.core.tasks.piling.FallPiling;
import io.github.ericmedvet.mrsim2d.core.tasks.piling.StandPiling;
import io.github.ericmedvet.mrsim2d.core.tasks.sumo.Sumo;
import io.github.ericmedvet.mrsim2d.core.tasks.trainingfight.TrainingFight;
import io.github.ericmedvet.mrsim2d.core.tasks.trainingsumo.TrainingSumo;
import java.util.random.RandomGenerator;

@Discoverable(prefixTemplate = "sim|s.task")
public class Tasks {
  private Tasks() {
  }

  @SuppressWarnings("unused")
  public static Balancing balancing(
      @Param(value = "name", dS = "balancing") String name,
      @Param(value = "duration", dD = 10) double duration,
      @Param(value = "swingLength", dD = 10.0) double swingLength,
      @Param(value = "swingDensity", dD = 0.1) double swingDensity,
      @Param(value = "supportHeight", dD = 1.0) double supportHeight,
      @Param(value = "initialXGap", dD = 0.0) double initialXGap,
      @Param(value = "initialYGap", dD = 0.1) double initialYGap
  ) {
    return new Balancing(duration, swingLength, swingDensity, supportHeight, initialXGap, initialYGap);
  }

  @SuppressWarnings("unused")
  public static FallPiling fallPiling(
      @Param(value = "name", dS = "fallPiling") String name,
      @Param(value = "duration", dD = 45d) double duration,
      @Param(value = "fallInterval", dD = 5d) double fallInterval,
      @Param("nOfAgents") int nOfAgents,
      @Param(value = "xSigmaRatio", dD = 0.1d) double xSigmaRatio,
      @Param(value = "randomGenerator", dNPM = "m.defaultRG()") RandomGenerator randomGenerator,
      @Param(value = "terrain", dNPM = "sim.terrain.flat()") Terrain terrain,
      @Param(value = "terrainAttachableDistance", dD = Double.POSITIVE_INFINITY) double terrainAttachableDistance,
      @Param(value = "yGapRatio", dD = 1d) double yGapRatio,
      @Param(value = "xGap", dD = 10d) double xGap
  ) {

    return new FallPiling(
        duration,
        fallInterval,
        nOfAgents,
        xSigmaRatio,
        randomGenerator,
        terrain,
        terrainAttachableDistance,
        yGapRatio,
        xGap
    );
  }

  @SuppressWarnings("unused")
  public static Jumping jumping(
      @Param(value = "name", dS = "jumping") String name,
      @Param(value = "duration", dD = 10) double duration,
      @Param(value = "initialYGap", dD = 0.1) double initialYGap
  ) {
    return new Jumping(duration, initialYGap);
  }

  @SuppressWarnings("unused")
  public static Locomotion locomotion(
      @Param(value = "name", iS = "locomotion[{terrain.name}]") String name,
      @Param(value = "duration", dD = 30) double duration,
      @Param(value = "terrain", dNPM = "sim.terrain.flat()") Terrain terrain,
      @Param(value = "terrainAttachableDistance", dD = Double.POSITIVE_INFINITY) double terrainAttachableDistance,
      @Param(value = "initialXGap", dD = 1) double initialXGap,
      @Param(value = "initialYGap", dD = 0.1) double initialYGap
  ) {
    return new Locomotion(duration, terrain, terrainAttachableDistance, initialXGap, initialYGap);
  }

  @SuppressWarnings("unused")
  public static PrebuiltIndependentLocomotion prebuiltIndependentLocomotion(
      @Param(value = "name", iS = "piLocomotion[{terrain.name}]") String name,
      @Param(value = "duration", dD = 30) double duration,
      @Param(value = "terrain", dNPM = "sim.terrain.flat()") Terrain terrain,
      @Param(value = "terrainAttachableDistance", dD = Double.POSITIVE_INFINITY) double terrainAttachableDistance,
      @Param(value = "initialXGap", dD = 1) double initialXGap,
      @Param(value = "initialYGap", dD = 0.1) double initialYGap,
      @Param(value = "shape") Grid<VoxelType> shape
  ) {
    return new PrebuiltIndependentLocomotion(
        duration,
        terrain,
        terrainAttachableDistance,
        initialXGap,
        initialYGap,
        shape
    );
  }

  @SuppressWarnings("unused")
  public static StandPiling standPiling(
      @Param(value = "name", dS = "standPiling") String name,
      @Param(value = "duration", dD = 45) double duration,
      @Param(value = "nOfAgents") int nOfAgents,
      @Param(value = "xGapRatio", dD = 1) double xGapRatio,
      @Param(value = "terrain", dNPM = "sim.terrain.flat()") Terrain terrain,
      @Param(value = "terrainAttachableDistance", dD = Double.POSITIVE_INFINITY) double terrainAttachableDistance,
      @Param(value = "firstXGap", dD = 10) double firstXGap,
      @Param(value = "initialYGap", dD = 0.1) double initialYGap
  ) {
    return new StandPiling(duration, nOfAgents, xGapRatio, terrain, terrainAttachableDistance, firstXGap, initialYGap);
  }

  @SuppressWarnings("unused")
  public static Sumo sumo(
      @Param(value = "name", dS = "sumo") String name,
      @Param(value = "duration", dD = 60) double duration
  ) {
    return new Sumo(duration);
  }

  @SuppressWarnings("unused")
  public static TrainingFight trainingFight(
      @Param(value = "name", dS = "trainingFight") String name,
      @Param(value = "duration", dD = 60) double duration,
      @Param(value = "terrain", dNPM = "sim.terrain.sumoArena()") Terrain terrain,
      @Param(value = "terrainAttachableDistance", dD = Double.POSITIVE_INFINITY) double terrainAttachableDistance,
      @Param(value = "initialYGap", dD = 0.25) double initialYGap
  ) {
    return new TrainingFight(duration, terrain, terrainAttachableDistance, initialYGap);
  }

  @SuppressWarnings("unused")
  public static TrainingSumo trainingSumo(
      @Param(value = "name", dS = "trainingSumo") String name,
      @Param(value = "duration", dD = 60) double duration,
      @Param(value = "terrain", dNPM = "sim.terrain.sumoArena()") Terrain terrain,
      @Param(value = "terrainAttachableDistance", dD = Double.POSITIVE_INFINITY) double terrainAttachableDistance,
      @Param(value = "initialYGap", dD = 0.25) double initialYGap
  ) {
    return new TrainingSumo(duration, terrain, terrainAttachableDistance, initialYGap);
  }
}
