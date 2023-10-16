
package io.github.ericmedvet.mrsim2d.sample;

import io.github.ericmedvet.jnb.core.NamedBuilder;
import io.github.ericmedvet.jsdynsym.core.NumericalParametrized;
import io.github.ericmedvet.jsdynsym.core.composed.Composed;
import io.github.ericmedvet.mrsim2d.buildable.PreparedNamedBuilder;
import io.github.ericmedvet.mrsim2d.core.EmbodiedAgent;
import io.github.ericmedvet.mrsim2d.core.NumMultiBrained;
import io.github.ericmedvet.mrsim2d.core.Snapshot;
import io.github.ericmedvet.mrsim2d.core.engine.Engine;
import io.github.ericmedvet.mrsim2d.core.geometry.Terrain;
import io.github.ericmedvet.mrsim2d.core.tasks.locomotion.Locomotion;
import io.github.ericmedvet.mrsim2d.viewer.Drawer;
import io.github.ericmedvet.mrsim2d.viewer.RealtimeViewer;

import java.io.*;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;
import java.util.List;
import java.util.ServiceLoader;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
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
    //prepare engine
    Supplier<Engine> engineSupplier = () -> ServiceLoader.load(Engine.class).findFirst().orElseThrow();
    //do single task
    if (true) {
      @SuppressWarnings("unchecked")
      Drawer drawer = ((Function<String, Drawer>) nb.build("sim.drawer(actions=true;enlargement=5)")).apply("test");
      taskOn(nb, engineSupplier, new RealtimeViewer(30, drawer), "s.t.hilly()").run();
      System.exit(0);
    }
    //prepare terrains
    List<String> terrains = List.of(
        "s.t.flat()",
        "s.t.flat(w = 500)",
        "s.t.hilly()",
        "s.t.hilly(chunkW = 0.5; chunkH = 0.1; w = 250)",
        "s.t.steppy(chunkW = 0.5; chunkH = 0.1; w = 250)",
        "s.t.hilly(chunkW = 0.5; chunkH = 0.1; w = 500)",
        "s.t.steppy(chunkW = 0.5; chunkH = 0.1; w = 500)",
        "s.t.hilly(chunkW = 0.5; chunkH = 0.1; w = 1500)",
        "s.t.steppy(chunkW = 0.5; chunkH = 0.1; w = 1500)",
        "s.t.steppy(chunkW = 0.5; chunkH = 0.1)"
    );
    Consumer<Snapshot> nullConsumer = s -> {};
    //warm up
    int warmUpNOfTimes = 10;
    L.info("Warming up");
    System.out.printf(
        "t=%5.3f with n=%d on %s%n",
        profile(taskOn(nb, engineSupplier, nullConsumer, terrains.get(0)), warmUpNOfTimes),
        warmUpNOfTimes,
        terrains.get(0)
    );
    //profile
    L.info("Testing");
    int testNOfTimes = 5;
    for (String terrain : terrains) {
      System.out.printf(
          "t=%5.3f with n=%d on %s%n",
          profile(taskOn(nb, engineSupplier, nullConsumer, terrain), testNOfTimes),
          testNOfTimes,
          terrain
      );
    }
  }

  private static double profile(Runnable runnable, int nOfTimes) {
    return IntStream.range(0, nOfTimes)
        .mapToDouble(i -> {
          Instant startingInstant = Instant.now();
          runnable.run();
          return Duration.between(startingInstant, Instant.now()).toMillis();
        })
        .average().orElse(Double.NaN) / 1000d;
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
      NamedBuilder<?> nb,
      Supplier<Engine> engineSupplier,
      Consumer<Snapshot> consumer,
      String terrain
  ) {
    //prepare task
    Locomotion locomotion = new Locomotion(60, (Terrain) nb.build(terrain));
    //read agent resource
    String agentDescription;
    try {
      agentDescription = readResource("/agents/trained-biped-vsr-centralized-mlp.txt");
    } catch (IOException e) {
      L.severe("Cannot read agent description: %s%n".formatted(e));
      throw new RuntimeException(e);
    }
    //load weights
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
    //prepare supplier
    Supplier<EmbodiedAgent> agentSupplier = () -> {
      EmbodiedAgent agent = (EmbodiedAgent) nb.build(agentDescription);
      //shuffle parameters
      if (agent instanceof NumMultiBrained numMultiBrained) {
        numMultiBrained.brains().stream()
            .map(b -> Composed.shallowest(b, NumericalParametrized.class))
            .forEach(o -> o.ifPresent(np -> np.setParams(params.stream().mapToDouble(d -> d).toArray())));
      }
      return agent;
    };
    return () -> locomotion.run(agentSupplier, engineSupplier.get(), consumer);
  }
}
