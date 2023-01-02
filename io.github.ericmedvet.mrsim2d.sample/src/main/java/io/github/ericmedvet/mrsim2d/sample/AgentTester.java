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
import io.github.ericmedvet.mrsim2d.core.NumMultiBrained;
import io.github.ericmedvet.mrsim2d.core.engine.Engine;
import io.github.ericmedvet.mrsim2d.core.tasks.Task;
import io.github.ericmedvet.mrsim2d.core.util.DoubleRange;
import io.github.ericmedvet.mrsim2d.core.util.Parametrized;
import io.github.ericmedvet.mrsim2d.viewer.Drawer;
import io.github.ericmedvet.mrsim2d.viewer.RealtimeViewer;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Random;
import java.util.ServiceLoader;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.logging.Logger;
import java.util.random.RandomGenerator;
import java.util.stream.Collectors;

/**
 * @author "Eric Medvet" on 2022/07/06 for 2dmrsim
 */
public class AgentTester {

  private final static Logger L = Logger.getLogger(AgentTester.class.getName());

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
                initialXGap = 0.1;
                duration = 120;
                terrain = s.t.steppy(chunkW = 0.5; chunkH = 0.1; w = 250)
              )
            """);
    //read agent resource
    String agentName = args.length > 1 ? args[0] : "legged-groupedsin";
    L.config("Loading agent description \"%s\"".formatted(agentName));
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
    EmbodiedAgent agent = (EmbodiedAgent) nb.build(agentDescription);
    //shuffle parameters
    if (agent instanceof NumMultiBrained numMultiBrained) {
      RandomGenerator random = new Random(1);
      numMultiBrained.brains().forEach(b -> {
        if (b instanceof Parametrized parametrized) {
          System.out.printf("Shuffling %d parameters of one brain%n", parametrized.getParams().length);
          parametrized.randomize(random, DoubleRange.SYMMETRIC_UNIT);
        }
      });
    }
    //do task
    task.run(() -> agent, engine, viewer);
  }


}
