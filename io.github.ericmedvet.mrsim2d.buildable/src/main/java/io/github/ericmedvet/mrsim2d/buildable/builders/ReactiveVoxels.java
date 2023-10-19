/*-
 * ========================LICENSE_START=================================
 * mrsim2d-buildable
 * %%
 * Copyright (C) 2020 - 2023 Eric Medvet
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * =========================LICENSE_END==================================
 */
package io.github.ericmedvet.mrsim2d.buildable.builders;

import io.github.ericmedvet.jnb.core.Discoverable;
import io.github.ericmedvet.jnb.core.Param;
import io.github.ericmedvet.jsdynsym.core.StatelessSystem;
import io.github.ericmedvet.jsdynsym.core.numerical.NumericalDynamicalSystem;
import io.github.ericmedvet.jsdynsym.core.numerical.NumericalStatelessSystem;
import io.github.ericmedvet.mrsim2d.core.Sensor;
import io.github.ericmedvet.mrsim2d.core.actions.SenseAngle;
import io.github.ericmedvet.mrsim2d.core.actions.SenseDistanceToBody;
import io.github.ericmedvet.mrsim2d.core.actions.SenseSideCompression;
import io.github.ericmedvet.mrsim2d.core.agents.gridvsr.GridBody;
import io.github.ericmedvet.mrsim2d.core.agents.gridvsr.ReactiveGridVSR;
import io.github.ericmedvet.mrsim2d.core.bodies.Body;
import io.github.ericmedvet.mrsim2d.core.bodies.Voxel;
import io.github.ericmedvet.mrsim2d.core.util.Pair;
import java.util.List;
import java.util.function.BiFunction;

@Discoverable(prefixTemplate = "sim|s.agent|a.vsr.reactiveVoxel|rv")
public class ReactiveVoxels {

  public enum Action {
    EXPAND(-1d),
    CONTRACT(1d);
    private final double value;

    Action(double value) {
      this.value = value;
    }

    public double getValue() {
      return value;
    }
  }

  private static final NumericalDynamicalSystem<StatelessSystem.State> EMPTY_NDS =
      nss(0, (t, inputs) -> new double[4]);

  private ReactiveVoxels() {}

  @SuppressWarnings("unused")
  public static ReactiveGridVSR.ReactiveVoxel asin(@Param(value = "f", dD = 1.0) double f) {
    return new ReactiveGridVSR.ReactiveVoxel(
        new GridBody.Element(GridBody.VoxelType.SOFT, Voxel.DEFAULT_MATERIAL),
        List.of(),
        nss(0, (t, inputs) -> four(Math.sin(2d * Math.PI * f * t))));
  }

  @SuppressWarnings("unused")
  public static ReactiveGridVSR.ReactiveVoxel aa(
      @Param(value = "angle", dD = 0d) double a,
      @Param(value = "action", dS = "expand") Action action) {
    double aDeg = Math.toRadians(a);
    return new ReactiveGridVSR.ReactiveVoxel(
        new GridBody.Element(GridBody.VoxelType.SOFT, Voxel.DEFAULT_MATERIAL),
        List.of(SenseAngle::new),
        nss(
            1,
            (t, inputs) -> {
              Voxel.Side side = fromAngle(inputs[0] + aDeg);
              return new double[] {
                side.equals(Voxel.Side.N) ? action.getValue() : 0d,
                side.equals(Voxel.Side.E) ? action.getValue() : 0d,
                side.equals(Voxel.Side.S) ? action.getValue() : 0d,
                side.equals(Voxel.Side.W) ? action.getValue() : 0d
              };
            }));
  }

  private static Voxel.Side fromAngle(double a) {
    while (a > Math.PI) {
      a = a - Math.PI;
    }
    if (-Math.PI / 4d < a && a <= Math.PI / 4d) {
      return Voxel.Side.E;
    }
    if (Math.PI / 4d < a && a <= Math.PI * 3d / 4d) {
      return Voxel.Side.N;
    }
    if (-Math.PI * 3d / 4d < a && a <= -Math.PI / 4d) {
      return Voxel.Side.S;
    }
    return Voxel.Side.W;
  }

  @SuppressWarnings("unused")
  public static ReactiveGridVSR.ReactiveVoxel asld(
      @Param(value = "duration", dD = 0.2) double duration,
      @Param(value = "threshold", dD = 0.05) double threshold,
      @Param(value = "action", dS = "expand") Action action) {
    Sensor<? super Voxel> sn = v -> new SenseSideCompression(Voxel.Side.N, v);
    Sensor<? super Voxel> se = v -> new SenseSideCompression(Voxel.Side.E, v);
    Sensor<? super Voxel> ss = v -> new SenseSideCompression(Voxel.Side.S, v);
    Sensor<? super Voxel> sw = v -> new SenseSideCompression(Voxel.Side.W, v);
    @SuppressWarnings({"unchecked", "rawtypes"})
    List<Sensor<? super Body>> sensors =
        List.of((Sensor) sn, (Sensor) se, (Sensor) ss, (Sensor) sw);
    return new ReactiveGridVSR.ReactiveVoxel(
        new GridBody.Element(GridBody.VoxelType.SOFT, Voxel.DEFAULT_MATERIAL),
        sensors,
        new NumericalDynamicalSystem<Pair<Double, Double>>() {
          private double lastNST = Double.NEGATIVE_INFINITY;
          private double lastEWT = Double.NEGATIVE_INFINITY;

          @Override
          public int nOfInputs() {
            return 4;
          }

          @Override
          public int nOfOutputs() {
            return 4;
          }

          @Override
          public Pair<Double, Double> getState() {
            return null;
          }

          @Override
          public void reset() {
            lastNST = Double.NEGATIVE_INFINITY;
            lastEWT = Double.NEGATIVE_INFINITY;
          }

          @Override
          public double[] step(double t, double[] inputs) {
            // read sensors
            double nCompression = inputs[0];
            double eCompression = inputs[1];
            double sCompression = inputs[2];
            double wCompression = inputs[3];
            // check diffs
            if (Math.abs(nCompression - sCompression) > threshold) {
              lastNST = t;
            }
            if (Math.abs(eCompression - wCompression) > threshold) {
              lastEWT = t;
            }
            // apply
            return new double[] {
              t - lastNST < duration ? action.getValue() : 0d,
              t - lastEWT < duration ? action.getValue() : 0d,
              t - lastNST < duration ? action.getValue() : 0d,
              t - lastEWT < duration ? action.getValue() : 0d,
            };
          }
        });
  }

  @SuppressWarnings("unused")
  public static ReactiveGridVSR.ReactiveVoxel at(
      @Param(value = "duration", dD = 0.2) double duration,
      @Param(value = "range", dD = 1) double range,
      @Param(value = "side", dS = "s") Voxel.Side side,
      @Param(value = "action", dS = "expand") Action action) {
    return new ReactiveGridVSR.ReactiveVoxel(
        new GridBody.Element(GridBody.VoxelType.SOFT, Voxel.DEFAULT_MATERIAL),
        List.of(b -> new SenseDistanceToBody(side.getNormalAngle(), range, b)),
        new NumericalDynamicalSystem<Double>() {

          private double lastContactT = Double.NEGATIVE_INFINITY;

          @Override
          public int nOfInputs() {
            return 1;
          }

          @Override
          public int nOfOutputs() {
            return 4;
          }

          @Override
          public Double getState() {
            return lastContactT;
          }

          @Override
          public void reset() {
            lastContactT = Double.NEGATIVE_INFINITY;
          }

          @Override
          public double[] step(double t, double[] inputs) {
            if (inputs[0] < range) {
              lastContactT = t;
            }
            return (t - lastContactT < duration) ? four(action.getValue()) : four(0d);
          }
        });
  }

  private static NumericalStatelessSystem nss(
      int nOfInputs, BiFunction<Double, double[], double[]> f) {
    return new NumericalStatelessSystem() {
      @Override
      public int nOfInputs() {
        return nOfInputs;
      }

      @Override
      public int nOfOutputs() {
        return 4;
      }

      @Override
      public double[] step(double t, double[] inputs) {
        return f.apply(t, inputs);
      }
    };
  }

  private static double[] four(double value) {
    return new double[] {value, value, value, value};
  }

  @SuppressWarnings("unused")
  public static ReactiveGridVSR.ReactiveVoxel none() {
    return new ReactiveGridVSR.ReactiveVoxel(
        new GridBody.Element(GridBody.VoxelType.NONE, Voxel.DEFAULT_MATERIAL),
        List.of(),
        EMPTY_NDS);
  }

  @SuppressWarnings("unused")
  public static ReactiveGridVSR.ReactiveVoxel ph() {
    return new ReactiveGridVSR.ReactiveVoxel(
        new GridBody.Element(GridBody.VoxelType.RIGID, Voxel.DEFAULT_MATERIAL),
        List.of(),
        EMPTY_NDS);
  }

  @SuppressWarnings("unused")
  public static ReactiveGridVSR.ReactiveVoxel ps() {
    return new ReactiveGridVSR.ReactiveVoxel(
        new GridBody.Element(GridBody.VoxelType.SOFT, Voxel.DEFAULT_MATERIAL),
        List.of(),
        EMPTY_NDS);
  }
}
