/*
 * Copyright 2022 Eric Medvet <eric.medvet@gmail.com> (as eric)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package it.units.erallab.mrsim2d.core.functions;

import it.units.erallab.mrsim2d.core.util.DoubleRange;
import it.units.erallab.mrsim2d.core.util.Parametrized;

import java.util.Arrays;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @author eric
 */
public class MultiLayerPerceptron implements RealFunction, Parametrized {

  protected final ActivationFunction activationFunction;
  protected final double[][][] weights;
  protected final int[] neurons;
  protected final double[][] activationValues;

  public MultiLayerPerceptron(
      ActivationFunction activationFunction,
      double[][][] weights,
      int[] neurons
  ) {
    this.activationFunction = activationFunction;
    this.weights = weights;
    this.neurons = neurons;
    activationValues = new double[neurons.length][];
    if (flat(weights, neurons).length != countWeights(neurons)) {
      throw new IllegalArgumentException(String.format(
          "Wrong number of weights: %d expected, %d found",
          countWeights(neurons),
          flat(weights, neurons).length
      ));
    }
  }

  public MultiLayerPerceptron(
      ActivationFunction activationFunction,
      int nOfInput,
      int[] innerNeurons,
      int nOfOutput,
      double[] weights
  ) {
    this(
        activationFunction,
        unflat(weights, countNeurons(nOfInput, innerNeurons, nOfOutput)),
        countNeurons(nOfInput, innerNeurons, nOfOutput)
    );
  }

  public MultiLayerPerceptron(ActivationFunction activationFunction, int nOfInput, int[] innerNeurons, int nOfOutput) {
    this(
        activationFunction,
        nOfInput,
        innerNeurons,
        nOfOutput,
        new double[countWeights(countNeurons(nOfInput, innerNeurons, nOfOutput))]
    );
  }

  public enum ActivationFunction implements Function<Double, Double> {
    RELU(x -> (x < 0) ? 0d : x, new DoubleRange(0d, Double.POSITIVE_INFINITY)),
    SIGMOID(x -> 1d / (1d + Math.exp(-x)), DoubleRange.UNIT),
    SIN(Math::sin, DoubleRange.SYMMETRIC_UNIT),
    TANH(Math::tanh, DoubleRange.SYMMETRIC_UNIT),
    SIGN(Math::signum, DoubleRange.SYMMETRIC_UNIT),
    IDENTITY(x -> x, DoubleRange.UNBOUNDED);

    private final Function<Double, Double> f;
    private final DoubleRange domain;

    ActivationFunction(Function<Double, Double> f, DoubleRange domain) {
      this.f = f;
      this.domain = domain;
    }

    public Double apply(Double x) {
      return f.apply(x);
    }

    public DoubleRange getDomain() {
      return domain;
    }

    public Function<Double, Double> getF() {
      return f;
    }
  }

  public static int[] countNeurons(int nOfInput, int[] innerNeurons, int nOfOutput) {
    final int[] neurons;
    neurons = new int[2 + innerNeurons.length];
    System.arraycopy(innerNeurons, 0, neurons, 1, innerNeurons.length);
    neurons[0] = nOfInput;
    neurons[neurons.length - 1] = nOfOutput;
    return neurons;
  }

  public static int countWeights(int[] neurons) {
    int c = 0;
    for (int i = 1; i < neurons.length; i++) {
      c = c + neurons[i] * (neurons[i - 1] + 1);
    }
    return c;
  }

  public static int countWeights(int nOfInput, int[] innerNeurons, int nOfOutput) {
    return countWeights(countNeurons(nOfInput, innerNeurons, nOfOutput));
  }

  public static double[] flat(double[][][] unflatWeights, int[] neurons) {
    double[] flatWeights = new double[countWeights(neurons)];
    int c = 0;
    for (int i = 1; i < neurons.length; i++) {
      for (int j = 0; j < neurons[i]; j++) {
        for (int k = 0; k < neurons[i - 1] + 1; k++) {
          flatWeights[c] = unflatWeights[i - 1][j][k];
          c = c + 1;
        }
      }
    }
    return flatWeights;
  }

  public static double[][][] unflat(double[] flatWeights, int[] neurons) {
    double[][][] unflatWeights = new double[neurons.length - 1][][];
    int c = 0;
    for (int i = 1; i < neurons.length; i++) {
      unflatWeights[i - 1] = new double[neurons[i]][neurons[i - 1] + 1];
      for (int j = 0; j < neurons[i]; j++) {
        for (int k = 0; k < neurons[i - 1] + 1; k++) {
          unflatWeights[i - 1][j][k] = flatWeights[c];
          c = c + 1;
        }
      }
    }
    return unflatWeights;
  }

  @Override
  public double[] apply(double[] input) {
    if (input.length != neurons[0]) {
      throw new IllegalArgumentException(String.format(
          "Expected input length is %d: found %d",
          neurons[0],
          input.length
      ));
    }
    activationValues[0] = Arrays.stream(input).map(activationFunction.f::apply).toArray();
    for (int i = 1; i < neurons.length; i++) {
      activationValues[i] = new double[neurons[i]];
      for (int j = 0; j < neurons[i]; j++) {
        double sum = weights[i - 1][j][0]; //set the bias
        for (int k = 1; k < neurons[i - 1] + 1; k++) {
          sum = sum + activationValues[i - 1][k - 1] * weights[i - 1][j][k];
        }
        activationValues[i][j] = activationFunction.apply(sum);
      }
    }
    return activationValues[neurons.length - 1];
  }

  public double[][] getActivationValues() {
    return activationValues;
  }

  public int[] getNeurons() {
    return neurons;
  }

  @Override
  public double[] getParams() {
    return MultiLayerPerceptron.flat(weights, neurons);
  }

  @Override
  public void setParams(double[] params) {
    double[][][] newWeights = MultiLayerPerceptron.unflat(params, neurons);
    for (int l = 0; l < newWeights.length; l++) {
      for (int s = 0; s < newWeights[l].length; s++) {
        System.arraycopy(newWeights[l][s], 0, weights[l][s], 0, newWeights[l][s].length);
      }
    }
  }

  public double[][][] getWeights() {
    return weights;
  }

  @Override
  public int hashCode() {
    int hash = 5;
    hash = 67 * hash + Objects.hashCode(this.activationFunction);
    hash = 67 * hash + Arrays.deepHashCode(this.weights);
    hash = 67 * hash + Arrays.hashCode(this.neurons);
    return hash;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null) {
      return false;
    }
    if (getClass() != obj.getClass()) {
      return false;
    }
    final MultiLayerPerceptron other = (MultiLayerPerceptron) obj;
    if (this.activationFunction != other.activationFunction) {
      return false;
    }
    if (!Arrays.deepEquals(this.weights, other.weights)) {
      return false;
    }
    return Arrays.equals(this.neurons, other.neurons);
  }

  @Override
  public String toString() {
    return "MLP." + activationFunction.toString().toLowerCase() + "[" +
        Arrays.stream(neurons).mapToObj(Integer::toString).collect(Collectors.joining(","))
        + "]";
  }

  @Override
  public int nOfInputs() {
    return neurons[0];
  }

  @Override
  public int nOfOutputs() {
    return neurons[neurons.length - 1];
  }

  public String weightsString() {
    StringBuilder sb = new StringBuilder();
    for (int i = 1; i < neurons.length; i++) {
      for (int j = 0; j < neurons[i]; j++) {
        sb.append("->(").append(i).append(",").append(j).append("):");
        for (int k = 0; k < neurons[i - 1] + 1; k++) {
          sb.append(String.format(" %+5.3f", weights[i - 1][j][k]));
        }
        sb.append("\n");
      }
    }
    return sb.toString();
  }

}
