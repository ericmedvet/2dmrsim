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

package it.units.erallab.mrsim2d.builder;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author "Eric Medvet" on 2022/08/12 for 2dmrsim
 */

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.PARAMETER)
public @interface Param {

  enum Injection {NONE, MAP, BUILDER}

  boolean dB() default false;

  boolean[] dBs() default {};

  double dD() default Double.NaN;

  double[] dDs() default {};

  int dI() default Integer.MIN_VALUE;

  int[] dIs() default {};

  String dNPM() default "";

  String[] dNPMs() default {};

  String dS() default "";

  String[] dSs() default {};

  Injection injection() default Injection.NONE;

  String value();
}
