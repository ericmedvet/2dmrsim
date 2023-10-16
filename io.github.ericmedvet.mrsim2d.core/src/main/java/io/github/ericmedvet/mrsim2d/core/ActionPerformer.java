
package io.github.ericmedvet.mrsim2d.core;
@FunctionalInterface
public interface ActionPerformer {
  <A extends Action<O>, O> ActionOutcome<A, O> perform(A action, Agent agent);

  default <A extends Action<O>, O> ActionOutcome<A, O> perform(A action) {
    return perform(action, null);
  }

}
