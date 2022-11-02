package it.units.erallab.mrsim2d.builder;

import java.io.PrintStream;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * @author "Eric Medvet" on 2022/11/02 for 2dmrsim
 */
public class InfoPrinter {

  private static final int PACKAGE_HEADING_LEVEL = 2;
  private static final int BUILDER_HEADING_LEVEL = PACKAGE_HEADING_LEVEL + 1;
  private final int packageHeadingLevel = PACKAGE_HEADING_LEVEL;
  private final int builderHeadingLevel = BUILDER_HEADING_LEVEL;

  record BuilderInfo(SortedSet<Name> names, Builder<?> builder) {
    public Name longestName() {
      return names().stream()
          .min(Comparator.comparingInt(n -> n.fullName().length()))
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

  private static String heading(int level) {
    return String.join("", Collections.nCopies(level, "#"));
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
        aliasesMap.put(new Name(fullName), new BuilderInfo(new TreeSet<>(), builder));
        mainName = new Name(fullName);
      }
      aliasesMap.get(mainName).names.add(new Name(fullName));
    });
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
      ps.printf("%s Package `%s`%n", heading(packageHeadingLevel), packageName);
      ps.println();
      ps.print("Aliases: ");
      ps.println(packageInfo.names().stream().map("`%s'"::formatted).collect(Collectors.joining(", ")));
      ps.println();
      for (BuilderInfo builderInfo : packageInfo.builderInfos()) {
        ps.printf("%s Builder `%s`%n", heading(builderHeadingLevel), builderInfo.names().first().simpleName());
        ps.println();
        if (builderInfo.builder() instanceof DocumentedBuilder<?> documentedBuilder) {
          if (documentedBuilder.params().isEmpty()) {
            ps.printf("`%s()` gives `%s`%n", builderInfo.shortestName().fullName(), documentedBuilder.builtType());
          } else {
            ps.printf("`%s(`%n", builderInfo.shortestName().fullName());
            for (DocumentedBuilder.ParamInfo paramInfo : documentedBuilder.params()) {
              if (!paramInfo.self()) {
                ps.printf("`  %s` %s", paramInfo.name(), paramInfo.type().rendered());
                if (paramInfo.defaultValue()!=null) {
                  ps.printf(" (with default `%s`)", paramInfo.defaultValue());
                }
                ps.printf(" takes `%s`", paramInfo.parameter().getParameterizedType().getTypeName());
                ps.println();
              }
            }
            ps.printf("`)` gives `%s`%n", documentedBuilder.builtType());
          }
        } else {
          ps.printf("`%s()` (no more info available)%n", builderInfo.shortestName().fullName());
        }
        ps.println();
      }
    }
  }
}
