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
