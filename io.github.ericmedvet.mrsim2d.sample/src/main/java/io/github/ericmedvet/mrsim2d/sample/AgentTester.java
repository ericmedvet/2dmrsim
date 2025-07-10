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
import io.github.ericmedvet.mrsim2d.core.engine.Engine;
import io.github.ericmedvet.mrsim2d.core.tasks.Task;
import io.github.ericmedvet.mrsim2d.viewer.Drawer;
import io.github.ericmedvet.mrsim2d.viewer.RealtimeViewer;
import java.io.*;
import java.util.Random;
import java.util.ServiceLoader;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.logging.Logger;
import java.util.random.RandomGenerator;
import java.util.stream.Collectors;

public class AgentTester {

  private static final Logger L = Logger.getLogger(AgentTester.class.getName());

  private static final String TASK_LOCOMOTION = "sim.task.locomotion(duration = 120; terrain = s.t.downhill(a = 0))";
  private static final String TASK_JUMPING = "sim.task.jumping()";
  private static final String TASK_BALANCING = "sim.task.balancing(supportHeight = 0.5; swingLength = 10; duration = 20)";

  public static void main(String[] args) throws IOException {
    NamedBuilder<Object> nb = NamedBuilder.fromDiscovery();
    // prepare drawer, viewer, engine
    @SuppressWarnings("unchecked") Drawer drawer = ((Function<String, Drawer>) nb.build(
        "sim.drawer(actions=true; nfc=true; enlargement = 5; info = false; miniAgents = none)"
    ))
        .apply("test");
    RealtimeViewer viewer = new RealtimeViewer(30, drawer);
    Engine engine = ServiceLoader.load(Engine.class).findFirst().orElseThrow();
    // prepare task
    @SuppressWarnings("unchecked") Task<Supplier<EmbodiedAgent>, ?, ?> task = (Task<Supplier<EmbodiedAgent>, ?, ?>) nb
        .build(TASK_LOCOMOTION);
    // read agent resource
    String agentName = args.length >= 1 ? args[0] : "biped-vsr-centralized-mlp";
    L.info("Loading agent description \"%s\"".formatted(agentName));
    InputStream inputStream = AgentTester.class.getResourceAsStream("/agents/%s.txt".formatted(agentName));
    String agentDescription = null;
    if (inputStream == null) {
      L.severe("Cannot find agent description");
    } else {
      try (BufferedReader br = new BufferedReader(new InputStreamReader(inputStream))) {
        agentDescription = br.lines().collect(Collectors.joining());
      } catch (IOException e) {
        L.severe("Cannot read agent description: %s%n".formatted(e));
        System.exit(-1);
      }
    }
    // do task
    task.run(getEmbodiedAgentSupplier(agentDescription, nb), engine, viewer);
    //    FramesImageBuilder fib = new FramesImageBuilder(500,400, 20,3,0.6,1, FramesImageBuilder.Direction.HORIZONTAL, false, drawer);
    //    task.run(getEmbodiedAgentSupplier(agentDescription, nb), engine, fib);
    //    ImageIO.write(fib.get(), "png", new File("../reactive-biped.png"));
  }

  private static Supplier<EmbodiedAgent> getEmbodiedAgentSupplier(String agentDescription, NamedBuilder<Object> nb) {
    RandomGenerator rg = new Random();
    return () -> {
      EmbodiedAgent agent = (EmbodiedAgent) nb.build(agentDescription);
      // shuffle parameters
      if (agent instanceof NumMultiBrained numMultiBrained) {
        numMultiBrained.brains()
            .stream()
            .map(b -> Composed.shallowest(b, NumericalParametrized.class))
            .forEach(o -> o.ifPresent(np -> {
              System.out.printf(
                  "Shuffling %d parameters of brain %s %n",
                  ((double[]) np.getParams()).length,
                  np
              );
              np.randomize(rg, DoubleRange.SYMMETRIC_UNIT);
            }));
      }
      return agent;
    };
  }
}
