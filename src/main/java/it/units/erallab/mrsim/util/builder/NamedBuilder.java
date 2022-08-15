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
import java.util.stream.Collectors;

public class NamedBuilder<X> {

  private final static NamedBuilder<Object> EMPTY = new NamedBuilder<>(Map.of());

  protected final static char NAME_SEPARATOR = '.';

  private final Map<String, Builder<? extends X>> builders;

  private NamedBuilder(Map<String, Builder<? extends X>> builders) {
    this.builders = new TreeMap<>(builders);
  }

  @SuppressWarnings("unchecked")
  public <T extends X> T build(NamedParamMap map, Supplier<T> defaultSupplier) throws BuilderException {
    if (!builders.containsKey(map.getName())) {
      if (defaultSupplier != null) {
        return defaultSupplier.get();
      }
      throw new BuilderException(String.format(
          "No builder for %s: closest matches are %s",
          map.getName(),
          builders.keySet().stream()
              .sorted((Comparator.comparing(s -> distance(s, map.getName()))))
              .limit(3)
              .collect(Collectors.joining(", "))
      ));
    }
    try {
      return (T) builders.get(map.getName()).build(map, this);
    } catch (BuilderException e) {
      if (defaultSupplier != null) {
        return defaultSupplier.get();
      }
      throw new BuilderException(String.format("Cannot build %s: %s", map.getName(), e), e);
    }
  }

  public <T extends X> T build(String mapString, Supplier<T> defaultSupplier) throws BuilderException {
    return build(StringNamedParamMap.parse(mapString), defaultSupplier);
  }

  public X build(NamedParamMap map) throws BuilderException {
    return build(map, null);
  }

  public X build(String mapString) throws BuilderException {
    return build(StringNamedParamMap.parse(mapString));
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
    return prettyToString(this, false);
  }

  public static String prettyToString(NamedBuilder<?> namedBuilder, boolean newLine) {
    return namedBuilder.builders.entrySet().stream()
        .map(e -> {
          String s = e.getKey();
          if (e.getValue() instanceof DocumentedBuilder<?> db) {
            s = s + db;
          }
          return s;
        })
        .collect(Collectors.joining(newLine ? "\n" : "; "));
  }

  private double distance(String s1, String s2) {
    return distance(s1.chars().boxed().toList(), s2.chars().boxed().toList());
  }

  private <T> Double distance(List<T> ts1, List<T> ts2) {
    int len0 = ts1.size() + 1;
    int len1 = ts2.size() + 1;
    int[] cost = new int[len0];
    int[] newCost = new int[len0];
    for (int i = 0; i < len0; i++) {
      cost[i] = i;
    }
    for (int j = 1; j < len1; j++) {
      newCost[0] = j;
      for (int i = 1; i < len0; i++) {
        int match = ts1.get(i - 1).equals(ts2.get(j - 1)) ? 0 : 1;
        int cost_replace = cost[i - 1] + match;
        int cost_insert = cost[i] + 1;
        int cost_delete = newCost[i - 1] + 1;
        newCost[i] = Math.min(Math.min(cost_insert, cost_delete), cost_replace);
      }
      int[] swap = cost;
      cost = newCost;
      newCost = swap;
    }
    return (double) cost[len0 - 1];
  }
}
