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

package it.units.erallab.mrsim.util;

import it.units.erallab.mrsim.util.builder.ParsableNamedParamMap;

import java.util.*;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author "Eric Medvet" on 2022/07/09 for 2dmrsim
 */
public class StringUtils {

  private StringUtils() {
  }

  public record TypedParams(
      Map<String, Integer> i,
      Map<String, Double> d,
      Map<String, Number> n,
      Map<String, Boolean> b,
      Map<String, String> s
  ) {
    public TypedParams() {
      this(
          new LinkedHashMap<>(),
          new LinkedHashMap<>(),
          new LinkedHashMap<>(),
          new LinkedHashMap<>(),
          new LinkedHashMap<>()
      );
    }
  }

  public static <T> Function<String, Optional<T>> formattedProvider(Map<String, Function<TypedParams, T>> providers) {
    return string -> {
      for (Map.Entry<String, Function<TypedParams, T>> providerEntry : providers.entrySet()) {
        //check match
        Map<String, String> params = params(providerEntry.getKey(), string);
        if (params == null) {
          continue;
        }
        //prepare typed params
        TypedParams typedParams = new TypedParams();
        typedParams.s.putAll(params);
        for (Map.Entry<String, String> paramEntry : params.entrySet()) {
          try {
            int i = Integer.parseInt(paramEntry.getValue());
            typedParams.i.put(paramEntry.getKey(), i);
            typedParams.n.put(paramEntry.getKey(), i);
          } catch (NumberFormatException ignored) {
          }
          try {
            double d = Double.parseDouble(paramEntry.getValue());
            typedParams.d.put(paramEntry.getKey(), d);
            typedParams.n.put(paramEntry.getKey(), d);
          } catch (NumberFormatException ignored) {
          }
          boolean b = Boolean.parseBoolean(paramEntry.getValue());
          typedParams.b.put(paramEntry.getKey(), b);
        }
        //invoke function
        T result = providerEntry.getValue().apply(typedParams);
        return result == null ? Optional.empty() : Optional.of(result);
      }
      return Optional.empty();
    };
  }

  public static <T> Function<String, Optional<T>> namedParamMapBuilder(Map<String, Function<ParsableNamedParamMap, T>> builders) {
    return s -> {
      try {
        ParsableNamedParamMap params = ParsableNamedParamMap.parse(s);
        try {
          T t = builders.getOrDefault(params.getName(), p -> null).apply(params);
          return t == null ? Optional.empty() : Optional.of(t);
        } catch (RuntimeException e) {
          return Optional.empty();
        }
      } catch (IllegalArgumentException p) {
        return Optional.empty();
      }
    };
  }

  public static String param(String pattern, String string, String paramName) {
    Matcher matcher = Pattern.compile(pattern).matcher(string);
    if (matcher.matches()) {
      return matcher.group(paramName);
    }
    throw new IllegalStateException(String.format(
        "Param %s not found in %s with pattern %s",
        paramName,
        string,
        pattern
    ));
  }

  public static Map<String, String> params(String pattern, String string) {
    if (!string.matches(pattern)) {
      return null;
    }
    Matcher m = Pattern.compile("\\(\\?<([a-zA-Z][a-zA-Z0-9]*)>").matcher(pattern);
    List<String> groupNames = new ArrayList<>();
    while (m.find()) {
      groupNames.add(m.group(1));
    }
    Map<String, String> params = new LinkedHashMap<>();
    for (String groupName : groupNames) {
      String value = param(pattern, string, groupName);
      if (value != null) {
        params.put(groupName, value);
      }
    }
    return params;
  }

}
