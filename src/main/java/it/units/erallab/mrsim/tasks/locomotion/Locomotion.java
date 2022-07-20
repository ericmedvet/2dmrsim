package it.units.erallab.mrsim.tasks.locomotion;

import it.units.erallab.mrsim.core.EmbodiedAgent;
import it.units.erallab.mrsim.core.Snapshot;
import it.units.erallab.mrsim.core.geometry.Poly;
import it.units.erallab.mrsim.engine.Engine;
import it.units.erallab.mrsim.tasks.Task;

import java.util.*;
import java.util.function.Consumer;

public class Locomotion implements Task<EmbodiedAgent, Locomotion.Outcome> {

  private final static double INITIAL_HEIGHT_GAP = 0.25;

  public record Outcome(SortedMap<Double, Observation> observations) {
    public record Observation(List<Poly> bodyPartPolies, double terrainHeight) {}
  }

  private final double duration;
  private final Poly terrainPoly;
  private final double initialHeightGap;

  public Locomotion(double duration, Poly terrainPoly, double initialHeightGap) {
    this.duration = duration;
    this.terrainPoly = terrainPoly;
    this.initialHeightGap = initialHeightGap;
  }

  public Locomotion(double duration, Poly terrainPoly) {
    this(duration, terrainPoly, INITIAL_HEIGHT_GAP);
  }

  @Override
  public Outcome run(EmbodiedAgent embodiedAgent, Engine engine, Consumer<Snapshot> snapshotConsumer) {
    //build world
    //place agent
    Map<Double, Outcome.Observation> observations = new HashMap<>();
    //run for defined time
    //return
    return new Locomotion.Outcome(new TreeMap<>(observations));
  }
}
