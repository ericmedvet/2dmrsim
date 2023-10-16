
package io.github.ericmedvet.mrsim2d.core.engine;

import io.github.ericmedvet.mrsim2d.core.ActionOutcome;
import io.github.ericmedvet.mrsim2d.core.Agent;
import io.github.ericmedvet.mrsim2d.core.NFCMessage;
import io.github.ericmedvet.mrsim2d.core.Snapshot;
import io.github.ericmedvet.mrsim2d.core.bodies.Body;

import java.util.Collection;
import java.util.Map;
public record EngineSnapshot(
    double t,
    Collection<Body> bodies,
    Collection<Agent> agents,
    Collection<ActionOutcome<?, ?>> actionOutcomes,
    Collection<NFCMessage> nfcMessages,
    Map<TimeType, Double> times,
    Map<CounterType, Integer> counters
) implements Snapshot {
  public enum CounterType {TICK, ACTION, ILLEGAL_ACTION, UNSUPPORTED_ACTION}

  public enum TimeType {ENVIRONMENT, WALL, TICK, INNER_TICK, PERFORM}

}
