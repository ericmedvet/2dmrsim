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

import java.lang.reflect.*;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 * @author "Eric Medvet" on 2022/08/12 for 2dmrsim
 */
public record AutoBuiltDocumentedBuilder<T>(
    String name,
    String builtType,
    List<ParamInfo> params,
    Builder<T> builder
) implements DocumentedBuilder<T> {
  @Override
  public T build(ParamMap map, NamedBuilder<?> namedBuilder) throws BuilderException {
    return builder.build(map, namedBuilder);
  }

  @Override
  public String toString() {
    return "(" + params.stream().map(ParamInfo::toString).collect(Collectors.joining("; ")) + ") -> " + builtType;
  }

  public static AutoBuiltDocumentedBuilder<Object> from(Executable executable) {
    Logger l = Logger.getLogger(DocumentedBuilder.class.getName());
    //check annotation
    BuilderMethod builderMethodAnnotation = executable.getAnnotation(BuilderMethod.class);
    //check public and static or constructor
    if (!Modifier.isPublic(executable.getModifiers())) {
      return null;
    }
    if (!Modifier.isStatic(executable.getModifiers()) && executable instanceof Method) {
      return null;
    }
    //check if has NamedBuilder parameter at the beginning
    boolean hasNamedBuilder = executable.getParameters().length > 0 &&
        executable.getParameters()[0].getType().equals(NamedBuilder.class);
    //find parameters
    List<ParamInfo> paramInfos = Arrays.stream(executable.getParameters())
        .map(AutoBuiltDocumentedBuilder::from)
        .filter(Objects::nonNull)
        .toList();
    if (paramInfos.size() != executable.getParameters().length - (hasNamedBuilder ? 1 : 0)) {
      l.warning(String.format(
          "Cannot process method %s(): %d on %d params are not valid",
          executable.getName(),
          executable.getParameters().length - (hasNamedBuilder ? 1 : 0) - paramInfos.size(),
          executable.getParameters().length - (hasNamedBuilder ? 1 : 0)
      ));
      return null;
    }
    //wrap and return
    String name;
    String buildType;
    if (executable instanceof Method method) {
      buildType = method.getReturnType().getName(); //TODO can be improved
      name = method.getName();
    } else {
      buildType = ((Constructor<?>) executable).getDeclaringClass().getName();
      name = toLowerCamelCase(((Constructor<?>) executable).getDeclaringClass().getSimpleName());
    }
    if (builderMethodAnnotation != null && !builderMethodAnnotation.value().isEmpty()) {
      name = builderMethodAnnotation.value();
    }
    return new AutoBuiltDocumentedBuilder<>(
        name,
        buildType,
        paramInfos,
        (ParamMap map, NamedBuilder<?> namedBuilder) -> {
          Object[] params = new Object[paramInfos.size() + (hasNamedBuilder ? 1 : 0)];
          if (hasNamedBuilder) {
            params[0] = namedBuilder;
          }
          for (int j = 0; j < paramInfos.size(); j++) {
            int k = j + (hasNamedBuilder ? 1 : 0);
            if (paramInfos.get(j).self()) {
              params[k] = map;
            } else {
              //noinspection unchecked
              params[k] = buildParam(
                  paramInfos.get(j),
                  map,
                  executable.getParameters()[k],
                  (NamedBuilder<Object>) namedBuilder
              );
            }
          }
          try {
            if (executable instanceof Method method) {
              return method.invoke(null, params);
            }
            return ((Constructor<?>) executable).newInstance(params);
          } catch (IllegalAccessException | InvocationTargetException | InstantiationException e) {
            throw new BuilderException(String.format("Cannot invoke %s: %s", executable.getName(), e), e);
          }
        }
    );
  }

  @SuppressWarnings("unchecked")
  private static Object buildParam(ParamInfo pi, ParamMap m, Parameter ap, NamedBuilder<Object> nb) {
    return switch (pi.type()) {
      case INT -> pi.defaultValue() == null ? m.i(pi.name()) : m.i(pi.name(), (Integer) pi.defaultValue());
      case DOUBLE -> pi.defaultValue() == null ? m.d(pi.name()) : m.d(pi.name(), (Double) pi.defaultValue());
      case STRING -> pi.defaultValue() == null ? m.s(pi.name()) : m.s(pi.name(), (String) pi.defaultValue());
      case BOOLEAN -> pi.defaultValue() == null ? m.b(pi.name()) : m.b(pi.name(), (Boolean) pi.defaultValue());
      case NAMED_PARAM_MAP -> processNPM(pi.defaultValue() == null ? m.npm(pi.name()) : m.npm(
          pi.name(),
          (NamedParamMap) pi.defaultValue()
      ), ap, nb);
      case INTS -> pi.defaultValue() == null ? m.is(pi.name()) : m.is(pi.name(), (List<Integer>) pi.defaultValue());
      case DOUBLES -> pi.defaultValue() == null ? m.ds(pi.name()) : m.ds(pi.name(), (List<Double>) pi.defaultValue());
      case STRINGS -> pi.defaultValue() == null ? m.ss(pi.name()) : m.ss(pi.name(), (List<String>) pi.defaultValue());
      case BOOLEANS -> pi.defaultValue() == null ? m.bs(pi.name()) : m.bs(pi.name(), (List<Boolean>) pi.defaultValue());
      case NAMED_PARAM_MAPS -> pi.defaultValue() == null ? m.npms(pi.name()) : m.npms(
              pi.name(),
              (List<NamedParamMap>) pi.defaultValue()
          )
          .stream()
          .map(npm -> processNPM(npm, ap, nb))
          .toList();
    };
  }

  private static Object processNPM(NamedParamMap npm, Parameter actualParameter, NamedBuilder<Object> nb) {
    if (actualParameter.getType().equals(NamedParamMap.class)) {
      return npm;
    }
    return nb.build(npm);
  }

  private static ParamInfo from(Parameter parameter) {
    Param paramAnnotation = parameter.getAnnotation(Param.class);
    if (paramAnnotation == null) {
      return null;
    }
    if (paramAnnotation.self() && !parameter.getType().equals(ParamMap.class)) {
      return null;
    }
    String name = paramAnnotation.value();
    if (parameter.getType().equals(Integer.class) || parameter.getType().equals(Integer.TYPE)) {
      return new ParamInfo(Type.INT, name, buildDefaultValue(Type.INT, paramAnnotation), paramAnnotation.self());
    }
    if (parameter.getType().equals(Double.class) || parameter.getType().equals(Double.TYPE)) {
      return new ParamInfo(Type.DOUBLE, name, buildDefaultValue(Type.DOUBLE, paramAnnotation), paramAnnotation.self());
    }
    if (parameter.getType().equals(String.class)) {
      return new ParamInfo(Type.STRING, name, buildDefaultValue(Type.STRING, paramAnnotation), paramAnnotation.self());
    }
    if (parameter.getType().equals(Boolean.class) || parameter.getType().equals(Boolean.TYPE)) {
      return new ParamInfo(
          Type.BOOLEAN,
          name,
          buildDefaultValue(Type.BOOLEAN, paramAnnotation),
          paramAnnotation.self()
      );
    }
    if (parameter.getType()
        .equals(List.class) && parameter.getParameterizedType() instanceof ParameterizedType parameterizedType) {
      if (parameterizedType.getActualTypeArguments()[0].equals(Integer.class)) {
        return new ParamInfo(Type.INTS, name, buildDefaultValue(Type.INTS, paramAnnotation), paramAnnotation.self());
      }
    }
    if (parameter.getType()
        .equals(List.class) && parameter.getParameterizedType() instanceof ParameterizedType parameterizedType) {
      if (parameterizedType.getActualTypeArguments()[0].equals(Double.class)) {
        return new ParamInfo(
            Type.DOUBLES,
            name,
            buildDefaultValue(Type.DOUBLES, paramAnnotation),
            paramAnnotation.self()
        );
      }
    }
    if (parameter.getType()
        .equals(List.class) && parameter.getParameterizedType() instanceof ParameterizedType parameterizedType) {
      if (parameterizedType.getActualTypeArguments()[0].equals(String.class)) {
        return new ParamInfo(
            Type.STRINGS,
            name,
            buildDefaultValue(Type.STRINGS, paramAnnotation),
            paramAnnotation.self()
        );
      }
    }
    if (parameter.getType()
        .equals(List.class) && parameter.getParameterizedType() instanceof ParameterizedType parameterizedType) {
      if (parameterizedType.getActualTypeArguments()[0].equals(NamedParamMap.class)) {
        return new ParamInfo(
            Type.NAMED_PARAM_MAPS,
            name,
            buildDefaultValue(Type.NAMED_PARAM_MAPS, paramAnnotation),
            paramAnnotation.self()
        );
      }
    }
    if (parameter.getType()
        .equals(List.class) && parameter.getParameterizedType() instanceof ParameterizedType) {
      return new ParamInfo(
          Type.NAMED_PARAM_MAPS,
          name,
          buildDefaultValue(Type.NAMED_PARAM_MAPS, paramAnnotation),
          paramAnnotation.self()
      );
    }
    return new ParamInfo(
        Type.NAMED_PARAM_MAP,
        name,
        buildDefaultValue(Type.NAMED_PARAM_MAP, paramAnnotation),
        paramAnnotation.self()
    );
  }

  private static Object buildDefaultValue(Type type, Param pa) {
    if (type.equals(Type.INT) && pa.dI() != Integer.MIN_VALUE) {
      return pa.dI();
    }
    if (type.equals(Type.DOUBLE) && !Double.isNaN(pa.dD())) {
      return pa.dD();
    }
    if (type.equals(Type.BOOLEAN) && pa.dB()) {
      return true;
    }
    if (type.equals(Type.STRING) && !pa.dS().isEmpty()) {
      return pa.dS();
    }
    if (type.equals(Type.NAMED_PARAM_MAP) && !pa.dNPM().isEmpty()) {
      return StringNamedParamMap.parse(pa.dNPM());
    }
    if (type.equals(Type.INTS)) {
      return Arrays.stream(pa.dIs()).boxed().toList();
    }
    if (type.equals(Type.DOUBLES)) {
      return Arrays.stream(pa.dDs()).boxed().toList();
    }
    if (type.equals(Type.BOOLEANS)) {
      return List.of(pa.dBs());
    }
    if (type.equals(Type.STRINGS)) {
      return List.of(pa.dSs());
    }
    if (type.equals(Type.NAMED_PARAM_MAPS)) {
      return Arrays.stream(pa.dNPMs()).map(StringNamedParamMap::parse).toList();
    }
    return null;
  }

  private static String toLowerCamelCase(String s) {
    return s.substring(0, 1).toLowerCase() + s.substring(1);
  }

}
