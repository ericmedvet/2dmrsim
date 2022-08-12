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

package it.units.erallab.mrsim.builders;

import it.units.erallab.mrsim.core.actions.*;
import it.units.erallab.mrsim.core.bodies.Voxel;
import it.units.erallab.mrsim.util.builder.NamedBuilder;
import it.units.erallab.mrsim.util.builder.ParamMap;

import java.util.function.Function;

/**
 * @author "Eric Medvet" on 2022/08/11 for 2dmrsim
 */
public class VoxelSensorBuilder extends NamedBuilder<Function<Voxel, Sense<? super Voxel>>> {

  private VoxelSensorBuilder() {
    register("rv", VoxelSensorBuilder::createRotatedVelocity);
    register("d", VoxelSensorBuilder::createDistance);
    register("a", VoxelSensorBuilder::createAngle);
    register("ar", VoxelSensorBuilder::createAreaRatio);
    register("c", VoxelSensorBuilder::createContact);
    register("sa", VoxelSensorBuilder::createSideAttachment);
    register("sc", VoxelSensorBuilder::createSideCompression);
  }

  private static Function<Voxel, Sense<? super Voxel>> createRotatedVelocity(ParamMap m, NamedBuilder<?> nb) {
    return v -> new SenseRotatedVelocity(m.d("a", 0) / 180d * Math.PI, v);
  }

  private static Function<Voxel, Sense<? super Voxel>> createDistance(ParamMap m, NamedBuilder<?> nb) {
    return v -> new SenseDistanceToBody(m.d("a", 0) / 180d * Math.PI, m.d("r", 0), v);
  }

  private static Function<Voxel, Sense<? super Voxel>> createAreaRatio(ParamMap m, NamedBuilder<?> nb) {
    return SenseAreaRatio::new;
  }

  private static Function<Voxel, Sense<? super Voxel>> createAngle(ParamMap m, NamedBuilder<?> nb) {
    return SenseAngle::new;
  }

  private static Function<Voxel, Sense<? super Voxel>> createSideCompression(ParamMap m, NamedBuilder<?> nb) {
    return v -> new SenseSideCompression(Voxel.Side.valueOf(m.fs("s", "[nesw]").toUpperCase()), v);
  }

  private static Function<Voxel, Sense<? super Voxel>> createContact(ParamMap m, NamedBuilder<?> nb) {
    return SenseContact::new;
  }

  private static Function<Voxel, Sense<? super Voxel>> createSideAttachment(ParamMap m, NamedBuilder<?> nb) {
    return v -> new SenseSideAttachment(Voxel.Side.valueOf(m.fs("s", "[nesw]").toUpperCase()), v);
  }

  private static Function<Voxel, Sense<? super Voxel>> createSinusoidal(ParamMap m, NamedBuilder<?> nb) {
    return v -> new SenseSinusoidal(m.d("f", 1), m.d("p", 0), v);
  }


  private final static VoxelSensorBuilder INSTANCE = new VoxelSensorBuilder();

  public static VoxelSensorBuilder getInstance() {
    return INSTANCE;
  }

}
