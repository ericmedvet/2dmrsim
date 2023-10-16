
package io.github.ericmedvet.mrsim2d.core;

import java.util.Optional;
public record ActionOutcome<A extends Action<O>, O>(
    Agent agent,
    A action,
    Optional<O> outcome
) {
}
