package it.units.erallab.mrsim2d.core.functions;

import it.units.erallab.mrsim2d.core.util.Parametrized;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.IntStream;

/**
 * @author "Eric Medvet" on 2022/10/03 for 2dmrsim
 */
public class Sinusoidal implements TimedRealFunction, Parametrized {

  private final int nOfInputs;
  private final int nOfOutputs;
  private final double[] phases;
  private final double[] frequencies;
  private final double[] amplitudes;
  private final Set<Type> types;

  public Sinusoidal(int nOfInputs, int nOfOutputs, Set<Type> types) {
    this.nOfInputs = nOfInputs;
    this.nOfOutputs = nOfOutputs;
    this.types = types;
    phases = new double[nOfOutputs];
    frequencies = new double[nOfOutputs];
    amplitudes = new double[nOfOutputs];
  }

  public enum Type {PHASE, FREQUENCY, AMPLITUDE}

  private static double[] nCopies(double value, int n) {
    double[] values = new double[n];
    Arrays.fill(values, value);
    return values;
  }

  @Override
  public double[] apply(double t, double[] input) {
    return IntStream.range(0, nOfOutputs)
        .mapToDouble(i -> amplitudes[i] * Math.sin(2d * Math.PI * frequencies[i] * t + phases[i]))
        .toArray();
  }

  @Override
  public int nOfInputs() {
    return nOfInputs;
  }

  @Override
  public int nOfOutputs() {
    return nOfOutputs;
  }

  @Override
  public double[] getParams() {
    double[] params = new double[nOfOutputs * types.size()];
    int i = 0;
    if (types.contains(Type.PHASE)) {
      System.arraycopy(phases, 0, params, i, nOfOutputs);
      i = i + nOfOutputs;
    }
    if (types.contains(Type.FREQUENCY)) {
      System.arraycopy(frequencies, 0, params, i, nOfOutputs);
      i = i + nOfOutputs;
    }
    if (types.contains(Type.AMPLITUDE)) {
      System.arraycopy(amplitudes, 0, params, i, nOfOutputs);
    }
    return params;
  }

  @Override
  public void setParams(double[] params) {
    if (params.length != (nOfOutputs * types.size())) {
      throw new IllegalArgumentException("Params size is wrong: %d expected, %d found".formatted(
          nOfOutputs * types.size(),
          params.length
      ));
    }
    int i = 0;
    if (types.contains(Type.PHASE)) {
      System.arraycopy(params, i, phases, 0, nOfOutputs);
      i = i + nOfOutputs;
    }
    if (types.contains(Type.FREQUENCY)) {
      System.arraycopy(params, i, frequencies, 0, nOfOutputs);
      i = i + nOfOutputs;
    }
    if (types.contains(Type.AMPLITUDE)) {
      System.arraycopy(params, i, amplitudes, 0, nOfOutputs);
    }
  }

  public void setAmplitudes(double amplitude) {
    setAmplitudes(nCopies(amplitude, nOfOutputs));
  }

  public void setAmplitudes(double[] amplitudes) {
    if (amplitudes.length != nOfOutputs) {
      throw new IllegalArgumentException("Amplitudes size is wrong: %d expected, %d found".formatted(
          nOfOutputs,
          phases.length
      ));
    }
    System.arraycopy(amplitudes, 0, this.amplitudes, 0, nOfOutputs);
  }

  public void setFrequencies(double frequency) {
    setFrequencies(nCopies(frequency, nOfOutputs));
  }

  public void setFrequencies(double[] frequencies) {
    if (frequencies.length != nOfOutputs) {
      throw new IllegalArgumentException("Frequencies size is wrong: %d expected, %d found".formatted(
          nOfOutputs,
          frequencies.length
      ));
    }
    System.arraycopy(frequencies, 0, this.frequencies, 0, nOfOutputs);
  }

  public void setPhases(double phase) {
    setPhases(nCopies(phase, nOfOutputs));
  }

  public void setPhases(double[] phases) {
    if (phases.length != nOfOutputs) {
      throw new IllegalArgumentException("Phases size is wrong: %d expected, %d found".formatted(
          nOfOutputs,
          phases.length
      ));
    }
    System.arraycopy(phases, 0, this.phases, 0, nOfOutputs);
  }
}
