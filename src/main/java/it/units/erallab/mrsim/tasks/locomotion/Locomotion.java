package it.units.erallab.mrsim.tasks.locomotion;

import it.units.erallab.mrsim.core.EmbodiedAgent;
import it.units.erallab.mrsim.core.Snapshot;
import it.units.erallab.mrsim.core.actions.AddAgent;
import it.units.erallab.mrsim.core.actions.CreateUnmovableBody;
import it.units.erallab.mrsim.core.bodies.Body;
import it.units.erallab.mrsim.core.geometry.Poly;
import it.units.erallab.mrsim.engine.Engine;
import it.units.erallab.mrsim.tasks.Task;

import java.util.*;
import java.util.function.Consumer;

public class Locomotion implements Task<EmbodiedAgent, Locomotion.Outcome> {

  private final static double INITIAL_X_GAP = 1;
  private final static double INITIAL_Y_GAP = 0.25;

  public record Outcome(SortedMap<Double, Observation> observations) {
    public record Observation(List<Poly> bodyPartPolies, double terrainHeight) {}
  }

  private final double duration;
  private final Poly terrainPoly;
  private final double initialXGap;
  private final double initialYGap;

  public Locomotion(double duration, Poly terrainPoly, double initialXGap, double initialYGap) {
    this.duration = duration;
    this.terrainPoly = terrainPoly;
    this.initialXGap = initialXGap;
    this.initialYGap = initialYGap;
  }

  public Locomotion(double duration, Poly terrainPoly) {
    this(duration, terrainPoly, INITIAL_X_GAP, INITIAL_Y_GAP);
  }

  @Override
  public Outcome run(EmbodiedAgent embodiedAgent, Engine engine, Consumer<Snapshot> snapshotConsumer) {
    //build world
    engine.perform(new CreateUnmovableBody(terrainPoly));
    engine.perform(new AddAgent(embodiedAgent));
    // TODO move agent (requires finding y of terrain at x, or smth like that)
    //place agent
    Map<Double, Outcome.Observation> observations = new HashMap<>();
    //run for defined time
    while (engine.t() < duration) {
      Snapshot snapshot = engine.tick();
      snapshotConsumer.accept(snapshot);
      observations.put(
          engine.t(),
          new Outcome.Observation(embodiedAgent.bodyParts().stream().map(Body::poly).toList(), 0d) // TODO put terr height
      );
    }
    //return
    return new Locomotion.Outcome(new TreeMap<>(observations));
  }
}
