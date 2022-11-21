package it.units.erallab.mrsim2d.builder;

import java.io.PrintStream;
import java.lang.reflect.Type;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * @author "Eric Medvet" on 2022/11/02 for 2dmrsim
 */
public class InfoPrinter {

  private static final int N_OF_COMPATIBILITY_ATTEMPTS = 10;
  private static final Set<DocumentedBuilder.Type> EASY_TYPES = EnumSet.of(
      DocumentedBuilder.Type.BOOLEAN,
      DocumentedBuilder.Type.DOUBLE,
      DocumentedBuilder.Type.INT,
      DocumentedBuilder.Type.STRING
  );


  private static final int PACKAGE_HEADING_LEVEL = 2;
  private static final int BUILDER_HEADING_LEVEL = PACKAGE_HEADING_LEVEL + 1;
  private final int packageHeadingLevel;
  private final int builderHeadingLevel;
  private final boolean shortenFQNs;
  private final boolean computeCompatibilities;

  public InfoPrinter(
      int packageHeadingLevel,
      int builderHeadingLevel,
      boolean shortenFQNs,
      boolean computeCompatibilities
  ) {
    this.packageHeadingLevel = packageHeadingLevel;
    this.builderHeadingLevel = builderHeadingLevel;
    this.shortenFQNs = shortenFQNs;
    this.computeCompatibilities = computeCompatibilities;
  }

  public InfoPrinter() {
    this(PACKAGE_HEADING_LEVEL, BUILDER_HEADING_LEVEL, true, false);
  }

  record BuilderInfo(
      SortedSet<Name> names, Builder<?> builder, List<ParamCompatibility> compatibilities,
      SortedSet<String> workingUsages
  ) {
    public Name longestName() {
      return names().stream()
          .max(Comparator.comparingInt(n -> n.fullName().length()))
          .orElseThrow();
    }

    public Name shortestName() {
      return names().stream()
          .min(Comparator.comparingInt(n -> n.fullName().length()))
          .orElseThrow();
    }
  }

  record Name(String packageName, String simpleName) implements Comparable<Name> {
    public Name(String fullName) {
      this(packageName(fullName), simpleName(fullName));
    }

    public static String packageName(String fullName) {
      String sep = "" + NamedBuilder.NAME_SEPARATOR;
      List<String> pieces = Arrays.stream(fullName.split(Pattern.quote(sep))).toList();
      String packageName = "";
      if (pieces.size() > 1) {
        packageName = String.join(sep, pieces.subList(0, pieces.size() - 1));
      }
      return packageName;
    }

    public static String simpleName(String fullName) {
      String sep = "" + NamedBuilder.NAME_SEPARATOR;
      String[] pieces = fullName.split(Pattern.quote(sep));
      return pieces[pieces.length - 1];
    }

    @Override
    public int compareTo(Name o) {
      if (o.packageName.equals(packageName)) {
        return simpleName.compareTo(o.simpleName);
      }
      return packageName.compareTo(o.packageName);
    }

    public String fullName() {
      String n = simpleName;
      if (!packageName.isEmpty()) {
        n = packageName + NamedBuilder.NAME_SEPARATOR + simpleName;
      }
      return n;
    }

  }

  record PackageInfo(SortedSet<String> names, List<BuilderInfo> builderInfos) {}

  record ParamCompatibility(DocumentedBuilder.ParamInfo paramInfo, List<BuilderInfo> builderInfos) {}

  record ParamTriplet(String name, String value, BuilderInfo builderInfo) {}

  private static List<ParamTriplet> easyParamPairs(List<DocumentedBuilder.ParamInfo> builder) {
    return builder.stream()
        .filter(pi -> pi.defaultValue() == null)
        .filter(pi -> EASY_TYPES.contains(pi.type()))
        .map(pi -> new ParamTriplet(
            pi.name(),
            switch (pi.type()) {
              case INT -> "1";
              case DOUBLE -> "1.0";
              case STRING -> "a";
              case BOOLEAN -> "false";
              default -> throw new IllegalStateException("Unexpected value: " + pi.type());
            },
            null
        ))
        .toList();
  }

  private static String heading(int level) {
    return String.join("", Collections.nCopies(level, "#"));
  }

  private static List<List<ParamTriplet>> paramTriplets(DocumentedBuilder<?> builder, List<BuilderInfo> builderInfos) {
    List<List<ParamTriplet>> paramPairs = new ArrayList<>();
    //easy case
    paramPairs.add(easyParamPairs(builder.params()));
    List<DocumentedBuilder.ParamInfo> npmParams = builder.params().stream()
        .filter(pi -> pi.type().equals(DocumentedBuilder.Type.NAMED_PARAM_MAP))
        .toList();
    if (!builderInfos.isEmpty() && !npmParams.isEmpty()) {
      //cartesian
      int[] indexes = new int[npmParams.size()];
      boolean done = false;
      while (!done) {
        List<ParamTriplet> localParamTriplets = new ArrayList<>(easyParamPairs(builder.params()));
        for (int i = 0; i < indexes.length; i++) {
          localParamTriplets.add(new ParamTriplet(
              npmParams.get(i).name(),
              builderInfos.get(indexes[i]).workingUsages().first(),
              builderInfos.get(indexes[i])
          ));
        }
        indexes[0] = indexes[0] + 1;
        for (int i = 0; i < indexes.length; i++) {
          if (indexes[i] >= builderInfos.size()) {
            if (i == indexes.length - 1) {
              done = true;
              break;
            } else {
              indexes[i] = 0;
              indexes[i + 1] = indexes[i + 1] + 1;
            }
          }
        }
        paramPairs.add(localParamTriplets);
      }
    }
    return paramPairs;
  }

  public void print(NamedBuilder<?> nb, PrintStream ps) {
    //group by aliases
    Map<Name, BuilderInfo> aliasesMap = new HashMap<>();
    @SuppressWarnings("unchecked") Map<String, Builder<?>> builders = (Map<String, Builder<?>>) nb.getBuilders();
    builders.forEach((fullName, builder) -> {
      Name mainName = aliasesMap.entrySet().stream()
          .filter(e -> e.getValue().builder() == builder)
          .findFirst()
          .map(Map.Entry::getKey)
          .orElse(null);
      if (mainName == null) {
        List<ParamCompatibility> compatibilities = new ArrayList<>();
        if (builder instanceof DocumentedBuilder<?> documentedBuilder) {
          documentedBuilder.params().stream()
              .filter(pi -> pi.injection().equals(Param.Injection.NONE))
              .forEach(pi -> compatibilities.add(new ParamCompatibility(pi, new ArrayList<>())));
        }
        aliasesMap.put(new Name(fullName), new BuilderInfo(
            new TreeSet<>(),
            builder,
            Collections.unmodifiableList(compatibilities),
            new TreeSet<>()
        ));
        mainName = new Name(fullName);
      }
      aliasesMap.get(mainName).names.add(new Name(fullName));
    });
    if (computeCompatibilities) {
      //find usages
      List<BuilderInfo> buildableBuilders = new ArrayList<>();
      int nOfAttempts = 0;
      while (aliasesMap.values().stream().anyMatch(bi -> bi.workingUsages()
          .isEmpty()) && nOfAttempts < N_OF_COMPATIBILITY_ATTEMPTS) {
        nOfAttempts = nOfAttempts + 1;
        System.out.printf(
            "Usages attempt %d on %d: %d known, %d unknown%n",
            nOfAttempts,
            N_OF_COMPATIBILITY_ATTEMPTS,
            buildableBuilders.size(),
            aliasesMap.values().size() - buildableBuilders.size()
        );
        for (BuilderInfo bi : aliasesMap.values()) {
          if (bi.workingUsages().isEmpty()) {
            if (bi.builder() instanceof DocumentedBuilder<?> builder) {
              List<List<ParamTriplet>> triplets = paramTriplets(builder, buildableBuilders);
              System.out.printf(
                  "Usages for %s with %d cases%n",
                  bi.shortestName().fullName(),
                  triplets.size()
              );
              for (List<ParamTriplet> paramTriplets : triplets) {
                try {
                  String usage = "%s(%s)".formatted(
                      bi.shortestName().fullName(),
                      paramTriplets.stream()
                          .map(pp -> "%s=%s".formatted(pp.name(), pp.value()))
                          .collect(Collectors.joining(";"))
                  );
                  nb.build(usage);
                  bi.workingUsages().add(usage);
                  buildableBuilders.add(bi);
                  break;
                } catch (BuilderException | IllegalArgumentException e) {
                  //ignore
                }
              }
            }
          }
        }
      }
      //update compatibilities
      for (BuilderInfo builderInfo : buildableBuilders) {
        if (builderInfo.builder() instanceof DocumentedBuilder<?> builder) {
          if (builder.params().stream().noneMatch(pi -> pi.type().equals(DocumentedBuilder.Type.NAMED_PARAM_MAP))) {
            continue;
          }
          List<List<ParamTriplet>> triplets = paramTriplets(builder, buildableBuilders);
          System.out.printf(
              "Compatibility for %s with %d cases%n",
              builderInfo.shortestName().fullName(),
              triplets.size()
          );
          for (List<ParamTriplet> paramTriplets : triplets) {
            try {
              String usage = "%s(%s)".formatted(
                  builderInfo.shortestName().fullName(),
                  paramTriplets.stream()
                      .map(pt -> "%s=%s".formatted(pt.name(), pt.value()))
                      .collect(Collectors.joining(";"))
              );
              nb.build(usage);
              builderInfo.compatibilities().stream()
                  .filter(c -> c.paramInfo().type().equals(DocumentedBuilder.Type.NAMED_PARAM_MAP))
                  .filter(c -> c.paramInfo().defaultValue() == null)
                  .forEach(c -> {
                    BuilderInfo cbi = paramTriplets.stream()
                        .filter(t -> t.name().equals(c.paramInfo().name()))
                        .findFirst()
                        .orElseThrow()
                        .builderInfo();
                    if (c.builderInfos.stream().noneMatch(bi -> bi.shortestName().equals(cbi.shortestName()))) {
                      c.builderInfos().add(cbi);
                    }
                  });
            } catch (BuilderException | IllegalArgumentException e) {
              //ignore
            }
          }
        }
      }
    }
    //group all by packages
    Map<String, PackageInfo> packagesMap = new HashMap<>();
    aliasesMap.forEach((name, builderInfo) -> {
      String longestPackageName = builderInfo.names().stream()
          .map(Name::packageName)
          .max(Comparator.comparingInt(String::length))
          .orElseThrow();
      PackageInfo packageInfo = packagesMap.getOrDefault(
          longestPackageName,
          new PackageInfo(new TreeSet<>(), new ArrayList<>())
      );
      packageInfo.names().addAll(builderInfo.names().stream().map(Name::packageName).toList());
      packageInfo.builderInfos().add(builderInfo);
      packagesMap.put(longestPackageName, packageInfo);
    });
    //iterate over packages
    SortedSet<String> packageNames = new TreeSet<>(packagesMap.keySet());
    for (String packageName : packageNames) {
      PackageInfo packageInfo = packagesMap.get(packageName);
      if (packageName.isEmpty()) {
        ps.printf("%s Unnamed package%n", heading(packageHeadingLevel));
      } else {
        ps.printf("%s Package `%s`%n", heading(packageHeadingLevel), packageName);
      }
      ps.println();
      if (packageInfo.names().size() > 1) {
        ps.print("Aliases: ");
        ps.println(packageInfo.names().stream().map("`%s`"::formatted).collect(Collectors.joining(", ")));
        ps.println();
      }
      packageInfo.builderInfos().stream()
          .sorted(Comparator.comparing(builderInfo -> builderInfo.shortestName().simpleName()))
          .forEach(builderInfo -> {
            ps.printf("%s Builder `%s()`%n", heading(builderHeadingLevel), builderInfo.longestName().fullName());
            ps.println();
            if (builderInfo.builder() instanceof DocumentedBuilder<?> documentedBuilder) {
              if (documentedBuilder.params().isEmpty()) {
                //signature
                ps.printf("`%s()`%n", builderInfo.shortestName().fullName());
              } else {
                //signature
                ps.printf(
                    "`%s(%s)`%n",
                    builderInfo.shortestName().fullName(),
                    documentedBuilder.params().stream()
                        .map(DocumentedBuilder.ParamInfo::name)
                        .collect(Collectors.joining("; "))
                );
                ps.println();
                //table with args
                if (computeCompatibilities) {
                  ps.println("| Param | Type | Default | Java type | Compatible builders |");
                  ps.println("| --- | --- | --- | --- | --- |");
                } else {
                  ps.println("| Param | Type | Default | Java type |");
                  ps.println("| --- | --- | --- | --- |");
                }
                for (ParamCompatibility compatibility : builderInfo.compatibilities()) {
                  if (compatibility.paramInfo().injection().equals(Param.Injection.NONE)) {
                    ps.printf(
                        "| `%s` | %s | %s | %s |" + (computeCompatibilities ? " %s|" : "") + "%n",
                        compatibility.paramInfo().name(),
                        compatibility.paramInfo().type().rendered(),
                        Objects.isNull(compatibility.paramInfo()
                            .defaultValue()) ? "" : "`%s`".formatted(compatibility.paramInfo().defaultValue()),
                        shortenJavaTypeName(compatibility.paramInfo().javaType()),
                        compatibility.builderInfos().stream()
                            .sorted(Comparator.comparing(bi -> bi.shortestName().fullName()))
                            .map(bi -> "`%s()`".formatted(bi.shortestName().fullName()))
                            .collect(Collectors.joining("<br>"))
                    );
                  }
                }
              }
              ps.println();
              ps.printf("Produces %s%n", shortenJavaTypeName(documentedBuilder.builtType()));
            } else {
              //signature
              ps.printf("`%s()` (no more info available)%n", builderInfo.shortestName().fullName());
            }
            ps.println();
          });
    }
  }

  private String shortenJavaTypeName(Type javaType) {
    if (!shortenFQNs) {
      return "`%s`".formatted(javaType.getTypeName());
    }
    String longName = javaType.getTypeName();
    String abbrName = longName.replaceAll("([a-z][a-zA-Z0-9_.]+)\\.([a-zA-Z0-9$]+)", "<abbr title=\"$0\">$2</abbr>");
    return "<code>%s</code>".formatted(abbrName);
  }
}
