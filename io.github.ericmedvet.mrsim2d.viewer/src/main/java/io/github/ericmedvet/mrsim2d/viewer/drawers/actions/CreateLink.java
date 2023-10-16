/*-
 * ========================LICENSE_START=================================
 * mrsim2d-viewer
 * %%
 * Copyright (C) 2020 - 2023 Eric Medvet
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * =========================LICENSE_END==================================
 */

package io.github.ericmedvet.mrsim2d.viewer.drawers.actions;

import io.github.ericmedvet.jsdynsym.core.DoubleRange;
import io.github.ericmedvet.mrsim2d.core.ActionOutcome;
import io.github.ericmedvet.mrsim2d.core.bodies.Anchor;
import io.github.ericmedvet.mrsim2d.core.geometry.Point;
import io.github.ericmedvet.mrsim2d.viewer.DrawingUtils;
import java.awt.*;
import java.awt.geom.Ellipse2D;
import java.util.function.BiPredicate;

public class CreateLink
    extends AbstractLastingActionOutcomeDrawer<
        io.github.ericmedvet.mrsim2d.core.actions.CreateLink, Anchor.Link> {

  private static final Color COLOR = Color.GREEN;

  private static final DoubleRange RADIUS = new DoubleRange(0, 0.15);

  private final Color color;

  public CreateLink(Color color, double duration) {
    super(io.github.ericmedvet.mrsim2d.core.actions.CreateLink.class, duration);
    this.color = color;
  }

  public CreateLink() {
    this(COLOR, DURATION);
  }

  @Override
  protected BiPredicate<Double, Graphics2D> innerBuildTask(
      double t,
      ActionOutcome<io.github.ericmedvet.mrsim2d.core.actions.CreateLink, Anchor.Link> o) {
    return (dT, g) -> {
      if (o.outcome().isPresent()) {
        g.setColor(color);
        double r = RADIUS.max() - RADIUS.denormalize(dT / duration);
        Anchor src = o.outcome().get().source();
        Anchor dst = o.outcome().get().destination();
        g.draw(new Ellipse2D.Double(src.point().x() - r, src.point().y() - r, r * 2d, r * 2d));
        g.draw(new Ellipse2D.Double(dst.point().x() - r, dst.point().y() - r, r * 2d, r * 2d));
        double a = src.point().diff(dst.point()).direction();
        Point lSrc = src.point().diff(new Point(a).scale(r));
        Point lDst = dst.point().sum(new Point(a).scale(r));
        DrawingUtils.drawLine(g, lSrc, lDst);
        return dT > duration;
      } else {
        return true;
      }
    };
  }
}
