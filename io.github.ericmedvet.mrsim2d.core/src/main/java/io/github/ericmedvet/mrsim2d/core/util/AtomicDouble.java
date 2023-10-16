
package io.github.ericmedvet.mrsim2d.core.util;
public class AtomicDouble {
  private double value;

  public AtomicDouble(double value) {
    this.value = value;
  }

  public void add(double addValue) {
    this.value = value + addValue;
  }

  public double get() {
    return value;
  }

  public void set(double value) {
    this.value = value;
  }

  @Override
  public String toString() {
    return Double.toString(value);
  }
}
