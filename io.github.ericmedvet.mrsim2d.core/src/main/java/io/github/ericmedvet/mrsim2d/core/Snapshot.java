
package io.github.ericmedvet.mrsim2d.core;

import io.github.ericmedvet.mrsim2d.core.bodies.Body;

import java.util.Collection;
public interface Snapshot {
  Collection<ActionOutcome<?, ?>> actionOutcomes();

  Collection<Agent> agents();

  Collection<Body> bodies();
  Collection<NFCMessage> nfcMessages();

  double t();
}
