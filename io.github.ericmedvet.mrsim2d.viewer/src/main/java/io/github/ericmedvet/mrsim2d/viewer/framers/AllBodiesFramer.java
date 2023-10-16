
package io.github.ericmedvet.mrsim2d.viewer.framers;

import io.github.ericmedvet.mrsim2d.core.Snapshot;
import io.github.ericmedvet.mrsim2d.core.geometry.BoundingBox;
public class AllBodiesFramer extends AbstractFramer<Snapshot> {
  public AllBodiesFramer(double sizeRelativeMargin) {
    super(sizeRelativeMargin);
  }

  @Override
  protected BoundingBox getCurrentBoundingBox(Snapshot snapshot) {
    return snapshot.bodies().stream()
        .map(b -> b.poly().boundingBox())
        .reduce(BoundingBox::enclosing)
        .orElse(DEFAULT_BOUNDING_BOX);
  }
}
