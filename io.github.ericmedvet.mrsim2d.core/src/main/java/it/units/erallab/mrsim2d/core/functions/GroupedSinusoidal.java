package it.units.erallab.mrsim2d.core.functions;

import it.units.erallab.mrsim2d.core.util.DoubleRange;
import it.units.erallab.mrsim2d.core.util.Parametrized;

import java.util.List;

public class GroupedSinusoidal implements TimedRealFunction, Parametrized {

  private final static DoubleRange PARAM_RANGE = DoubleRange.SYMMETRIC_UNIT;
  private final double[] phases;
  private final double[] frequencies;
  private final double[] amplitudes;
  private final double[] biases;
  private final double[] sums;
  private final int nOfInputs;
  private final int nOfOutputs;
  private final List<Group> groups;

  public GroupedSinusoidal(int nOfInputs, List<Group> groups) {
    this.nOfInputs = nOfInputs;
    this.groups = groups;
    nOfOutputs = groups.stream().mapToInt(Group::size).sum();
    amplitudes = new double[groups.size()];
    frequencies = new double[groups.size()];
    phases = new double[groups.size()];
    biases = new double[groups.size()];
    sums = new double[groups.size()];
  }

  public record Group(
      int size,
      DoubleRange aRange,
      DoubleRange fRange,
      DoubleRange pRange,
      DoubleRange bRange,
      DoubleRange sRange
  ) {}

  @Override
  public double[] apply(double t, double[] input) {
    double[] outputs = new double[nOfOutputs];
    int oi = 0;
    for (int gi = 0; gi < groups.size(); gi = gi + 1) {
      double a = groups.get(gi).aRange.denormalize(PARAM_RANGE.normalize(amplitudes[gi]));
      double f = groups.get(gi).fRange.denormalize(PARAM_RANGE.normalize(frequencies[gi]));
      double p = groups.get(gi).pRange.denormalize(PARAM_RANGE.normalize(phases[gi]));
      double b = groups.get(gi).bRange.denormalize(PARAM_RANGE.normalize(biases[gi]));
      double s = groups.get(gi).sRange.denormalize(PARAM_RANGE.normalize(sums[gi]));
      double v = a * Math.sin(2d * Math.PI * f * t + p) + b;
      for (int li = 0; li < groups.get(gi).size(); li = li + 1) {
        outputs[oi] = (li % 2 == 0) ? (s / (double) groups.size() + v) : (s / (double) groups.size() - v);
        oi = oi + 1;
      }
    }
    return outputs;
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
    double[] params = new double[groups.size() * 5];
    System.arraycopy(amplitudes, 0, params, 0, groups.size());
    System.arraycopy(frequencies, 0, params, groups.size(), groups.size());
    System.arraycopy(phases, 0, params, groups.size() * 2, groups.size());
    System.arraycopy(biases, 0, params, groups.size() * 3, groups.size());
    System.arraycopy(sums, 0, params, groups.size() * 4, groups.size());
    return params;
  }

  @Override
  public void setParams(double[] params) {
    System.arraycopy(params, 0, amplitudes, 0, groups.size());
    System.arraycopy(params, groups.size(), frequencies, 0, groups.size());
    System.arraycopy(params, groups.size() * 2, phases, 0, groups.size());
    System.arraycopy(params, groups.size() * 3, biases, 0, groups.size());
    System.arraycopy(params, groups.size() * 4, sums, 0, groups.size());
  }
}
