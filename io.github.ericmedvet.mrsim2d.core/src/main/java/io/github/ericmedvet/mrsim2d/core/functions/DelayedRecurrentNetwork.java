package io.github.ericmedvet.mrsim2d.core.functions;

import io.github.ericmedvet.mrsim2d.core.util.DoubleRange;
import io.github.ericmedvet.mrsim2d.core.util.Parametrized;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

/**
 * @author "Eric Medvet" on 2022/12/29 for 2dmrsim
 */
public class DelayedRecurrentNetwork implements TimedRealFunction, Parametrized {

  private final MultiLayerPerceptron.ActivationFunction activationFunction;
  private final int nOfInputs;
  private final int nOfOutputs;
  private final int nOfInnerNeurons;
  private final DoubleRange timeRange;
  private final double threshold;
  private final double timeResolution;
  private final Map<Coord, Connection> connections;
  private final double[] biases;

  private final double[] outValues;
  private final double[][] inValues;

  public DelayedRecurrentNetwork(
      MultiLayerPerceptron.ActivationFunction activationFunction,
      int nOfInputs,
      int nOfOutputs,
      int nOfInnerNeurons,
      DoubleRange timeRange,
      double threshold,
      double timeResolution
  ) {
    this.activationFunction = activationFunction;
    this.nOfInputs = nOfInputs;
    this.nOfOutputs = nOfOutputs;
    this.nOfInnerNeurons = nOfInnerNeurons;
    this.timeRange = timeRange;
    this.threshold = threshold;
    this.timeResolution = timeResolution;
    connections = new HashMap<>();
    int nOfNeurons = nOfInputs + nOfOutputs + nOfInnerNeurons;
    biases = new double[nOfNeurons];
    outValues = new double[nOfNeurons];
    inValues = new double[nOfNeurons][];
    for (int fromI = 0; fromI < nOfNeurons; fromI = fromI + 1) {
      inValues[fromI] = new double[(int) Math.ceil(timeRange.max() / timeResolution)];
      for (int toI = 0; toI < nOfNeurons; toI = toI + 1) {
        connections.put(new Coord(fromI, toI), new Connection(0, 0, 0));
      }
    }
  }

  private record Connection(double weight, double delay, double duration) {}

  private record Coord(int fromId, int toId) {}

  public static void main(String[] args) {
    DelayedRecurrentNetwork drn = new DelayedRecurrentNetwork(
        MultiLayerPerceptron.ActivationFunction.TANH,
        1,
        1,
        1,
        new DoubleRange(0.25,0.35),
        0.1,
        0.1
    );
    drn.randomize(new Random(), DoubleRange.SYMMETRIC_UNIT);
    for (double t = 0; t < 2; t = t + 0.1) {
      System.out.println(drn.apply(t, new double[]{1})[0]);
    }
  }

  @Override
  public double[] apply(double t, double[] input) {
    //compute current time index
    int currentTI = timeIndex(t);
    //add inputs
    for (int i = 0; i < nOfInputs; i = i + 1) {
      if (Math.abs(input[i]) > threshold) {
        inValues[i][currentTI] = inValues[i][currentTI] + input[i];
      }
    }
    //compute neuron values
    int nOfNeurons = nOfInputs + nOfOutputs + nOfInnerNeurons;
    for (int i = 0; i < nOfNeurons; i = i + 1) {
      outValues[i] = activationFunction.apply(biases[i] + inValues[i][currentTI]);
    }
    //generate new pulses
    for (int fromI = 0; fromI < nOfNeurons; fromI = fromI + 1) {
      for (int toI = 0; toI < nOfNeurons; toI = toI + 1) {
        Connection connection = connections.get(new Coord(fromI, toI));
        double pulseValue = outValues[fromI] * connection.weight();
        if (Math.abs(pulseValue) > threshold) {
          double delay = timeRange
              .denormalize(DoubleRange.SYMMETRIC_UNIT.normalize(connection.delay()));
          double duration = new DoubleRange(delay, timeRange.max())
              .denormalize(DoubleRange.SYMMETRIC_UNIT.normalize(connection.duration()));
          for (double futureT = t + delay; futureT <= t + delay + duration; futureT = futureT + timeResolution) {
            int futureTI = timeIndex(futureT);
            inValues[toI][futureTI] = inValues[toI][futureTI] + pulseValue;
          }
        }
      }
    }
    //clear previous index
    int previousTI = currentTI - 1;
    if (previousTI < 0) {
      previousTI = timeIndex(timeRange.max());
    }
    for (int i = 0; i < nOfInputs; i = i + 1) {
      inValues[i][previousTI] = 0;
    }
    //read outputs
    double[] outputs = new double[nOfOutputs];
    System.arraycopy(outValues, nOfInputs + nOfInnerNeurons, outputs, 0, outputs.length);
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
    int nOfNeurons = nOfInputs + nOfOutputs + nOfInnerNeurons;
    double[] params = new double[3 * nOfNeurons * nOfNeurons + nOfNeurons];
    int c = 0;
    for (int i = 0; i < nOfNeurons; i = i + 1) {
      params[c] = biases[i];
      c = c + 1;
    }
    for (int fromI = 0; fromI < nOfNeurons; fromI = fromI + 1) {
      for (int toI = 0; toI < nOfNeurons; toI = toI + 1) {
        Connection connection = connections.get(new Coord(fromI, toI));
        params[c] = connection.weight();
        params[c + 1] = connection.delay();
        params[c + 2] = connection.duration();
        c = c + 3;
      }
    }
    return params;
  }

  @Override
  public void setParams(double[] params) {
    int nOfNeurons = nOfInputs + nOfOutputs + nOfInnerNeurons;
    if (params.length != 3 * nOfNeurons * nOfNeurons + nOfNeurons) {
      throw new IllegalArgumentException("Wrong number of parameters: %d found, %d expected".formatted(
          params.length,
          3 * nOfNeurons * nOfNeurons + nOfNeurons
      ));
    }
    int c = 0;
    for (int i = 0; i < nOfNeurons; i = i + 1) {
      biases[i] = params[c];
      c = c + 1;
    }
    for (int fromI = 0; fromI < nOfNeurons; fromI = fromI + 1) {
      for (int toI = 0; toI < nOfNeurons; toI = toI + 1) {
        connections.put(new Coord(fromI, toI), new Connection(params[c], params[c + 1], params[c + 2]));
        c = c + 3;
      }
    }
  }

  private int timeIndex(double t) {
    return (int) Math.floor((t % timeRange.max()) / timeResolution);
  }
}
