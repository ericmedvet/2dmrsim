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

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;
import java.util.logging.Logger;

public class NamedBuilder {

  protected final static char NAME_SEPARATOR = '.';
  private final static Logger L = Logger.getLogger(NamedBuilder.class.getName());

  private final Map<String, Builder<?>> builders;

  public NamedBuilder() {
    this.builders = new LinkedHashMap<>();
  }

  @FunctionalInterface
  public interface Builder<T> {
    T build(ParamMap map, NamedBuilder namedBuilder) throws IllegalArgumentException;
  }

  @SuppressWarnings("unchecked")
  public <T> Optional<T> build(NamedParamMap map, Supplier<T> defaultSupplier) {
    if (!builders.containsKey(map.getName())) {
      T t = defaultSupplier.get();
      return t == null ? Optional.empty() : Optional.of(t);
    }
    try {
      T t = (T) builders.get(map.getName()).build(map, this);
      return t == null ? Optional.empty() : Optional.of(t);
    } catch (IllegalArgumentException e) {
      L.warning(String.format("Cannot use builder for %s: %s", map.getName(), e));
      T t = defaultSupplier.get();
      return t == null ? Optional.empty() : Optional.of(t);
    }
  }

  public <T> Optional<T> build(String mapString, Supplier<T> defaultSupplier) {
    return build(ParsableNamedParamMap.parse(mapString), defaultSupplier);
  }

  public Optional<Object> build(NamedParamMap map) {
    return build(map, Object::new);
  }

  public Optional<Object> build(String mapString) {
    return build(ParsableNamedParamMap.parse(mapString));
  }

  public void register(String name, Builder<?> builder) {
    builders.put(name, builder);
  }

  public void register(String prefix, NamedBuilder namedBuilder) {
    namedBuilder.builders.forEach((s, b) -> builders.put(prefix + NAME_SEPARATOR + s, b));
  }

}
