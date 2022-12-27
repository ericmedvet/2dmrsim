package io.github.ericmedvet.mrsim2d.sample;

import io.github.ericmedvet.jnb.core.NamedBuilder;
import io.github.ericmedvet.mrsim2d.buildable.PreparedNamedBuilder;
import io.github.ericmedvet.mrsim2d.core.Snapshot;
import io.github.ericmedvet.mrsim2d.core.actions.*;
import io.github.ericmedvet.mrsim2d.core.bodies.Anchor;
import io.github.ericmedvet.mrsim2d.core.bodies.RigidBody;
import io.github.ericmedvet.mrsim2d.core.bodies.RotationalJoint;
import io.github.ericmedvet.mrsim2d.core.bodies.UnmovableBody;
import io.github.ericmedvet.mrsim2d.core.engine.Engine;
import io.github.ericmedvet.mrsim2d.core.geometry.Point;
import io.github.ericmedvet.mrsim2d.core.geometry.Poly;
import io.github.ericmedvet.mrsim2d.core.tasks.Task;
import io.github.ericmedvet.mrsim2d.viewer.Drawer;
import io.github.ericmedvet.mrsim2d.viewer.RealtimeViewer;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.DoubleUnaryOperator;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.logging.Logger;

/**
 * @author "Eric Medvet" on 2022/12/27 for 2dmrsim
 */
public class JointTester {
  private final static Logger L = Logger.getLogger(JointTester.class.getName());

  enum Type {SIN, SQUARE}

  public static class JointTask implements Task<DoubleUnaryOperator, JointTask.Outcome> {

    private final double jointLength;
    private final double loadMass;
    private final double duration;

    public JointTask(double jointLength, double loadMass, double duration) {
      this.jointLength = jointLength;
      this.loadMass = loadMass;
      this.duration = duration;
    }

    public record Observation(double target, double actual) {}

    public record Outcome(SortedMap<Double, Observation> observations) {}

    @Override
    public Outcome run(DoubleUnaryOperator targetF, Engine engine, Consumer<Snapshot> snapshotConsumer) {
      //prepare world
      UnmovableBody sBox = engine.perform(new CreateUnmovableBody(
              Poly.rectangle(5 + jointLength / 2d - 1, 2),
              1
          ))
          .outcome()
          .orElseThrow();
      engine.perform(new CreateAndTranslateUnmovableBody(
          Poly.rectangle(5, 1),
          1,
          new Point(0, 2)
      ));
      UnmovableBody nBox = engine.perform(new CreateAndTranslateUnmovableBody(
          Poly.rectangle(5 + jointLength / 2d - 1, 2),
          1,
          new Point(0, 3)
      )).outcome().orElseThrow();
      //place joint
      RotationalJoint joint = engine.perform(new CreateRotationalJoint(
              jointLength,
              1,
              1,
              new RotationalJoint.Motor()
          )
      ).outcome().orElseThrow();
      engine.perform(new TranslateBodyAt(joint, new Point(5, 3)));
      engine.perform(new AttachClosestAnchors(
          1,
          joint,
          nBox,
          Anchor.Link.Type.RIGID
      ));
      engine.perform(new AttachClosestAnchors(
          1,
          joint,
          sBox,
          Anchor.Link.Type.RIGID
      ));
      //place load
      if (loadMass > 0) {
        RigidBody load = engine.perform(new CreateRigidBody(Poly.square(1d), loadMass, 1)).outcome().orElseThrow();
        engine.perform(new TranslateBodyAt(load, joint.poly().boundingBox().max()));
        engine.perform(new AttachClosestAnchors(
            2,
            joint,
            load,
            Anchor.Link.Type.RIGID
        ));
      }
      //run for defined time
      Map<Double, Observation> observations = new HashMap<>();
      while (engine.t() < duration) {
        double targetAngle = targetF.applyAsDouble(engine.t());
        engine.perform(new ActuateRotationalJoint(joint, targetAngle));
        Snapshot snapshot = engine.tick();
        snapshotConsumer.accept(snapshot);
        observations.put(
            engine.t(),
            new Observation(targetAngle, joint.jointAngle())
        );
      }
      //return
      return new Outcome(new TreeMap<>(observations));
    }
  }

  private static void doAll(double duration, String filePath) {
    File outFile = new File(filePath);
    try (BufferedWriter bw = new BufferedWriter(new FileWriter(outFile))) {
      //write header
      bw.append("f;load;type;t;targetA;actualA\n");
      //prepare engine
      Supplier<Engine> engineSupplier = () -> ServiceLoader.load(Engine.class).findFirst().orElseThrow();
      List<Double> freqs = List.of(0.1, 0.25, 0.5, 0.75, 1d, 1.25);
      List<Double> loads = List.of(0d, 1d, 2d, 3d, 4d, 5d);
      List<Type> types = List.of(Type.values());
      //iterate
      for (double freq : freqs) {
        for (double load : loads) {
          for (Type type : types) {
            L.info("Doing f=%.3f load=%.3f type=%s".formatted(freq, load, type.toString().toLowerCase()));
            JointTask.Outcome outcome = doTask(
                duration,
                engineSupplier,
                freq,
                load,
                type,
                null
            );
            //print results
            for (double t : outcome.observations().keySet()) {
              JointTask.Observation obs = outcome.observations().get(t);
              bw.append(String.format(
                  Locale.ROOT,
                  "%5.3f; %5.3f; %10.10s; %6.3f; %+6.4f; %+6.4f%n",
                  freq,
                  load,
                  type.toString().toLowerCase(),
                  t,
                  obs.target,
                  obs.actual
              ));
            }
          }
        }
      }
    } catch (IOException ex) {
      L.severe("Exception: %s".formatted(ex));
    }
  }

  private static void doSingle(double duration, double freq, double load, Type type) {
    NamedBuilder<Object> nb = PreparedNamedBuilder.get();
    //prepare drawer, viewer, engine
    @SuppressWarnings("unchecked")
    Drawer drawer = ((Function<String, Drawer>) nb.build("sim.drawer(actions=true)")).apply("test");
    RealtimeViewer viewer = new RealtimeViewer(30, drawer);
    Supplier<Engine> engineSupplier = () -> ServiceLoader.load(Engine.class).findFirst().orElseThrow();
    doTask(
        duration,
        engineSupplier,
        freq,
        load,
        type,
        viewer
    );
  }

  private static JointTask.Outcome doTask(
      double duration,
      Supplier<Engine> engineSupplier,
      double freq,
      double load,
      Type type,
      Consumer<Snapshot> consumer
  ) {
    //prepare function
    DoubleUnaryOperator targetF = switch (type) {
      case SIN -> t -> Math.PI / 2d * Math.sin(2d * Math.PI * freq * t);
      case SQUARE -> t -> Math.PI / 2d * (Math.sin(2d * Math.PI * freq * t) > 0 ? 1d : -1d);
    };
    //prepare task
    JointTask task = new JointTask(5, load, duration);
    //do task
    return task.run(
        targetF,
        engineSupplier.get(),
        (consumer == null) ? (s -> {}) : consumer
    );
  }

  public static void main(String[] args) {
    doAll(10, "/home/eric/experiments/2dmrsim/joint/test.csv");
    //doSingle(30, .25, 5, Type.SIN);
  }


}
