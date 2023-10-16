package io.github.ericmedvet.mrsim2d.engine.dyn4j;
public interface Actuable {
  void actuate(double t, double lastT);
}
