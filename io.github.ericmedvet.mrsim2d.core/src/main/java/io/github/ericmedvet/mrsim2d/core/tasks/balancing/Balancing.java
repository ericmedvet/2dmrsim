package io.github.ericmedvet.mrsim2d.core.tasks.balancing;

import io.github.ericmedvet.jsdynsym.core.DoubleRange;
import io.github.ericmedvet.mrsim2d.core.EmbodiedAgent;
import io.github.ericmedvet.mrsim2d.core.Snapshot;
import io.github.ericmedvet.mrsim2d.core.actions.*;
import io.github.ericmedvet.mrsim2d.core.bodies.Anchor;
import io.github.ericmedvet.mrsim2d.core.bodies.Body;
import io.github.ericmedvet.mrsim2d.core.bodies.RigidBody;
import io.github.ericmedvet.mrsim2d.core.bodies.RotationalJoint;
import io.github.ericmedvet.mrsim2d.core.engine.Engine;
import io.github.ericmedvet.mrsim2d.core.geometry.*;
import io.github.ericmedvet.mrsim2d.core.tasks.AgentsObservation;
import io.github.ericmedvet.mrsim2d.core.tasks.Outcome;
import io.github.ericmedvet.mrsim2d.core.tasks.Task;
import io.github.ericmedvet.mrsim2d.core.util.PolyUtils;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class Balancing implements Task<Supplier<EmbodiedAgent>, Outcome<BalancingObservation>> {

  public static final double TERRAIN_BORDER_W = 10d;
  public final static double TERRAIN_BORDER_H = 100d;
  public static final double TERRAIN_W = 100d;
  public static final double TERRAIN_H = 25d;
  private final static double INITIAL_Y_GAP = 0.25;
  private final static double SUPPORT_WIDTH = 1;
  private final static double SWING_HEIGHT = 0.5;
  private final double duration;
  private final double initialYGap;
  private final double swingLength;
  private final double swingDensity;
  private final double supportHeight;
  private final double initialXGap;

  public Balancing(
      double duration,
      double swingLength,
      double swingDensity,
      double supportHeight,
      double initialXGap,
      double initialYGap
  ) {
    this.duration = duration;
    this.swingLength = swingLength;
    this.swingDensity = swingDensity;
    this.supportHeight = supportHeight;
    this.initialXGap = initialXGap;
    this.initialYGap = initialYGap;
  }

  public Balancing(double duration, double swingLength, double swingDensity, double supportHeight, double initialXGap) {
    this(duration, swingLength, swingDensity, supportHeight, initialXGap, INITIAL_Y_GAP);
  }


  @Override
  public Outcome<BalancingObservation> run(
      Supplier<EmbodiedAgent> embodiedAgentSupplier,
      Engine engine,
      Consumer<Snapshot> snapshotConsumer
  ) {
    //create agent
    EmbodiedAgent embodiedAgent = embodiedAgentSupplier.get();
    //build world
    Terrain terrain = Terrain.fromPath(
        new Path(new Point(TERRAIN_W, 0)),
        TERRAIN_H,
        TERRAIN_BORDER_W,
        TERRAIN_BORDER_H
    );
    engine.perform(new CreateUnmovableBody(terrain.poly()));
    //create swing
    double worldCenterX = terrain.withinBordersXRange().min() + terrain.withinBordersXRange().extent() / 2d;
    Point worldCenter = new Point(worldCenterX, terrain.maxHeightAt(new DoubleRange(worldCenterX, worldCenterX)));
    RigidBody support = engine.perform(new CreateUnmovableBody(
        Poly.rectangle(SUPPORT_WIDTH, supportHeight),
        1d
    )).outcome().orElseThrow();
    engine.perform(new TranslateBodyAt(support, BoundingBox.Anchor.CL, worldCenter));
    RotationalJoint joint = engine.perform(new CreateRotationalJoint(
        2d * SUPPORT_WIDTH,
        SUPPORT_WIDTH,
        1d,
        new RotationalJoint.Motor(0d, 0d, 0d, 0d, 0d, 2d * Math.PI),
        DoubleRange.UNBOUNDED
    )).outcome().orElseThrow();
    engine.perform(new RotateBody(joint, Math.PI / 2d));
    engine.perform(new TranslateBodyAt(
        joint,
        BoundingBox.Anchor.CL,
        support.poly().boundingBox().anchor(BoundingBox.Anchor.CU)
    ));
    RigidBody swing = engine.perform(new CreateRigidBody(Poly.rectangle(
        swingLength,
        SWING_HEIGHT
    ), swingDensity * swingLength * SWING_HEIGHT, 1)).outcome().orElseThrow();
    engine.perform(new TranslateBodyAt(
        swing,
        BoundingBox.Anchor.CL,
        joint.poly().boundingBox().anchor(BoundingBox.Anchor.CU)
    ));
    engine.perform(new AttachClosestAnchors(2, joint, support, Anchor.Link.Type.RIGID));
    engine.perform(new AttachClosestAnchors(2, swing, joint, Anchor.Link.Type.RIGID));
    //add agent
    engine.perform(new AddAgent(embodiedAgent));
    engine.perform(new TranslateAgentAt(
        embodiedAgent,
        BoundingBox.Anchor.CL,
        swing.poly().boundingBox().anchor(BoundingBox.Anchor.CU).sum(new Point(initialXGap, initialYGap))
    ));
    //run for defined time
    Map<Double, BalancingObservation> observations = new HashMap<>();
    while (engine.t() < duration) {
      Snapshot snapshot = engine.tick();
      snapshotConsumer.accept(snapshot);
      observations.put(
          engine.t(),
          new BalancingObservation(
              List.of(new AgentsObservation.Agent(
                  embodiedAgent.bodyParts().stream().map(Body::poly).toList(),
                  PolyUtils.maxYAtX(terrain.poly(), embodiedAgent.boundingBox().center().x())
              )),
              0d, // TODO fill
              false // TODO fill
          )
      );
    }
    //return
    return new Outcome<>(new TreeMap<>(observations));
  }
}
