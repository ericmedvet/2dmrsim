
package io.github.ericmedvet.mrsim2d.viewer.framers;

import io.github.ericmedvet.mrsim2d.core.EmbodiedAgent;
import io.github.ericmedvet.mrsim2d.core.Snapshot;
import io.github.ericmedvet.mrsim2d.core.geometry.BoundingBox;
public class AllAgentsFramer extends AbstractFramer<Snapshot> {

  public AllAgentsFramer(double sizeRelativeMargin) {
    super(sizeRelativeMargin);
  }

  @Override
  protected BoundingBox getCurrentBoundingBox(Snapshot snapshot) {
    return snapshot.agents().stream()
        .filter(a -> a instanceof EmbodiedAgent)
        .map(a -> ((EmbodiedAgent) a).bodyParts().stream()
            .map(b -> b.poly().boundingBox())
            .reduce(BoundingBox::enclosing)
            .orElse(DEFAULT_BOUNDING_BOX)
        )
        .reduce(BoundingBox::enclosing)
        .orElse(DEFAULT_BOUNDING_BOX);
  }
}
