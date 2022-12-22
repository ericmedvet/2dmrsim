package it.units.erallab.mrsim2d.engine.dyn4j;

/**
 * @author "Eric Medvet" on 2022/09/19 for 2dmrsim
 */
public interface Actuable {
  void actuate(double t, double lastT);
}
