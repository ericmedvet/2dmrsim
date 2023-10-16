
package io.github.ericmedvet.mrsim2d.viewer.framers;

import io.github.ericmedvet.mrsim2d.core.Snapshot;
import io.github.ericmedvet.mrsim2d.core.geometry.BoundingBox;
public class StaticFramer extends AbstractFramer<Snapshot> {

  private BoundingBox initialBoundingBox;

  public StaticFramer(BoundingBox initialBoundingBox) {
    super(1d);
    this.initialBoundingBox = initialBoundingBox;
  }

  public StaticFramer(double sizeRelativeMargin) {
    super(sizeRelativeMargin);
  }

  @Override
  protected BoundingBox getCurrentBoundingBox(Snapshot snapshot) {
    if (initialBoundingBox == null) {
      initialBoundingBox = snapshot.bodies().stream()
          .map(b -> b.poly().boundingBox())
          .reduce(BoundingBox::enclosing)
          .orElse(DEFAULT_BOUNDING_BOX);
    }
    return initialBoundingBox;
  }
}
