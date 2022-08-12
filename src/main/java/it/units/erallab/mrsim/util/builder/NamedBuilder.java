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

import java.lang.reflect.Constructor;
import java.util.*;
import java.util.function.Supplier;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public class NamedBuilder<X> {

  private final static NamedBuilder<Object> EMPTY = new NamedBuilder<>(Map.of());

  protected final static char NAME_SEPARATOR = '.';
  private final static Logger L = Logger.getLogger(NamedBuilder.class.getName());

  private final Map<String, Builder<? extends X>> builders;

  private NamedBuilder(Map<String, Builder<? extends X>> builders) {
    this.builders = new TreeMap<>(builders);
  }

  @SuppressWarnings("unchecked")
  public <T extends X> Optional<T> build(NamedParamMap map, Supplier<T> defaultSupplier) {
    if (!builders.containsKey(map.getName())) {
      T t = defaultSupplier.get();
      return t == null ? Optional.empty() : Optional.of(t);
    }
    try {
      T t = (T) builders.get(map.getName()).build(map, this);
      return t == null ? Optional.empty() : Optional.of(t);
    } catch (IllegalArgumentException e) {
      L.warning(String.format("Cannot use builder for %s: %s", map.getName(), e));
      e.printStackTrace();
      T t = defaultSupplier.get();
      return t == null ? Optional.empty() : Optional.of(t);
    }
  }

  public <T extends X> Optional<T> build(String mapString, Supplier<T> defaultSupplier) {
    return build(StringNamedParamMap.parse(mapString), defaultSupplier);
  }

  public Optional<X> build(NamedParamMap map) {
    return build(map, () -> null);
  }

  public Optional<X> build(String mapString) {
    return build(StringNamedParamMap.parse(mapString));
  }

  public NamedBuilder<X> register(String name, Builder<? extends X> builder) {
    builders.put(name, builder);
    return this;
  }

  public NamedBuilder<X> and(String prefix, NamedBuilder<? extends X> namedBuilder) {
    return and(List.of(prefix), namedBuilder);
  }

  public NamedBuilder<X> and(List<String> prefixes, NamedBuilder<? extends X> namedBuilder) {
    Map<String, Builder<? extends X>> allBuilders = new HashMap<>(builders);
    prefixes.forEach(
        prefix -> namedBuilder.builders
            .forEach((k, v) -> allBuilders.put(prefix.isEmpty() ? k : (prefix + NAME_SEPARATOR + k), v))
    );
    return new NamedBuilder<>(allBuilders);
  }

  public NamedBuilder<X> and(NamedBuilder<? extends X> namedBuilder) {
    return and("", namedBuilder);
  }

  public static NamedBuilder<Object> empty() {
    return EMPTY;
  }

  public static NamedBuilder<Object> fromUtilityClass(Class<?> c) {
    return new NamedBuilder<>(Arrays.stream(c.getMethods())
        .map(AutoBuiltDocumentedBuilder::from)
        .filter(Objects::nonNull)
        .collect(Collectors.toMap(DocumentedBuilder::name, b -> b)));
  }

  @SuppressWarnings("unchecked")
  public static <C> NamedBuilder<C> fromClass(Class<? extends C> c) {
    List<Constructor<?>> constructors = Arrays.stream(c.getConstructors()).toList();
    if (constructors.size() > 1) {
      constructors = constructors.stream()
          .filter(constructor -> constructor.getAnnotation(BuilderMethod.class) != null)
          .toList();
    }
    if (constructors.size() != 1) {
      throw new IllegalArgumentException(String.format(
          "Cannot build named builder from class %s that has %d!=1 constructors",
          c.getSimpleName(),
          c.getConstructors().length
      ));
    }
    DocumentedBuilder<C> builder = (DocumentedBuilder<C>) AutoBuiltDocumentedBuilder.from(constructors.get(0));
    if (builder != null) {
      return new NamedBuilder<>(Map.of(
          builder.name(), builder
      ));
    } else {
      return (NamedBuilder<C>) empty();
    }
  }

  @Override
  public String toString() {
    return prettyToString(false);
  }

  public String prettyToString(boolean indent) {
    StringBuilder sb = new StringBuilder();
    sb.append("NamedBuilder{");
    sb.append(indent ? "\n" : "");
    builders.forEach((k, v) -> {
      sb.append(indent ? "\t" : "");
      sb.append(k);
      if (v instanceof DocumentedBuilder<?> db) {
        sb.append(db);
      }
      sb.append(indent ? "\n" : "");
    });
    sb.append("}");
    return sb.toString();
  }
}
