
package io.github.ericmedvet.mrsim2d.core.util;

import java.util.List;
public class Utils {

  private Utils() {
  }

  public static double[] concat(double[] a1, double[] a2) {
    double[] a = new double[a1.length + a2.length];
    System.arraycopy(a1, 0, a, 0, a1.length);
    System.arraycopy(a2, 0, a, a1.length, a2.length);
    return a;
  }

  public static double[] concat(List<double[]> as) {
    int s = as.stream().mapToInt(a -> a.length).sum();
    double[] a = new double[s];
    int c = 0;
    for (double[] doubles : as) {
      System.arraycopy(doubles, 0, a, c, doubles.length);
      c = c + doubles.length;
    }
    return a;
  }

}
