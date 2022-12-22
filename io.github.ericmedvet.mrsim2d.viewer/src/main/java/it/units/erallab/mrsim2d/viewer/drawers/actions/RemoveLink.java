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

package it.units.erallab.mrsim2d.viewer.drawers.actions;

import it.units.erallab.mrsim2d.core.ActionOutcome;
import it.units.erallab.mrsim2d.core.bodies.Anchor;
import it.units.erallab.mrsim2d.core.geometry.Point;
import it.units.erallab.mrsim2d.core.util.DoubleRange;

import java.awt.*;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;
import java.util.function.BiPredicate;

/**
 * @author "Eric Medvet" on 2022/07/11 for 2dmrsim
 */
public class RemoveLink extends AbstractLastingActionOutcomeDrawer<it.units.erallab.mrsim2d.core.actions.RemoveLink,
    Anchor.Link> {

  private final static Color COLOR = Color.RED;

  private final static DoubleRange RADIUS = new DoubleRange(0, 0.15);

  private final Color color;


  public RemoveLink(Color color, double duration) {
    super(it.units.erallab.mrsim2d.core.actions.RemoveLink.class, duration);
    this.color = color;
  }

  public RemoveLink() {
    this(COLOR, DURATION);
  }

  @Override
  protected BiPredicate<Double, Graphics2D> innerBuildTask(
      double t,
      ActionOutcome<it.units.erallab.mrsim2d.core.actions.RemoveLink, Anchor.Link> o
  ) {
    return (dT, g) -> {
      if (o.outcome().isPresent()) {
        g.setColor(color);
        double r = RADIUS.denormalize(dT / duration);
        Anchor.Link link = o.outcome().get();
        Anchor src = link.source();
        g.draw(new Ellipse2D.Double(src.point().x() - r, src.point().y() - r, r * 2d, r * 2d));
        Anchor dst = link.destination();
        g.draw(new Ellipse2D.Double(dst.point().x() - r, dst.point().y() - r, r * 2d, r * 2d));
        double a = src.point().diff(dst.point()).direction();
        Point lSrc = src.point().diff(new Point(a).scale(r));
        Point lDst = dst.point().sum(new Point(a).scale(r));
        g.draw(new Line2D.Double(lSrc.x(), lSrc.y(), lDst.x(), lDst.y()));
        return dT > duration;
      }
      return true;
    };
  }
}
