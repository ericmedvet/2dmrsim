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

import java.util.*;
import java.util.function.Supplier;

public class NamedBuilder {

  protected final static char NAME_SEPARATOR = '.';

  private final Map<String, Builder<?>> builders;

  public NamedBuilder() {
    this.builders = new LinkedHashMap<>();
  }

  @FunctionalInterface
  public interface Builder<T> {
    T build(ParamMap map, NamedBuilder namedBuilder) throws IllegalArgumentException;
  }

  public static void main(String[] args) {

    //System.out.println(ParsableNamedParamMap.parse("a(value=ciao;ns=[1;2;3.4])"));
    //System.out.println(ParsableNamedParamMap.parse("a(value=ciao;ns=[1;2;3.4];b=33)"));
    //System.out.println(ParsableNamedParamMap.parse("a(value=ciao;ns=[1;2;3.4];b=f(ss=1))"));
    //System.out.println(ParsableNamedParamMap.parse("a(value=ciao;ns=[1;2;3.4];b=f(ss=[a;b]))"));
    //System.out.println(ParsableNamedParamMap.parse("a(value=ciao;ns=[1;2;3.4];b=f(ss=[a;b];es=[j(a=1);k(b=2)]))"));
    System.out.println(ParsableNamedParamMap.parse("exp(runs=(seed=[1;2;3])*(mapper=[a;b])*[run(ea=numga)])"));
    System.exit(0);

    NamedBuilder stringNB = new NamedBuilder();
    stringNB.register("one", (m, nb) -> m.s("value"));
    stringNB.register("list", (m, nb) -> Collections.nCopies(m.i("n"), m.s("value")));

    NamedBuilder numNB = new NamedBuilder();
    numNB.register("one", (m, nb) -> m.d("value"));

    NamedBuilder allNb = new NamedBuilder();
    allNb.register("strings", stringNB);
    allNb.register("nums", numNB);

    System.out.println(allNb.build(ParsableNamedParamMap.parse("strings.one(value=ciao)"), () -> ""));
    List<String> ss = allNb.build(ParsableNamedParamMap.parse("strings.list(n=3;value=ciao)"), () -> new ArrayList<String>())
        .get();
    System.out.println(ss);
    System.out.println(allNb.build(ParsableNamedParamMap.parse("nums.one(value=5.3)")));
  }

  @SuppressWarnings("unchecked")
  public <T> Optional<T> build(NamedParamMap map, Supplier<T> defaultSupplier) {
    if (!builders.containsKey(map.getName())) {
      return Optional.empty();
    }
    try {
      T t = (T) builders.get(map.getName()).build(map, this);
      return t == null ? Optional.empty() : Optional.of(t);
    } catch (IllegalArgumentException e) {
      return Optional.empty();
    }
  }

  public Optional<Object> build(NamedParamMap map) {
    return build(map, Object::new);
  }

  public void register(String name, Builder<?> builder) {
    builders.put(name, builder);
  }

  public void register(String prefix, NamedBuilder namedBuilder) {
    namedBuilder.builders.forEach((s, b) -> builders.put(prefix + NAME_SEPARATOR + s, b));
  }

}
