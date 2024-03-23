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
import io.github.ericmedvet.mrsim2d.core.tasks.Task;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;
import java.util.Random;
import java.util.function.Supplier;
import java.util.logging.Logger;
import java.util.random.RandomGenerator;
import java.util.stream.Collectors;

/**
 * @author "Eric Medvet" on 2024/03/23 for 2dmrsim
 */
public class SequentialAgentTester {

  private static final Logger L = Logger.getLogger(AgentTester.class.getName());
  private static final String TASK = "sim.task.locomotion(duration = 10; terrain = s.t.downhill(a = 5))";
  private static final List<String> AGENTS = List.of(
      "ball-vsr-reactive.txt",
      "biped-vsr-centralized-drn.txt",
      "biped-vsr-centralized-mlp.txt",
      "biped-vsr-reactive.txt",
      "hybrid-biped-vsr-centralized-mlp.txt",
      "hybrid-tripod-vsr-distributed-mlp.txt",
      "independent-voxel-all-mlp.txt",
      "independent-voxel-noanchors-mlp.txt",
      "legged-mlp.txt",
      "legged-sin.txt",
      "modular-legged-mlp.txt",
      "modular-legged-sin.txt",
      "trained-biped-vsr-centralized-mlp.txt",
      "tripod-vsr-distributed-mlp.txt",
      "worm-vsr-reactive.txt");

  public static void main(String[] args) {
    NamedBuilder<Object> nb = NamedBuilder.fromDiscovery();
    // prepare task
    @SuppressWarnings("unchecked")
    Task<Supplier<EmbodiedAgent>, ?, ?> task = (Task<Supplier<EmbodiedAgent>, ?, ?>) nb.build(TASK);
    for (String agent : AGENTS) {
      System.out.printf("Loading agent description \"%s\"%n", agent);
      InputStream inputStream = AgentTester.class.getResourceAsStream("/agents/%s".formatted(agent));
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
      System.out.println(task.simulate(getEmbodiedAgentSupplier(agentDescription, nb)));
    }
  }

  private static Supplier<EmbodiedAgent> getEmbodiedAgentSupplier(String agentDescription, NamedBuilder<Object> nb) {
    RandomGenerator rg = new Random();
    return () -> {
      EmbodiedAgent agent = (EmbodiedAgent) nb.build(agentDescription);
      // shuffle parameters
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
  }
}
