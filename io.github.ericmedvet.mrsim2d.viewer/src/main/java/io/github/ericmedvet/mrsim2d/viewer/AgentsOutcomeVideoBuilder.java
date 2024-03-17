package io.github.ericmedvet.mrsim2d.viewer;

import io.github.ericmedvet.jsdynsym.control.Simulation;
import io.github.ericmedvet.jviz.core.drawer.VideoBuilder;
import io.github.ericmedvet.mrsim2d.core.ActionOutcome;
import io.github.ericmedvet.mrsim2d.core.Agent;
import io.github.ericmedvet.mrsim2d.core.NFCMessage;
import io.github.ericmedvet.mrsim2d.core.Snapshot;
import io.github.ericmedvet.mrsim2d.core.bodies.Body;
import io.github.ericmedvet.mrsim2d.core.tasks.AgentsObservation;
import io.github.ericmedvet.mrsim2d.core.tasks.AgentsOutcome;

import java.io.IOException;
import java.util.Collection;
import java.util.List;

public class AgentsOutcomeVideoBuilder implements VideoBuilder<Simulation.Outcome<Snapshot>> {

  private record ThinSnapshot(double t, Collection<Agent> agents, Collection<Body> bodies) implements Snapshot {
    @Override
    public Collection<ActionOutcome<?, ?>> actionOutcomes() {
      return List.of();
    }

    @Override
    public Collection<NFCMessage> nfcMessages() {
      return List.of();
    }
  }
  private final Drawer drawer;

  public AgentsOutcomeVideoBuilder(Drawer drawer) {
    this.drawer = drawer;
  }

  @Override
  public Video build(VideoInfo videoInfo, Simulation.Outcome<Snapshot> o) throws IOException {
    return null;
  }
}
