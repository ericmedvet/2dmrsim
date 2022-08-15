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
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * @author "Eric Medvet" on 2022/08/08 for 2d-robot-evolution
 */
public class StringNamedParamMap implements NamedParamMap {

  // BNF
  // <e> ::= <n>(<nps>)
  // <nps> ::= ∅ | <np> | <nps>;<np>
  // <np> ::= <n>=<e> | <n>=<d> | <n>=<s> | <n>=<le> | <n>=<ld> | <n>=<ls>
  // <le> ::= (<np>)*<le> | [<es>]
  // <ld> ::= [<ds>]
  // <ls> ::= [<ss>]
  // <es> ::= ∅ | <e> | <es>;<e>
  // <ds> ::= ∅ | <d> | <ds>;<d>
  // <ns> ::= ∅ |<n> | <ns>;<s>


  private final String name;
  private final SortedMap<String, Double> dMap;
  private final SortedMap<String, String> sMap;
  private final SortedMap<String, StringNamedParamMap> npmMap;
  private final SortedMap<String, List<Double>> dsMap;
  private final SortedMap<String, List<String>> ssMap;
  private final SortedMap<String, List<StringNamedParamMap>> npmsMap;

  private StringNamedParamMap(
      String name,
      Map<String, Double> dMap,
      Map<String, String> sMap,
      Map<String, StringNamedParamMap> npmMap,
      Map<String, List<Double>> dsMap,
      Map<String, List<String>> ssMap,
      Map<String, List<StringNamedParamMap>> npmsMap
  ) {
    this.name = name;
    this.dMap = new TreeMap<>(dMap);
    this.sMap = new TreeMap<>(sMap);
    this.npmMap = new TreeMap<>(npmMap);
    this.dsMap = new TreeMap<>(dsMap);
    this.ssMap = new TreeMap<>(ssMap);
    this.npmsMap = new TreeMap<>(npmsMap);
  }

  private StringNamedParamMap(ENode eNode) {
    this(
        eNode.name(),
        eNode.child().children().stream()
            .filter(n -> n.value() instanceof DNode)
            .collect(Collectors.toMap(NPNode::name, n -> ((DNode) n.value()).value().doubleValue())),
        eNode.child().children().stream()
            .filter(n -> n.value() instanceof SNode)
            .collect(Collectors.toMap(NPNode::name, n -> ((SNode) n.value()).value())),
        eNode.child().children().stream()
            .filter(n -> n.value() instanceof ENode)
            .collect(Collectors.toMap(NPNode::name, n -> new StringNamedParamMap((ENode) n.value))),
        eNode.child().children().stream()
            .filter(n -> n.value() instanceof LDNode)
            .collect(Collectors.toMap(
                NPNode::name,
                n -> ((LDNode) n.value()).child().children().stream().map(c -> c.value().doubleValue()).toList()
            )),
        eNode.child().children().stream()
            .filter(n -> n.value() instanceof LSNode)
            .collect(Collectors.toMap(
                NPNode::name,
                n -> ((LSNode) n.value()).child().children().stream().map(SNode::value).toList()
            )),
        eNode.child().children().stream()
            .filter(n -> n.value() instanceof LENode)
            .collect(Collectors.toMap(
                NPNode::name,
                n -> ((LENode) n.value()).child().children().stream().map(StringNamedParamMap::new).toList()
            ))
    );
  }

  private enum TokenType {
    NUM("\\s*-?[0-9]+(\\.[0-9]+)?\\s*", ""),
    STRING("\\s*([A-Za-z][A-Za-z0-9_]*)|(\"[./:\\-\\w]+\")\\s*", ""),
    NAME("\\s*[A-Za-z][" + NamedBuilder.NAME_SEPARATOR + "A-Za-z0-9_]*\\s*", ""),
    OPEN_CONTENT("\\s*\\(\\s*", "("),
    CLOSED_CONTENT("\\s*\\)\\s*", ")"),
    ASSIGN_SEPARATOR("\\s*=\\s*", "="),
    LIST_SEPARATOR("\\s*;\\s*", ";"),
    OPEN_LIST("\\s*\\[\\s*", "["),
    CLOSED_LIST("\\s*\\]\\s*", "]"),
    JOIN("\\s*\\*\\s*", "*");
    private final String regex;
    private final String rendered;

    TokenType(String regex, String rendered) {
      this.regex = regex;
      this.rendered = rendered;
    }

    public Optional<Token> next(String s, int i) {
      Matcher matcher = Pattern.compile(regex).matcher(s);
      if (!matcher.find(i)) {
        return Optional.empty();
      }
      if (matcher.start() != i) {
        return Optional.empty();
      }
      return Optional.of(new Token(matcher.start(), matcher.end()));
    }

    public String rendered() {
      return rendered;
    }
  }

  private interface Node {
    Token token();
  }

  private record DNode(Token token, Number value) implements Node {
    static DNode parse(String s, int i) {
      return TokenType.NUM.next(s, i).map(t -> new DNode(
          t,
          Double.parseDouble(t.trimmedContent(s))
      )).orElseThrow(error(TokenType.NUM, s, i));
    }
  }

  private record DSNode(Token token, List<DNode> children) implements Node {
    static DSNode parse(String s, int i) {
      List<DNode> nodes = new ArrayList<>();
      try {
        nodes.add(DNode.parse(s, i));
      } catch (IllegalArgumentException e) {
        //ignore
      }
      while (!nodes.isEmpty()) {
        Optional<Token> sepT = TokenType.LIST_SEPARATOR.next(s, nodes.get(nodes.size() - 1).token().end());
        if (sepT.isEmpty()) {
          break;
        }
        nodes.add(DNode.parse(s, sepT.get().end()));
      }
      return new DSNode(new Token(i, nodes.isEmpty() ? i : nodes.get(nodes.size() - 1).token().end()), nodes);
    }
  }

  private record ENode(Token token, NPSNode child, String name) implements Node {
    static ENode parse(String s, int i) {
      Token tName = TokenType.NAME.next(s, i).orElseThrow(error(TokenType.NAME, s, i));
      Token tOpenPar = TokenType.OPEN_CONTENT.next(s, tName.end()).orElseThrow(error(
          TokenType.OPEN_CONTENT,
          s,
          tName.end()
      ));
      NPSNode npsNode = NPSNode.parse(s, tOpenPar.end());
      Token tClosedPar = TokenType.CLOSED_CONTENT.next(
          s,
          npsNode.token().end()
      ).orElseThrow(error(
          TokenType.CLOSED_CONTENT,
          s,
          npsNode.token().end()
      ));
      return new ENode(new Token(tName.start(), tClosedPar.end()), npsNode, tName.trimmedContent(s));
    }
  }

  private record ESNode(Token token, List<ENode> children) implements Node {
    static ESNode parse(String s, int i) {
      List<ENode> nodes = new ArrayList<>();
      try {
        nodes.add(ENode.parse(s, i));
      } catch (IllegalArgumentException e) {
        //ignore
      }
      while (!nodes.isEmpty()) {
        Optional<Token> sepT = TokenType.LIST_SEPARATOR.next(s, nodes.get(nodes.size() - 1).token().end());
        if (sepT.isEmpty()) {
          break;
        }
        nodes.add(ENode.parse(s, sepT.get().end()));
      }
      return new ESNode(new Token(i, nodes.isEmpty() ? i : nodes.get(nodes.size() - 1).token().end()), nodes);
    }
  }

  private record NPSNode(Token token, List<NPNode> children) implements Node {
    static NPSNode parse(String s, int i) {
      List<NPNode> nodes = new ArrayList<>();
      try {
        nodes.add(NPNode.parse(s, i));
      } catch (IllegalArgumentException e) {
        //ignore
      }
      while (!nodes.isEmpty()) {
        Optional<Token> ot = TokenType.LIST_SEPARATOR.next(s, nodes.get(nodes.size() - 1).token().end());
        if (ot.isEmpty()) {
          break;
        }
        nodes.add(NPNode.parse(s, ot.get().end()));
      }
      return new NPSNode(new Token(i, nodes.isEmpty() ? i : nodes.get(nodes.size() - 1).token().end()), nodes);
    }
  }

  private record NPNode(Token token, String name, Node value) implements Node {
    static NPNode parse(String s, int i) {
      Token tName = TokenType.STRING.next(s, i).orElseThrow(error(TokenType.STRING, s, i));
      Token tAssign = TokenType.ASSIGN_SEPARATOR.next(s, tName.end()).orElseThrow(error(
          TokenType.ASSIGN_SEPARATOR,
          s,
          tName.end()
      ));
      Node value = null;
      try {
        value = ENode.parse(s, tAssign.end());
      } catch (IllegalArgumentException e) {
        //ignore
      }
      if (value == null) {
        try {
          value = DNode.parse(s, tAssign.end());
        } catch (IllegalArgumentException e) {
          //ignore
        }
      }
      if (value == null) {
        try {
          value = SNode.parse(s, tAssign.end());
        } catch (IllegalArgumentException e) {
          //ignore
        }
      }
      if (value == null) {
        try {
          value = LENode.parse(s, tAssign.end());
        } catch (IllegalArgumentException e) {
          //ignore
        }
      }
      if (value == null) {

        try {
          value = LDNode.parse(s, tAssign.end());
        } catch (IllegalArgumentException e) {
          //ignore
        }
      }
      if (value == null) {
        try {
          value = LSNode.parse(s, tAssign.end());
        } catch (IllegalArgumentException e) {
          //ignore
        }
      }
      if (value == null) {
        throw new IllegalArgumentException(String.format(
            "Cannot find valid token as param value at `%s`",
            s.substring(tAssign.end())
        ));
      }
      return new NPNode(
          new Token(tName.start(), value.token().end()),
          tName.trimmedContent(s),
          value
      );
    }
  }

  private record SNode(Token token, String value) implements Node {
    static SNode parse(String s, int i) {
      return TokenType.STRING.next(s, i).map(t -> new SNode(
          t,
          t.trimmedUnquotedContent(s)
      )).orElseThrow(error(TokenType.STRING, s, i));
    }
  }

  private record SSNode(Token token, List<SNode> children) implements Node {
    static SSNode parse(String s, int i) {
      List<SNode> nodes = new ArrayList<>();
      try {
        nodes.add(SNode.parse(s, i));
      } catch (IllegalArgumentException e) {
        //ignore
      }
      while (!nodes.isEmpty()) {
        Optional<Token> sepT = TokenType.LIST_SEPARATOR.next(s, nodes.get(nodes.size() - 1).token().end());
        if (sepT.isEmpty()) {
          break;
        }
        nodes.add(SNode.parse(s, sepT.get().end()));
      }
      return new SSNode(new Token(i, nodes.isEmpty() ? i : nodes.get(nodes.size() - 1).token().end()), nodes);
    }

  }

  private record LENode(Token token, ESNode child) implements Node {
    @SuppressWarnings("InfiniteRecursion")
    static LENode parse(String s, int i) {
      //list with join
      try {
        Token openT = TokenType.OPEN_CONTENT.next(s, i).orElseThrow(error(TokenType.OPEN_CONTENT, s, i));
        NPNode npNode = NPNode.parse(s, openT.end());
        Token closedT = TokenType.CLOSED_CONTENT.next(s, npNode.token().end()).orElseThrow(error(
            TokenType.CLOSED_CONTENT,
            s,
            npNode.token().end()
        ));
        Token jointT = TokenType.JOIN.next(s, closedT.end()).orElseThrow(error(TokenType.JOIN, s, closedT.end()));
        LENode outerLENode = LENode.parse(s, jointT.end());
        //do cartesian product
        List<ENode> originalENodes = outerLENode.child().children();
        List<ENode> eNodes = new ArrayList<>();
        for (ENode originalENode : originalENodes) {
          if (npNode.value() instanceof DNode || npNode.value() instanceof SNode || npNode.value() instanceof ENode) {
            eNodes.add(new ENode(
                originalENode.token(),
                new NPSNode(originalENode.child().token(), withAppended(originalENode.child().children(), npNode)),
                originalENode.name()
            ));
          } else {
            if (npNode.value() instanceof LDNode ldNode) {
              for (DNode dNode : ldNode.child().children()) {
                eNodes.add(new ENode(
                    originalENode.token(),
                    new NPSNode(originalENode.child().token(), withAppended(
                        originalENode.child().children(),
                        new NPNode(ldNode.token(), npNode.name(), dNode)
                    )),
                    originalENode.name()
                ));
              }
            } else if (npNode.value() instanceof LSNode lsNode) {
              for (SNode sNode : lsNode.child().children()) {
                eNodes.add(new ENode(
                    originalENode.token(),
                    new NPSNode(originalENode.child().token(), withAppended(
                        originalENode.child().children(),
                        new NPNode(lsNode.token(), npNode.name(), sNode)
                    )),
                    originalENode.name()
                ));
              }
            } else if (npNode.value() instanceof LENode leNode) {
              for (ENode eNode : leNode.child().children()) {
                eNodes.add(new ENode(
                    originalENode.token(),
                    new NPSNode(originalENode.child().token(), withAppended(
                        originalENode.child().children(),
                        new NPNode(leNode.token(), npNode.name(), eNode)
                    )),
                    originalENode.name()
                ));
              }
            }
          }
        }
        return new LENode(new Token(openT.start(), outerLENode.token().end()), new ESNode(
            new Token(npNode.token().start(), outerLENode.token.end()),
            eNodes
        ));
      } catch (IllegalArgumentException e) {
        //ignore
      }
      //just list
      Token openT = TokenType.OPEN_LIST.next(s, i).orElseThrow(error(TokenType.OPEN_LIST, s, i));
      ESNode sNode = ESNode.parse(s, openT.end());
      Token closedT = TokenType.CLOSED_LIST.next(s, sNode.token().end()).orElseThrow(error(
          TokenType.CLOSED_LIST,
          s,
          sNode.token().end()
      ));
      return new LENode(new Token(openT.start(), closedT.end()), sNode);
    }
  }

  private record LDNode(Token token, DSNode child) implements Node {
    static LDNode parse(String s, int i) {
      Token openT = TokenType.OPEN_LIST.next(s, i).orElseThrow(error(TokenType.OPEN_LIST, s, i));
      DSNode sNode = DSNode.parse(s, openT.end());
      Token closedT = TokenType.CLOSED_LIST.next(s, sNode.token().end()).orElseThrow(error(
          TokenType.CLOSED_LIST,
          s,
          sNode.token().end()
      ));
      return new LDNode(new Token(openT.start(), closedT.end()), sNode);
    }
  }

  private record LSNode(Token token, SSNode child) implements Node {
    static LSNode parse(String s, int i) {
      Token openT = TokenType.OPEN_LIST.next(s, i).orElseThrow(error(TokenType.OPEN_LIST, s, i));
      SSNode sNode = SSNode.parse(s, openT.end());
      Token closedT = TokenType.CLOSED_LIST.next(s, sNode.token().end()).orElseThrow(error(
          TokenType.CLOSED_LIST,
          s,
          sNode.token().end()
      ));
      return new LSNode(new Token(openT.start(), closedT.end()), sNode);
    }
  }

  private record Token(int start, int end) {
    public String trimmedContent(String s) {
      return s.substring(start, end).trim();
    }

    public String trimmedUnquotedContent(String s) {
      return trimmedContent(s).replaceAll("\"", "");
    }
  }

  private static Supplier<IllegalArgumentException> error(TokenType tokenType, String s, int i) {
    return () -> new IllegalArgumentException(String.format(
        "Cannot find %s token: `%s` does not match %s",
        tokenType.name().toLowerCase(),
        s.substring(i),
        tokenType.regex
    ));
  }

  private static boolean isInt(Double v) {
    return v.intValue() == v;
  }

  private static <T> List<T> withAppended(List<T> ts, T t) {
    List<T> newTs = new ArrayList<>(ts);
    newTs.add(t);
    return newTs;
  }

  public static StringNamedParamMap parse(String string) throws IllegalArgumentException {
    ENode eNode = ENode.parse(string, 0);
    return new StringNamedParamMap(eNode);
  }

  @Override
  public Boolean b(String n) {
    if (sMap.containsKey(n)) {
      return sMap.get(n).equalsIgnoreCase(Boolean.TRUE.toString());
    }
    return null;
  }

  @Override
  public List<Boolean> bs(String n) {
    if (!ssMap.containsKey(n)) {
      return null;
    }
    return ssMap.get(n).stream().map(s -> s.equalsIgnoreCase(Boolean.TRUE.toString())).toList();
  }

  @Override
  public Double d(String n) {
    return dMap.get(n);
  }

  @Override
  public List<Double> ds(String n) {
    return dsMap.get(n);
  }

  @Override
  public Integer i(String n) {
    if (!dMap.containsKey(n)) {
      return null;
    }
    double v = dMap.get(n);
    return isInt(v) ? (int) v : null;
  }

  @Override
  public List<Integer> is(String n) {
    if (!dsMap.containsKey(n)) {
      return null;
    }
    List<Double> vs = dsMap.get(n);
    List<Integer> is = vs.stream().filter(StringNamedParamMap::isInt).map(Double::intValue).toList();
    if (is.size() == vs.size()) {
      return is;
    }
    return null;
  }

  @Override
  public StringNamedParamMap npm(String n) {
    return npmMap.get(n);
  }

  @Override
  public List<NamedParamMap> npms(String n) {
    return npmsMap.containsKey(n) ? npmsMap.get(n).stream().map(m -> (NamedParamMap) m).toList() : null;
  }

  @Override
  public String s(String n) {
    return sMap.get(n);
  }

  @Override
  public List<String> ss(String n) {
    return ssMap.get(n);
  }

  @Override
  public String getName() {
    return name;
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append(name);
    sb.append(TokenType.OPEN_CONTENT.rendered());
    Map<String, String> content = new TreeMap<>();
    dMap.forEach((key, value) -> content.put(key, value.toString()));
    npmMap.forEach((key, value) -> content.put(key, value.toString()));
    content.putAll(sMap);
    dsMap.forEach((key, value) -> content.put(
        key,
        TokenType.OPEN_LIST.rendered() +
            value.stream()
                .map(Object::toString)
                .collect(Collectors.joining(TokenType.LIST_SEPARATOR.rendered)) +
            TokenType.CLOSED_LIST.rendered()
    ));
    ssMap.forEach((key, value) -> content.put(
        key,
        TokenType.OPEN_LIST.rendered() +
            value.stream()
                .map(Object::toString)
                .collect(Collectors.joining(TokenType.LIST_SEPARATOR.rendered)) +
            TokenType.CLOSED_LIST.rendered()
    ));
    npmsMap.forEach((key, value) -> content.put(
        key,
        TokenType.OPEN_LIST.rendered() +
            value.stream()
                .map(Object::toString)
                .collect(Collectors.joining(TokenType.LIST_SEPARATOR.rendered)) +
            TokenType.CLOSED_LIST.rendered()
    ));
    sb.append(content.entrySet().stream()
        .map(e -> e.getKey() + TokenType.ASSIGN_SEPARATOR.rendered() + e.getValue())
        .collect(Collectors.joining(TokenType.LIST_SEPARATOR.rendered())));
    sb.append(TokenType.CLOSED_CONTENT.rendered());
    return sb.toString();
  }

  public static String prettyToString(ParamMap map) {
    return prettyToString(map, 80);
  }

  public static String prettyToString(ParamMap map, int maxW) {
    StringBuilder sb = new StringBuilder();
    prettyToString(map, sb, maxW, 0, 2, " ");
    return sb.toString();
  }

  public static void prettyToString(ParamMap map, StringBuilder sb, int maxW, int w, int indent, String space) {
    //iterate
    if (map instanceof NamedParamMap namedParamMap) {
      sb.append(namedParamMap.getName());
    }
    sb.append(TokenType.OPEN_CONTENT.rendered());
    String content = mapContentToInlineString(map, space);
    if (map.names().isEmpty() || content.length() + currentLineLength(sb.toString()) < maxW) {
      sb.append(content);
    } else {
      mapContentToMultilineString(sb, maxW, w, indent, space, map);
    }
    sb.append(TokenType.CLOSED_CONTENT.rendered());
  }

  private static void listContentToMultilineString(
      StringBuilder sb,
      int maxW,
      int w,
      int indent,
      String space,
      List<?> l
  ) {
    for (int j = 0; j < l.size(); j++) {
      sb.append("\n").append(indent(w + indent + indent));
      if (l.get(j) instanceof NamedParamMap m) {
        prettyToString(m, sb, maxW, w + indent + indent, indent, space);
      } else {
        sb.append(l.get(j).toString());
      }
      if (j < l.size() - 1) {
        sb.append(TokenType.LIST_SEPARATOR.rendered());
      }
    }
    sb.append("\n").append(indent(w + indent));
  }

  private static String listContentToInlineString(List<?> l, String space) {
    StringBuilder sb = new StringBuilder();
    for (int j = 0; j < l.size(); j++) {
      if (l.get(j) instanceof ParamMap m) {
        if (m instanceof NamedParamMap namedParamMap) {
          sb.append(namedParamMap.getName())
              .append(TokenType.OPEN_CONTENT.rendered());
        }
        sb.append(mapContentToInlineString(m, space));
        if (m instanceof NamedParamMap) {
          sb.append(TokenType.CLOSED_CONTENT.rendered());
        }
      } else {
        sb.append(l.get(j).toString());
      }
      if (j < l.size() - 1) {
        sb.append(TokenType.LIST_SEPARATOR.rendered()).append(space);
      }
    }
    return sb.toString();
  }

  private static void mapContentToMultilineString(
      StringBuilder sb,
      int maxW,
      int w,
      int indent,
      String space,
      ParamMap map
  ) {
    List<String> names = new ArrayList<>(map.names());
    for (int i = 0; i < names.size(); i++) {
      sb.append("\n")
          .append(indent(w + indent))
          .append(names.get(i))
          .append(space)
          .append(TokenType.ASSIGN_SEPARATOR.rendered())
          .append(space);
      Object value = map.value(names.get(i));
      if (value instanceof List<?> l) {
        sb.append(TokenType.OPEN_LIST.rendered());
        String listContent = listContentToInlineString(l, space);
        if (l.isEmpty() || listContent.length() + currentLineLength(sb.toString()) < maxW) {
          sb.append(listContent);
        } else {
          listContentToMultilineString(sb, maxW, w, indent, space, l);
        }
        sb.append(TokenType.CLOSED_LIST.rendered());
      } else if (value instanceof NamedParamMap m) {
        prettyToString(m, sb, maxW, w + indent, indent, space);
      } else {
        sb.append(value.toString());
      }
      if (i < names.size() - 1) {
        sb.append(TokenType.LIST_SEPARATOR.rendered());
      }
    }
    sb.append("\n").append(indent(w));
  }

  private static String mapContentToInlineString(ParamMap m, String space) {
    StringBuilder sb = new StringBuilder();
    List<String> names = new ArrayList<>(m.names());
    for (int i = 0; i < names.size(); i++) {
      sb.append(names.get(i))
          .append(space)
          .append(TokenType.ASSIGN_SEPARATOR.rendered())
          .append(space);
      Object value = m.value(names.get(i));
      if (value instanceof List<?> l) {
        sb.append(listContentToInlineString(l, space));
      } else if (value instanceof ParamMap innerMap) {
        if (innerMap instanceof NamedParamMap namedParamMap) {
          sb.append(namedParamMap.getName())
              .append(TokenType.OPEN_CONTENT.rendered());
        }
        sb.append(mapContentToInlineString(innerMap, space));
        if (innerMap instanceof NamedParamMap) {
          sb.append(TokenType.CLOSED_CONTENT.rendered());
        }
      } else {
        sb.append(value.toString());
      }
      if (i < names.size() - 1) {
        sb.append(TokenType.LIST_SEPARATOR.rendered()).append(space);
      }
    }
    return sb.toString();
  }

  private static String indent(int w) {
    return IntStream.range(0, w).mapToObj(i -> " ").collect(Collectors.joining());
  }

  private static int currentLineLength(String s) {
    String[] lines = s.split("\n");
    return lines[lines.length - 1].length();
  }

  @Override
  public Set<String> names() {
    Set<String> names = new TreeSet<>();
    names.addAll(dMap.keySet());
    names.addAll(sMap.keySet());
    names.addAll(npmMap.keySet());
    names.addAll(dsMap.keySet());
    names.addAll(ssMap.keySet());
    names.addAll(npmsMap.keySet());
    return names;
  }

}
