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

package it.units.erallab.mrsim.util.builder;

import java.util.List;
import java.util.Set;

public interface ParamMap {

  Set<String> names();

  Boolean b(String n);

  default boolean b(String n, boolean defaultValue) {
    return b(n) == null ? defaultValue : b(n);
  }

  List<Boolean> bs(String n);

  default List<Boolean> bs(String n, List<Boolean> defaultValue) {
    return bs(n) == null ? defaultValue : bs(n);
  }

  Double d(String n);

  default double d(String n, double defaultValue) {
    return d(n) == null ? defaultValue : d(n);
  }

  List<Double> ds(String n);

  default List<Double> ds(String n, List<Double> defaultValue) {
    return ds(n) == null ? defaultValue : ds(n);
  }

  Integer i(String n);

  default int i(String n, int defaultValue) {
    return i(n) == null ? defaultValue : i(n);
  }

  List<Integer> is(String n);

  default List<Integer> is(String n, List<Integer> defaultValue) {
    return is(n) == null ? defaultValue : is(n);
  }

  NamedParamMap npm(String n);

  default NamedParamMap npm(String n, NamedParamMap defaultValue) {
    return npm(n) == null ? defaultValue : npm(n);
  }

  List<NamedParamMap> npms(String n);

  default List<NamedParamMap> npms(String n, List<NamedParamMap> defaultValue) {
    return npms(n) == null ? defaultValue : npms(n);
  }

  String s(String n);

  default String s(String n, String defaultValue) {
    return s(n) == null ? defaultValue : s(n);
  }

  List<String> ss(String n);

  default List<String> ss(String n, List<String> defaultValue) {
    return ss(n) == null ? defaultValue : ss(n);
  }

  default String fs(String n, String regex) {
    String s = s(n);
    if (s == null || !s.matches(regex)) {
      return null;
    }
    return s;
  }

  default List<String> fss(String n, String regex) {
    List<String> ss = ss(n);
    if (ss == null || ss.stream().filter(s -> s.matches(regex)).count() != ss.size()) {
      return null;
    }
    return ss;
  }

}
