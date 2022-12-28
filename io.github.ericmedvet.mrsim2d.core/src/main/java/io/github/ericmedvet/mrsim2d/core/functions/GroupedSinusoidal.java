package io.github.ericmedvet.mrsim2d.core.functions;

import io.github.ericmedvet.mrsim2d.core.util.DoubleRange;
import io.github.ericmedvet.mrsim2d.core.util.Parametrized;

import java.util.ArrayList;
import java.util.List;
import java.util.function.DoubleUnaryOperator;

public class GroupedSinusoidal implements TimedRealFunction, Parametrized {

  private final static DoubleRange PARAM_RANGE = DoubleRange.SYMMETRIC_UNIT;
  private final int nOfInputs;
  private final int nOfOutputs;
  private final List<GroupWithParams> groups;

  public GroupedSinusoidal(int nOfInputs, List<Group> groups) {
    this.nOfInputs = nOfInputs;
    this.groups = groups.stream()
        .map(g -> new GroupWithParams(g, 0d, 0d, 0d, 0d, 0d))
        .toList();
    nOfOutputs = groups.stream()
        .mapToInt(Group::size)
        .sum();
  }

  public record Group(
      int size,
      DoubleRange aRange,
      DoubleRange fRange,
      DoubleRange pRange,
      DoubleRange bRange,
      DoubleRange sRange
  ) {
  }

  private static final class GroupWithParams implements DoubleUnaryOperator, Parametrized {
    private final Group group;
    private double a;
    private double f;
    private double p;
    private double b;
    private double s;

    private GroupWithParams(Group group, double a, double f, double p, double b, double s) {
      this.group = group;
      this.a = a;
      this.f = f;
      this.p = p;
      this.b = b;
      this.s = s;
    }

    @Override
    public double applyAsDouble(double t) {
      double a = group.aRange.denormalize(PARAM_RANGE.normalize(this.a));
      double f = group.fRange.denormalize(PARAM_RANGE.normalize(this.f));
      double p = group.pRange.denormalize(PARAM_RANGE.normalize(this.p));
      double b = group.bRange.denormalize(PARAM_RANGE.normalize(this.b));
      return a * Math.sin(2d * Math.PI * f * t + p) + b;
    }

    @Override
    public double[] getParams() {
      List<Double> params = new ArrayList<>();
      if (group.aRange.extent() > 0) {
        params.add(a);
      }
      if (group.fRange.extent() > 0) {
        params.add(f);
      }
      if (group.pRange.extent() > 0) {
        params.add(p);
      }
      if (group.bRange.extent() > 0) {
        params.add(b);
      }
      if (group.sRange.extent() > 0) {
        params.add(s);
      }
      return params.stream().mapToDouble(d -> d).toArray();
    }

    @Override
    public void setParams(double[] params) {
      int c = 0;
      try {
        if (group.aRange.extent() > 0) {
          a = params[c];
          c = c + 1;
        }
        if (group.fRange.extent() > 0) {
          f = params[c];
          c = c + 1;
        }
        if (group.pRange.extent() > 0) {
          p = params[c];
          c = c + 1;
        }
        if (group.bRange.extent() > 0) {
          b = params[c];
          c = c + 1;
        }
        if (group.sRange.extent() > 0) {
          s = params[c];
          c = c + 1;
        }
      } catch (ArrayIndexOutOfBoundsException e) {
        throw new IllegalArgumentException("Wrong number of parameters");
      }
      if (c < params.length) {
        throw new IllegalArgumentException("Wrong number of parameters: %d found, %d expected".formatted(
            params.length,
            c
        ));
      }
    }

    public Group group() {
      return group;
    }

    @Override
    public String toString() {
      return "GroupWithParams[" +
          "group=" + group + ", " +
          "a=" + a + ", " +
          "f=" + f + ", " +
          "p=" + p + ", " +
          "b=" + b + ", " +
          "s=" + s + ']';
    }

  }

  @Override
  public double[] apply(double t, double[] input) {
    double[] outputs = new double[nOfOutputs];
    int oi = 0;
    for (GroupWithParams g : groups) {
      double v = g.applyAsDouble(t);
      double s = g.group.sRange.denormalize(PARAM_RANGE.normalize(g.s));
      for (int li = 0; li < g.group().size(); li = li + 1) {
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
    double[] params = new double[groups.stream().mapToInt(g -> g.getParams().length).sum()];
    int c = 0;
    for (GroupWithParams g : groups) {
      double[] groupParams = g.getParams();
      System.arraycopy(groupParams, 0, params, c, groupParams.length);
    }
    return params;
  }

  @Override
  public void setParams(double[] params) {
    int c = 0;
    try {
      for (GroupWithParams g : groups) {
        double[] groupParams = new double[g.getParams().length];
        System.arraycopy(params, c, groupParams, 0, groupParams.length);
        g.setParams(groupParams);
        c = c + groupParams.length;
      }
    } catch (ArrayIndexOutOfBoundsException e) {
      throw new IllegalArgumentException("Wrong number of parameters");
    }
    if (c < params.length) {
      throw new IllegalArgumentException("Wrong number of parameters: %d found, %d expected".formatted(
          params.length,
          c
      ));
    }
  }
}
