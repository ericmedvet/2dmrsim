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
import io.github.ericmedvet.jsdynsym.core.DoubleRange;
import io.github.ericmedvet.jsdynsym.core.NumericalParametrized;
import io.github.ericmedvet.jsdynsym.core.composed.Composed;
import io.github.ericmedvet.mrsim2d.buildable.PreparedNamedBuilder;
import io.github.ericmedvet.mrsim2d.core.EmbodiedAgent;
import io.github.ericmedvet.mrsim2d.core.NumMultiBrained;
import io.github.ericmedvet.mrsim2d.core.engine.Engine;
import io.github.ericmedvet.mrsim2d.core.tasks.Task;
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

  private final static String TASK_1 = "sim.task.locomotion(duration = 120; terrain = s.t.steppy(chunkW = 8.5; chunkH" +
      " = 0.1; w = 250))";
  private final static String TASK_2 = "s.task.prebuiltIndependentLocomotion(shape = s.a.vsr.s.biped(w = 4; h = 3))";
  private final static String TASK_3 = "s.task.standPiling(nOfAgents = 3)";
  private final static String TASK_4 = "sim.task.locomotion(duration = 120; terrain = s.t.downhill(a = 5))";

  public static void main(String[] args) {
    NamedBuilder<Object> nb = PreparedNamedBuilder.get();
    //prepare drawer, viewer, engine
    @SuppressWarnings("unchecked")
    Drawer drawer = ((Function<String, Drawer>) nb.build("sim.drawer(actions=true;nfc=true)")).apply("test");
    RealtimeViewer viewer = new RealtimeViewer(30, drawer);
    Engine engine = ServiceLoader.load(Engine.class).findFirst().orElseThrow();
    //prepare task
    @SuppressWarnings("unchecked") Task<Supplier<EmbodiedAgent>, ?> task = (Task<Supplier<EmbodiedAgent>, ?>) nb.build(
        TASK_4);
    //read agent resource
    String agentName = args.length >= 1 ? args[0] : "worm-vsr-reactive";
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
    String agentDesc = agentDescription;
    RandomGenerator rg = new Random();
    Supplier<EmbodiedAgent> agentSupplier = () -> {
      EmbodiedAgent agent = (EmbodiedAgent) nb.build(agentDesc);
      //shuffle parameters
      if (agent instanceof NumMultiBrained numMultiBrained) {
        numMultiBrained.brains().stream()
            .map(b -> Composed.shallowest(b, NumericalParametrized.class))
            .forEach(o -> o.ifPresent(np -> {
              System.out.printf("Shuffling %d parameters of brain %s %n", np.getParams().length, np);
              np.randomize(rg, DoubleRange.SYMMETRIC_UNIT);
            }));
      }
      return agent;
    };
    //do task
    task.run(agentSupplier, engine, viewer);
  }

}
