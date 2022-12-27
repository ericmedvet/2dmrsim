package io.github.ericmedvet.mrsim2d.sample;

import io.github.ericmedvet.jnb.core.NamedBuilder;
import io.github.ericmedvet.mrsim2d.buildable.PreparedNamedBuilder;
import io.github.ericmedvet.mrsim2d.core.actions.CreateAndTranslateUnmovableBody;
import io.github.ericmedvet.mrsim2d.core.actions.CreateUnmovableBody;
import io.github.ericmedvet.mrsim2d.core.bodies.UnmovableBody;
import io.github.ericmedvet.mrsim2d.core.engine.Engine;
import io.github.ericmedvet.mrsim2d.core.geometry.Point;
import io.github.ericmedvet.mrsim2d.core.geometry.Poly;
import io.github.ericmedvet.mrsim2d.core.geometry.Terrain;
import io.github.ericmedvet.mrsim2d.viewer.Drawer;
import io.github.ericmedvet.mrsim2d.viewer.RealtimeViewer;

import java.util.ServiceLoader;
import java.util.function.Function;
import java.util.logging.Logger;

/**
 * @author "Eric Medvet" on 2022/12/27 for 2dmrsim
 */
public class JointTester {
  private final static Logger L = Logger.getLogger(JointTester.class.getName());

  public static void main(String[] args) {
    NamedBuilder<Object> nb = PreparedNamedBuilder.get();
    //prepare drawer, viewer, engine
    @SuppressWarnings("unchecked")
    Drawer drawer = ((Function<String, Drawer>) nb.build("sim.drawer(actions=true)")).apply("test");
    RealtimeViewer viewer = new RealtimeViewer(30, drawer);
    Engine engine = ServiceLoader.load(Engine.class).findFirst().orElseThrow();
    //prepare world
    Terrain terrain = (Terrain) nb.build("sim.terrain.flat()");
    UnmovableBody sBox = engine.perform(new CreateUnmovableBody(Poly.rectangle(10,4), 1)).outcome().orElseThrow();
    engine.perform( new CreateAndTranslateUnmovableBody(Poly.rectangle(6,1), 1, new Point(0,4)));
    UnmovableBody nBox = engine.perform(new CreateAndTranslateUnmovableBody(Poly.rectangle(10,4), 1, new Point(0,5))).outcome().orElseThrow();

  }
}
