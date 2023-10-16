
package io.github.ericmedvet.mrsim2d.core.bodies;
public interface SoftBody extends Body {
  double restArea();

  default double areaRatio() {
    return poly().area() / restArea();
  }
}
