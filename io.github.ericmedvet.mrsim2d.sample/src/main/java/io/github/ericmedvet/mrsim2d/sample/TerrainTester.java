/*
 * Copyright 2022 eric
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

package io.github.ericmedvet.mrsim2d.sample;

import io.github.ericmedvet.jnb.core.NamedBuilder;
import io.github.ericmedvet.mrsim2d.buildable.PreparedNamedBuilder;
import io.github.ericmedvet.mrsim2d.core.EmbodiedAgent;
import io.github.ericmedvet.mrsim2d.core.NumBrained;
import io.github.ericmedvet.mrsim2d.core.engine.Engine;
import io.github.ericmedvet.mrsim2d.core.tasks.Task;
import io.github.ericmedvet.mrsim2d.core.util.Parametrized;
import io.github.ericmedvet.mrsim2d.viewer.Drawer;
import io.github.ericmedvet.mrsim2d.viewer.RealtimeViewer;

import java.io.*;
import java.util.Base64;
import java.util.List;
import java.util.ServiceLoader;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 * @author "Eric Medvet" on 2022/07/06 for 2dmrsim
 */
public class TerrainTester {

  private final static Logger L = Logger.getLogger(TerrainTester.class.getName());

  private static Object fromBase64(String content) throws IOException {
    try (ByteArrayInputStream bais = new ByteArrayInputStream(Base64.getDecoder().decode(content));
         ObjectInputStream ois = new ObjectInputStream(bais)) {
      return ois.readObject();
    } catch (Throwable t) {
      throw new IOException(t);
    }
  }

  public static void main(String[] args) {
    NamedBuilder<Object> nb = PreparedNamedBuilder.get();
    //prepare drawer, viewer, engine
    @SuppressWarnings("unchecked")
    Drawer drawer = ((Function<String, Drawer>) nb.build("sim.drawer(actions=true)")).apply("test");
    RealtimeViewer viewer = new RealtimeViewer(30, drawer);
    Engine engine = ServiceLoader.load(Engine.class).findFirst().orElseThrow();
    //prepare task
    @SuppressWarnings("unchecked") Task<Supplier<EmbodiedAgent>, ?> task = (Task<Supplier<EmbodiedAgent>, ?>) nb.build(
        """
              sim.task.locomotion(
                duration = 120;
                terrain = s.t.steppy(chunkW = 0.5; chunkH = 0.1)
              )
            """);
    //read agent resource
    String agentDescription = null;
    try {
      agentDescription = readResource("/agents/trained-biped-vsr-centralized-mlp.txt");
    } catch (IOException e) {
      L.severe("Cannot read agent description: %s%n".formatted(e));
      System.exit(-1);
    }
    EmbodiedAgent agent = (EmbodiedAgent) nb.build(agentDescription);
    //load weights
    String serializedWeights = null;
    try {
      serializedWeights = readResource("/agents/trained-biped-fast-mlp-weights.txt");
    } catch (IOException e) {
      L.severe("Cannot read serialized params: %s%n".formatted(e));
      System.exit(-1);
    }
    try {
      @SuppressWarnings("unchecked") List<Double> params = (List<Double>)fromBase64(serializedWeights);
      if (agent instanceof NumBrained numBrained) {
        if (numBrained.brain() instanceof Parametrized parametrized) {
          parametrized.setParams(params.stream().mapToDouble(d -> d).toArray());
        }
      }
    } catch (IOException e) {
      L.severe("Cannot deserialize params: %s%n".formatted(e));
    }
    //do task
    task.run(() -> agent, engine, viewer);
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
}
