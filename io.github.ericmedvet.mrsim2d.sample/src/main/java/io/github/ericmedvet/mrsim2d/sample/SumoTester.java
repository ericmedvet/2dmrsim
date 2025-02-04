/*-
 * ========================LICENSE_START=================================
 * mrsim2d-sample
 * %%
 * Copyright (C) 2020 - 2025 Eric Medvet
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
package io.github.ericmedvet.mrsim2d.sample;

import io.github.ericmedvet.jnb.core.NamedBuilder;
import io.github.ericmedvet.jnb.datastructure.NumericalParametrized;
import io.github.ericmedvet.jsdynsym.core.composed.Composed;
import io.github.ericmedvet.mrsim2d.core.EmbodiedAgent;
import io.github.ericmedvet.mrsim2d.core.NumMultiBrained;
import io.github.ericmedvet.mrsim2d.core.engine.Engine;
import io.github.ericmedvet.mrsim2d.core.tasks.sumo.Sumo;
import io.github.ericmedvet.mrsim2d.viewer.Drawer;
import io.github.ericmedvet.mrsim2d.viewer.RealtimeViewer;

import java.util.Random;
import java.util.ServiceLoader;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.random.RandomGenerator;
import java.util.stream.IntStream;

public class SumoTester {

  private static final String FIXED_DRAWER = """
      sim.drawer(framer = sim.staticFramer(minX = 14.0; maxX = 31.0; minY = 13.0; maxY = 17.0); actions = true)
      """;

  private static final String DRAWER = "sim.drawer(actions = true)";

  private static final String CENTRALIZED_BIPED = """
      s.a.centralizedNumGridVSR(
        body = s.a.vsr.gridBody(
          shape = s.a.vsr.s.free(s="rss-sss-s.r");
          sensorizingFunction = s.a.vsr.sf.directional(
            headSensors = [s.s.d(a = -15; r = 5)]
          )
        );
        function = ds.num.sin()
      )
      """;

  private static final String DISTRIBUTED_BIPED = """
      s.a.distributedNumGridVSR(
        body = s.a.vsr.gridBody(
          shape = s.a.vsr.s.free(s="rsss-ssss-s..r");
          sensorizingFunction = s.a.vsr.sf.directional(
            headSensors = [s.s.d(a = -15; r = 5); s.s.sin()];
            sSensors = [s.s.d(a = -90; r = 1)];
            sensors = []
          )
        );
        nOfSignals = 1;
        directional = true;
        function = ds.num.mlp()
      )
      """;

  public static void main(String[] args) {
    RandomGenerator rg = new Random(7);
    double[] rndValues = IntStream.range(0, 100).mapToDouble(i -> 5 * rg.nextGaussian()).toArray();
    NamedBuilder<?> nb = NamedBuilder.fromDiscovery();
    @SuppressWarnings("unchecked") Drawer drawer = ((Function<String, Drawer>) nb.build(DRAWER)).apply("test");
    Sumo sumo = new Sumo(30);
    Supplier<Engine> engineSupplier = () -> ServiceLoader.load(Engine.class).findFirst().orElseThrow();
    Supplier<EmbodiedAgent> eas1 = () -> reparametrize(
        (EmbodiedAgent) nb.build(DISTRIBUTED_BIPED),
        i -> rndValues[i % rndValues.length]
    );
    sumo.run(eas1, eas1, engineSupplier.get(), new RealtimeViewer(30, drawer));
  }

  private static EmbodiedAgent reparametrize(EmbodiedAgent agent, Function<Integer, Double> f) {
    if (agent instanceof NumMultiBrained numMultiBrained) {
      numMultiBrained.brains()
          .stream()
          .map(b -> Composed.shallowest(b, NumericalParametrized.class))
          .forEach(o -> o.ifPresent(v -> {
            NumericalParametrized<?> np = (NumericalParametrized<?>) v;
            np.setParams(
                IntStream.range(0, np.getParams().length)
                    .mapToDouble(f::apply)
                    .toArray()
            );
          }));
    }
    return agent;
  }
}
