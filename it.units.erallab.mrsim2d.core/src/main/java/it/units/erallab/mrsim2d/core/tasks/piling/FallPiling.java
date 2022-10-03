package it.units.erallab.mrsim2d.core.tasks.piling;

import it.units.erallab.mrsim2d.builder.BuilderMethod;
import it.units.erallab.mrsim2d.builder.Param;
import it.units.erallab.mrsim2d.core.EmbodiedAgent;
import it.units.erallab.mrsim2d.core.Snapshot;
import it.units.erallab.mrsim2d.core.actions.AddAgent;
import it.units.erallab.mrsim2d.core.actions.CreateUnmovableBody;
import it.units.erallab.mrsim2d.core.actions.TranslateAgent;
import it.units.erallab.mrsim2d.core.bodies.Body;
import it.units.erallab.mrsim2d.core.engine.Engine;
import it.units.erallab.mrsim2d.core.geometry.BoundingBox;
import it.units.erallab.mrsim2d.core.geometry.Point;
import it.units.erallab.mrsim2d.core.geometry.Terrain;
import it.units.erallab.mrsim2d.core.tasks.Task;
import it.units.erallab.mrsim2d.core.util.DoubleRange;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.random.RandomGenerator;

public class FallPiling implements Task<Supplier<EmbodiedAgent>, Outcome> {

  private final static double X_GAP = 10;

  private final double duration;
  private final double fallInterval;
  private final int nOfAgents;
  private final double xSigmaRatio;
  private final RandomGenerator randomGenerator;
  private final Terrain terrain;
  private final double yGapRatio;
  private final double xGap;

  public FallPiling(
      double duration,
      double fallInterval,
      int nOfAgents,
      double xSigmaRatio,
      RandomGenerator randomGenerator,
      Terrain terrain,
      double yGapRatio,
      double xGap
  ) {
    this.duration = duration;
    this.fallInterval = fallInterval;
    this.nOfAgents = nOfAgents;
    this.xSigmaRatio = xSigmaRatio;
    this.randomGenerator = randomGenerator;
    this.terrain = terrain;
    this.xGap = xGap;
    this.yGapRatio = yGapRatio;
  }

  @BuilderMethod
  public FallPiling(
      @Param(value = "duration", dD = 45d) double duration,
      @Param(value = "fallInterval", dD = 5d) double fallInterval,
      @Param("nOfAgents") int nOfAgents,
      @Param(value = "xSigmaRatio", dD = 0.1d) double xSigmaRatio,
      @Param(value = "randomGenerator") RandomGenerator randomGenerator,
      @Param(value = "terrain") Terrain terrain,
      @Param(value = "yGapRatio", dD = 1d) double yGapRatio
  ) {
    this(duration, fallInterval, nOfAgents, xSigmaRatio, randomGenerator, terrain, yGapRatio, X_GAP);
  }

  private void placeAgent(Engine engine, EmbodiedAgent agent, List<EmbodiedAgent> agents) {
    BoundingBox agentBB = agent.boundingBox();
    DoubleRange xRange = new DoubleRange(
        -agentBB.width() / 2d,
        agentBB.width() / 2d
    ).delta(terrain.withinBordersXRange().min() + xGap);
    double baseY;
    if (agents.isEmpty()) {
      baseY = terrain.maxHeightAt(xRange);
    } else {
      baseY = agents.stream()
          .map(EmbodiedAgent::boundingBox)
          .filter(b -> xRange.overlaps(new DoubleRange(b.min().x(), b.max().x())))
          .mapToDouble(b -> b.max().y())
          .max()
          .orElse(0d);
    }
    baseY = baseY + agentBB.height() * yGapRatio;
    engine.perform(new TranslateAgent(agent, new Point(
        xRange.min() + xRange.extent() / 2d + randomGenerator.nextGaussian(
            0d,
            xSigmaRatio * agentBB.width()
        ) - agentBB.min().x(),
        baseY - agentBB.min().y()
    )));
  }

  @Override
  public Outcome run(
      Supplier<EmbodiedAgent> embodiedAgentSupplier,
      Engine engine,
      Consumer<Snapshot> snapshotConsumer
  ) {
    //build world
    engine.perform(new CreateUnmovableBody(terrain.poly()));
    //run for defined time
    Map<Double, Outcome.Observation> observations = new HashMap<>();
    List<EmbodiedAgent> agents = new ArrayList<>(nOfAgents);
    while (engine.t() < duration) {
      //check if new agent needed
      if (agents.size() < Math.ceil(engine.t() / fallInterval) && agents.size() < nOfAgents) {
        EmbodiedAgent agent = embodiedAgentSupplier.get();
        engine.perform(new AddAgent(agent));
        placeAgent(engine, agent, agents);
        agents.add(agent);
      }
      //tick
      Snapshot snapshot = engine.tick();
      snapshotConsumer.accept(snapshot);
      observations.put(
          engine.t(),
          new Outcome.Observation(agents.stream()
              .map(a -> a.bodyParts().stream().map(Body::poly).toList())
              .toList()
          )
      );
    }
    return new Outcome(new TreeMap<>(observations));
  }
}