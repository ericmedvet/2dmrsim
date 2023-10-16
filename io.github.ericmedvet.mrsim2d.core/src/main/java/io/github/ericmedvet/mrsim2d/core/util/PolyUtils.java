
package io.github.ericmedvet.mrsim2d.core.util;

import io.github.ericmedvet.mrsim2d.core.bodies.Anchor;
import io.github.ericmedvet.mrsim2d.core.geometry.*;
public class PolyUtils {

  private PolyUtils() {
  }

  private static double angle(Point a, Point b, Point c) {
    double angle = c.diff(b).direction() - a.diff(b).direction();
    return angle > 0 ? angle : (angle + 2d * Math.PI);
  }

  public static double distance(Point p, Point p1, Point p2) {
    p2 = p2.diff(p1);
    p = p.diff(p1);
    double a = p.angle(p2);
    double l = p.magnitude();
    if (a < Math.PI / 2d && a > -Math.PI / 2d) {
      if (Math.cos(a) * l < p2.magnitude()) {
        return Math.sin(a) * l;
      }
      return p2.sum(p1).distance(p);
    }
    return l;
  }

  public static double distance(Point p, Poly poly) {
    double minD = distance(p, poly.vertexes()[poly.vertexes().length - 1], poly.vertexes()[0]);
    for (int i = 0; i < poly.vertexes().length - 1; i++) {
      minD = Math.min(minD, distance(p, poly.vertexes()[i], poly.vertexes()[i + 1]));
    }
    return minD;
  }

  public static Poly makeCounterClockwise(Poly poly) {
    Point c = poly.center();
    double sum = 0d;
    for (int i = 1; i < poly.vertexes().length; i++) {
      sum = sum + angle(poly.vertexes()[i], c, poly.vertexes()[i - 1]);
    }
    sum = sum + angle(poly.vertexes()[0], c, poly.vertexes()[poly.vertexes().length - 1]);
    if (sum >= 0) {
      return poly;
    }
    Point[] vertexes = new Point[poly.vertexes().length];
    for (int i = 0; i < vertexes.length; i++) {
      vertexes[i] = poly.vertexes()[vertexes.length - 1 - i];
    }
    return new Poly(vertexes);
  }

  public static double maxYAtX(Poly poly, double x) {
    return poly.sides().stream()
        .mapToDouble(s -> yAtX(s, x))
        .filter(y -> !Double.isNaN(y))
        .max().orElse(Double.NaN);
  }

  public static double minAnchorDistance(Anchor anchor1, Anchor anchor2) {
    double d1 = PolyUtils.distance(anchor1.point(), anchor1.anchorable().poly());
    double d2 = PolyUtils.distance(anchor2.point(), anchor2.anchorable().poly());
    return d1 + d2;
  }

  public static double yAtX(Segment s, double x) {
    BoundingBox bb = s.boundingBox();
    if (x < bb.min().x() || x > bb.max().x()) {
      return Double.NaN;
    }
    return s.p1().y() + (s.p2().y() - s.p1().y()) * (x - s.p1().x()) / (s.p2().x() - s.p1().x());
  }

  public static Path zigZag(Point src, Point dst, int n, double w) {
    Point dL = dst.diff(src).scale(0.5d / (double) n);
    double a = dL.direction() + Math.PI / 2d;
    Point dP = new Point(a).scale(w);
    Path p = new Path(src)
        .moveBy(dL.diff(dP.scale(0.5)));
    for (int i = 0; i < n - 1; i++) {
      p = p
          .moveBy(dL.sum(dP))
          .moveBy(dL.diff(dP));
    }
    return p.moveBy(dL.sum(dP.scale(0.5)));
  }

}
