
package io.github.ericmedvet.mrsim2d.viewer;

import java.util.function.Consumer;
import java.util.function.Supplier;
public interface Accumulator<O,I> extends Consumer<I>, Supplier<O> {
}
