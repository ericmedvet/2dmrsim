package it.units.erallab.mrsim2d.core.functions;

import java.util.Collection;
import java.util.EnumSet;
import java.util.SortedMap;
import java.util.TreeMap;

/**
 * @author "Eric Medvet" on 2022/10/07 for 2dmrsim
 */
public class DiffInputTRF extends CompositeTRF {
  private final double windowT;
  private final EnumSet<Type> types;
  private final SortedMap<Double, double[]> memory;

  public DiffInputTRF(TimedRealFunction innerF, double windowT, Collection<Type> types) {
    super(innerF);
    if (innerF.nOfInputs() % types.size() != 0) {
      throw new IllegalArgumentException(
          "Cannot build function with %d aggregate types (%s), because inner function input size is wrong (%d)".formatted(
              types.size(),
              types,
              innerF.nOfInputs()
          ));
    }
    this.windowT = windowT;
    this.types = EnumSet.copyOf(types);
    this.memory = new TreeMap<>();
  }

  public enum Type {CURRENT, TREND, AVG}

  @Override
  public double[] apply(double t, double[] input) {
    //add new sample to memory
    memory.put(t, input);
    //update memory
    memory.keySet().stream()
        .filter(mt -> mt < t - windowT)
        .toList()
        .forEach(memory.keySet()::remove);
    //build inner input
    double[] iInput = new double[innerF.nOfInputs()];
    double[] firstInput = memory.get(memory.firstKey());
    double firstT = memory.firstKey();
    int c = 0;
    for (Type type : types) {
      if (type.equals(Type.CURRENT)) {
        System.arraycopy(input, 0, iInput, c, input.length);
        c = c + input.length;
      } else if (type.equals(Type.TREND)) {
        double[] lInput = new double[input.length];
        double dT = t - firstT;
        for (int i = 0; i < input.length; i = i + 1) {
          lInput[i] = (input[i] - firstInput[i]) / dT;
        }
        System.arraycopy(lInput, 0, iInput, c, input.length);
        c = c + input.length;
      } else if (type.equals(Type.AVG)) {
        double[] lInput = new double[input.length];
        for (int i = 0; i < input.length; i = i + 1) {
          lInput[i] = (input[i] + firstInput[i]) / 2d;
        }
        System.arraycopy(lInput, 0, iInput, c, input.length);
        c = c + input.length;
      }
    }
    return innerF.apply(t, iInput);
  }

  @Override
  public int nOfInputs() {
    return innerF.nOfInputs() / types.size();
  }

  @Override
  public int nOfOutputs() {
    return innerF.nOfOutputs();
  }

  @Override
  public String toString() {
    return "InputDiffTRF{" +
        "windowT=" + windowT +
        ", types=" + types +
        '}';
  }
}
