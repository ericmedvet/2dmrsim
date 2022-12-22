/*
 * Copyright 2022 Eric Medvet <eric.medvet@gmail.com> (as eric)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.github.ericmedvet.mrsim2d.viewer.framers;

import io.github.ericmedvet.mrsim2d.core.geometry.BoundingBox;
import io.github.ericmedvet.mrsim2d.core.geometry.Point;
import io.github.ericmedvet.mrsim2d.viewer.Framer;

/**
 * @author "Eric Medvet" on 2022/07/08 for 2dmrsim
 */
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
