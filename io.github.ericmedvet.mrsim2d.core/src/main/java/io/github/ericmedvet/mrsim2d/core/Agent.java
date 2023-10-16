
package io.github.ericmedvet.mrsim2d.core;

import java.util.List;
public interface Agent {
  List<? extends Action<?>> act(double t, List<ActionOutcome<?,?>> previousActionOutcomes);
}
