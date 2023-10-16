
package io.github.ericmedvet.mrsim2d.viewer.framers;

import io.github.ericmedvet.mrsim2d.core.geometry.BoundingBox;
import io.github.ericmedvet.mrsim2d.core.geometry.Point;
import io.github.ericmedvet.mrsim2d.viewer.Framer;
public abstract class AbstractFramer<K> implements Framer<K> {
  public static final BoundingBox DEFAULT_BOUNDING_BOX = new BoundingBox(Point.ORIGIN, new Point(10, 5));
  private final double sizeRelativeMargin;

  public AbstractFramer(double sizeRelativeMargin) {
    this.sizeRelativeMargin = sizeRelativeMargin;
  }

  protected abstract BoundingBox getCurrentBoundingBox(K k);

  @Override
  public BoundingBox getFrame(double t, K k, double ratio) {
    BoundingBox currentBB = getCurrentBoundingBox(k);
    //enlarge
    double cx = currentBB.center().x();
    double cy = currentBB.center().y();
    double w = currentBB.width();
    double h = currentBB.height();
    BoundingBox enlarged = new BoundingBox(
        new Point(cx - w / 2d * sizeRelativeMargin, cy - h / 2d * sizeRelativeMargin),
        new Point(cx + w / 2d * sizeRelativeMargin, cy + h / 2d * sizeRelativeMargin)
    );
    //adjust
    BoundingBox adjusted = enlarged;
    double fRatio = enlarged.width() / enlarged.height();
    if (fRatio > ratio) {
      //enlarge h
      adjusted = new BoundingBox(
          new Point(enlarged.min().x(), cy - h / 2d * sizeRelativeMargin * fRatio / ratio),
          new Point(enlarged.max().x(), cy + h / 2d * sizeRelativeMargin * fRatio / ratio)
      );
    } else if (fRatio < ratio) {
      //enlarge w
      adjusted = new BoundingBox(
          new Point(cx - w / 2d * sizeRelativeMargin * ratio / fRatio, enlarged.min().y()),
          new Point(cx + w / 2d * sizeRelativeMargin * ratio / fRatio, enlarged.max().y())
      );
    }
    return adjusted;
  }
}
