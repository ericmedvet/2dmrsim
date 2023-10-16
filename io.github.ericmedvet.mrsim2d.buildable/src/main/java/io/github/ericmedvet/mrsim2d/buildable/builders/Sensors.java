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

package io.github.ericmedvet.mrsim2d.buildable.builders;

import io.github.ericmedvet.jnb.core.Param;
import io.github.ericmedvet.mrsim2d.core.Sensor;
import io.github.ericmedvet.mrsim2d.core.actions.*;
import io.github.ericmedvet.mrsim2d.core.bodies.Body;
import io.github.ericmedvet.mrsim2d.core.bodies.RotationalJoint;
import io.github.ericmedvet.mrsim2d.core.bodies.Voxel;

public class Sensors {

  public Sensors() {}

  @SuppressWarnings("unused")
  public static Sensor<Body> a() {
    return SenseAngle::new;
  }

  @SuppressWarnings("unused")
  public static Sensor<Body> ar() {
    return SenseAreaRatio::new;
  }

  @SuppressWarnings("unused")
  public static Sensor<Body> c() {
    return SenseContact::new;
  }

  @SuppressWarnings("unused")
  public static Sensor<Body> d(
      @Param(value = "a", dD = 0) Double a, @Param(value = "r", dD = 1) Double r) {
    return b -> new SenseDistanceToBody(Math.toRadians(a), r, b);
  }

  @SuppressWarnings("unused")
  public static Sensor<RotationalJoint> ja() {
    return SenseJointAngle::new;
  }

  @SuppressWarnings("unused")
  public static Sensor<Body> rv(@Param(value = "a", dD = 0) Double a) {
    return b -> new SenseRotatedVelocity(Math.toRadians(a), b);
  }

  @SuppressWarnings("unused")
  public static Sensor<Voxel> sa(@Param("s") Voxel.Side s) {
    return v -> new SenseSideAttachment(s, v);
  }

  @SuppressWarnings("unused")
  public static Sensor<Voxel> sc(@Param("s") Voxel.Side s) {
    return v -> new SenseSideCompression(s, v);
  }

  @SuppressWarnings("unused")
  public static Sensor<Body> sin(
      @Param(value = "f", dD = 1) Double f, @Param(value = "p", dD = 0) Double p) {
    return b -> new SenseSinusoidal(f, p, b);
  }
}
