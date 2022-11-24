/*
 * Copyright 2022 eric
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


import it.units.erallab.mrsim2d.core.Sensor;
import it.units.erallab.mrsim2d.core.actions.*;
import it.units.erallab.mrsim2d.core.bodies.Body;
import it.units.erallab.mrsim2d.core.bodies.SoftBody;
import it.units.erallab.mrsim2d.core.bodies.Voxel;
import it.units.malelab.jnb.core.Param;

/**
 * @author "Eric Medvet" on 2022/08/11 for 2dmrsim
 */
public class SensorBuilder {

  public SensorBuilder() {
  }

  public static Sensor<Body> a() {
    return SenseAngle::new;
  }

  public static Sensor<SoftBody> ar() {
    return SenseAreaRatio::new;
  }

  public static Sensor<Body> c() {
    return SenseContact::new;
  }

  public static Sensor<Body> d(
      @Param(value = "a", dD = 0) Double a,
      @Param(value = "r", dD = 1) Double r
  ) {
    return b -> new SenseDistanceToBody(Math.toRadians(a), r, b);
  }

  public static Sensor<Body> rv(@Param(value = "a", dD = 0) Double a) {
    return b -> new SenseRotatedVelocity(Math.toRadians(a), b);
  }

  public static Sensor<Voxel> sa(@Param("s") Voxel.Side s) {
    return v -> new SenseSideAttachment(s, v);
  }

  public static Sensor<Voxel> sc(@Param("s") Voxel.Side s) {
    return v -> new SenseSideCompression(s, v);
  }

  public static Sensor<Body> sin(
      @Param(value = "f", dD = 1) Double f,
      @Param(value = "p", dD = 0) Double p
  ) {
    return b -> new SenseSinusoidal(f, p, b);
  }

}
