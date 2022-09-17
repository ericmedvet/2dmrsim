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

package it.units.erallab.mrsim2d.core.builders;


import it.units.erallab.mrsim2d.builder.Param;
import it.units.erallab.mrsim2d.core.actions.*;
import it.units.erallab.mrsim2d.core.bodies.Voxel;

import java.util.function.Function;

/**
 * @author "Eric Medvet" on 2022/08/11 for 2dmrsim
 */
public class VoxelSensorBuilder {

  public static Function<Voxel, Sense<? super Voxel>> a() {
    return SenseAngle::new;
  }

  public static Function<Voxel, Sense<? super Voxel>> ar() {
    return SenseAreaRatio::new;
  }

  public static Function<Voxel, Sense<? super Voxel>> c() {
    return SenseContact::new;
  }

  public static Function<Voxel, Sense<? super Voxel>> d(
      @Param(value = "a", dD = 0) Double a,
      @Param(value = "r", dD = 1) Double r
  ) {
    return v -> new SenseDistanceToBody(Math.toRadians(a), r, v);
  }

  public static Function<Voxel, Sense<? super Voxel>> rv(@Param(value = "a", dD = 0) Double a) {
    return v -> new SenseRotatedVelocity(Math.toRadians(a), v);
  }

  public static Function<Voxel, Sense<? super Voxel>> sa(@Param("s") String s) {
    return v -> new SenseSideAttachment(Voxel.Side.valueOf(s.toUpperCase()), v);
  }

  public static Function<Voxel, Sense<? super Voxel>> sc(@Param("s") String s) {
    return v -> new SenseSideCompression(Voxel.Side.valueOf(s.toUpperCase()), v);
  }

  public static Function<Voxel, Sense<? super Voxel>> sin(
      @Param(value = "f", dD = 1) Double f,
      @Param(value = "p", dD = 0) Double p
  ) {
    return v -> new SenseSinusoidal(f, p, v);
  }

}
