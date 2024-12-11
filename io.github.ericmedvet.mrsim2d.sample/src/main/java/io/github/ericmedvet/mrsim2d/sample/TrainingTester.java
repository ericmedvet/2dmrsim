/*-
 * ========================LICENSE_START=================================
 * mrsim2d-sample
 * %%
 * Copyright (C) 2020 - 2024 Eric Medvet
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
import io.github.ericmedvet.jnb.datastructure.DoubleRange;
import io.github.ericmedvet.jnb.datastructure.NumericalParametrized;
import io.github.ericmedvet.jsdynsym.core.composed.Composed;
import io.github.ericmedvet.mrsim2d.core.EmbodiedAgent;
import io.github.ericmedvet.mrsim2d.core.NumMultiBrained;
import io.github.ericmedvet.mrsim2d.core.Snapshot;
import io.github.ericmedvet.mrsim2d.core.engine.Engine;
import io.github.ericmedvet.mrsim2d.core.geometry.Terrain;
import io.github.ericmedvet.mrsim2d.core.tasks.trainingsumo.TrainingSumo;
import io.github.ericmedvet.mrsim2d.viewer.Drawer;
import io.github.ericmedvet.mrsim2d.viewer.RealtimeViewer;
import java.io.*;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;
import java.util.List;
import java.util.Random;
import java.util.ServiceLoader;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.logging.Logger;
import java.util.random.RandomGenerator;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class TrainingTester {

  private static final Logger L = Logger.getLogger(TrainingTester.class.getName());

  private static Object fromBase64(String content) throws IOException {
    try (ByteArrayInputStream bais =
            new ByteArrayInputStream(Base64.getDecoder().decode(content));
        ObjectInputStream ois = new ObjectInputStream(bais)) {
      return ois.readObject();
    } catch (Throwable t) {
      throw new IOException(t);
    }
  }

  public static void main(String[] args) {
    NamedBuilder<Object> nb = NamedBuilder.fromDiscovery();
    // prepare engine
    Supplier<Engine> engineSupplier =
        () -> ServiceLoader.load(Engine.class).findFirst().orElseThrow();
    // do single task
    @SuppressWarnings("unchecked")
    Drawer drawer = ((Function<String, Drawer>)
            nb.build(
                "sim.drawer(framer = sim.staticFramer(minX = 10.0; maxX = 35.0; minY = 1.0; maxY = 15.0); actions = true)"))
        .apply("test");
    taskOn(
            nb,
            engineSupplier,
            new RealtimeViewer(30, drawer),
            //            "s.t.sumoArena()")
            "s.t.sumoArena()")
        .run();
    System.exit(0);
  }

  private static double profile(Runnable runnable, int nOfTimes) {
    return IntStream.range(0, nOfTimes)
            .mapToDouble(i -> {
              Instant startingInstant = Instant.now();
              runnable.run();
              return Duration.between(startingInstant, Instant.now())
                  .toMillis();
            })
            .average()
            .orElse(Double.NaN)
        / 1000d;
  }

  private static String readResource(String resourcePath) throws IOException {
    InputStream inputStream = TerrainTester.class.getResourceAsStream(resourcePath);
    String content;
    if (inputStream == null) {
      throw new IOException("Cannot find resource %s".formatted(resourcePath));
    } else {
      try (BufferedReader br = new BufferedReader(new InputStreamReader(inputStream))) {
        content = br.lines().collect(Collectors.joining());
      }
    }
    return content;
  }

  private static Runnable taskOn(
      NamedBuilder<?> nb, Supplier<Engine> engineSupplier, Consumer<Snapshot> consumer, String terrain) {
    // prepare task
    TrainingSumo trainingSumo = new TrainingSumo(30, (Terrain) nb.build(terrain));
    // read agent resource
    String agentDescription;
    try {
      agentDescription = readResource("/agents/biped-vsr-centralized-mlp.txt");
    } catch (IOException e) {
      L.severe("Cannot read agent description: %s%n".formatted(e));
      throw new RuntimeException(e);
    }
    // load weights
    String serializedWeights;
    try {
      serializedWeights = readResource("/agents/trained-biped-fast-mlp-weights.txt");
    } catch (IOException e) {
      L.severe("Cannot read serialized params: %s%n".formatted(e));
      throw new RuntimeException(e);
    }
    List<Double> params;
    try {
      //noinspection unchecked
      params = (List<Double>) fromBase64(serializedWeights);
    } catch (IOException e) {
      L.severe("Cannot deserialize params: %s%n".formatted(e));
      throw new RuntimeException(e);
    }

    RandomGenerator rg = new Random();
    // prepare supplier
    Supplier<EmbodiedAgent> agentSupplier = () -> {
      EmbodiedAgent agent = (EmbodiedAgent) nb.build(agentDescription);

      if (agent instanceof NumMultiBrained numMultiBrained) {
        numMultiBrained.brains().stream()
            .map(b -> Composed.shallowest(b, NumericalParametrized.class))
            .forEach(o -> o.ifPresent(np -> {
              System.out.printf(
                  "Shuffling %d parameters of brain %s %n", ((double[]) np.getParams()).length, np);
              np.randomize(rg, DoubleRange.SYMMETRIC_UNIT);
            }));
      }
      return agent;
    };
    return () -> trainingSumo.run(agentSupplier, engineSupplier.get(), consumer);
  }
}
