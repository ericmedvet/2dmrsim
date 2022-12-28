package io.github.ericmedvet.mrsim2d.core.functions;

import io.github.ericmedvet.mrsim2d.core.util.DoubleRange;
import io.github.ericmedvet.mrsim2d.core.util.Parametrized;

import java.util.Arrays;
import java.util.stream.IntStream;

/**
 * @author "Eric Medvet" on 2022/10/03 for 2dmrsim
 */
public class Sinusoidal implements TimedRealFunction, Parametrized {

  private final static DoubleRange PARAM_RANGE = DoubleRange.SYMMETRIC_UNIT;

  private final int nOfInputs;
  private final int nOfOutputs;
  private final double[] phases;
  private final double[] frequencies;
  private final double[] amplitudes;
  private final double[] biases;
  private final DoubleRange phaseRange;
  private final DoubleRange frequencyRange;
  private final DoubleRange amplitudeRange;
  private final DoubleRange biasRange;

  public Sinusoidal(
      int nOfInputs, int nOfOutputs,
      DoubleRange phaseRange,
      DoubleRange frequencyRange,
      DoubleRange amplitudeRange,
      DoubleRange biasRange
  ) {
    this.nOfInputs = nOfInputs;
    this.nOfOutputs = nOfOutputs;
    this.phaseRange = phaseRange;
    this.frequencyRange = frequencyRange;
    this.amplitudeRange = amplitudeRange;
    this.biasRange = biasRange;
    phases = new double[nOfOutputs];
    frequencies = new double[nOfOutputs];
    amplitudes = new double[nOfOutputs];
    biases = new double[nOfOutputs];
  }

  public enum Type {PHASE, FREQUENCY, AMPLITUDE, BIAS}

  private static double[] nCopies(double value, int n) {
    double[] values = new double[n];
    Arrays.fill(values, value);
    return values;
  }

  @Override
  public double[] apply(double t, double[] input) {
    return IntStream.range(0, nOfOutputs)
        .mapToDouble(i -> {
          double a = amplitudeRange.denormalize(PARAM_RANGE.normalize(amplitudes[i]));
          double p = phaseRange.denormalize(PARAM_RANGE.normalize(phases[i]));
          double f = frequencyRange.denormalize(PARAM_RANGE.normalize(frequencies[i]));
          double b = biasRange.denormalize(PARAM_RANGE.normalize(biases[i]));
          return a * Math.sin(2d * Math.PI * f * t + p) + b;
        })
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
    double[] params = new double[nOfOutputs * nOfTypes()];
    int i = 0;
    if (phaseRange.extent() > 0) {
      System.arraycopy(phases, 0, params, i, nOfOutputs);
      i = i + nOfOutputs;
    }
    if (frequencyRange.extent() > 0) {
      System.arraycopy(frequencies, 0, params, i, nOfOutputs);
      i = i + nOfOutputs;
    }
    if (amplitudeRange.extent() > 0) {
      System.arraycopy(amplitudes, 0, params, i, nOfOutputs);
      i = i + nOfOutputs;
    }
    if (biasRange.extent() > 0) {
      System.arraycopy(biases, 0, params, i, nOfOutputs);
    }
    return params;
  }

  @Override
  public void setParams(double[] params) {
    if (params.length != (nOfOutputs * nOfTypes())) {
      throw new IllegalArgumentException("Params size is wrong: %d expected, %d found".formatted(
          nOfOutputs * nOfTypes(),
          params.length
      ));
    }
    int i = 0;
    if (phaseRange.extent() > 0) {
      System.arraycopy(params, i, phases, 0, nOfOutputs);
      i = i + nOfOutputs;
    }
    if (frequencyRange.extent() > 0) {
      System.arraycopy(params, i, frequencies, 0, nOfOutputs);
      i = i + nOfOutputs;
    }
    if (amplitudeRange.extent() > 0) {
      System.arraycopy(params, i, amplitudes, 0, nOfOutputs);
      i = i + nOfOutputs;
    }
    if (biasRange.extent() > 0) {
      System.arraycopy(params, i, biases, 0, nOfOutputs);
    }
  }

  private int nOfTypes() {
    int n = 0;
    n = n + ((phaseRange.extent() > 0) ? 1 : 0);
    n = n + ((frequencyRange.extent() > 0) ? 1 : 0);
    n = n + ((amplitudeRange.extent() > 0) ? 1 : 0);
    n = n + ((biasRange.extent() > 0) ? 1 : 0);
    return n;
  }

  public void setAmplitudes(double amplitude) {
    setAmplitudes(nCopies(amplitude, nOfOutputs));
  }

  public void setAmplitudes(double[] amplitudes) {
    if (amplitudes.length != nOfOutputs) {
      throw new IllegalArgumentException("Amplitudes size is wrong: %d expected, %d found".formatted(
          nOfOutputs,
          amplitudes.length
      ));
    }
    System.arraycopy(amplitudes, 0, this.amplitudes, 0, nOfOutputs);
  }

  public void setBiases(double[] biases) {
    if (biases.length != nOfOutputs) {
      throw new IllegalArgumentException("Biases size is wrong: %d expected, %d found".formatted(
          nOfOutputs,
          biases.length
      ));
    }
    System.arraycopy(biases, 0, this.biases, 0, nOfOutputs);
  }

  public void setBiases(double bias) {
    setBiases(nCopies(bias, nOfOutputs));
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
