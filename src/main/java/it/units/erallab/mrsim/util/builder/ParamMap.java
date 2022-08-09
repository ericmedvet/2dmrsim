package it.units.erallab.mrsim.util.builder;

import java.util.List;

public interface ParamMap {
  boolean b(String n);

  List<Boolean> bs(String n);

  Double d(String n);

  List<Double> ds(String n);

  Integer i(String n);

  List<Integer> is(String n);

  ParsableNamedParamMap npm(String n);

  List<ParsableNamedParamMap> npms(String n);

  String s(String n);

  List<String> ss(String n);

  default String s(String n, String regex) {
    String s = s(n);
    if (s == null || !s.matches(regex)) {
      return null;
    }
    return s;
  }

  default List<String> ss(String n, String regex) {
    List<String> ss = ss(n);
    if (ss == null || ss.stream().filter(s -> s.matches(regex)).count() != ss.size()) {
      return null;
    }
    return ss;
  }
}
