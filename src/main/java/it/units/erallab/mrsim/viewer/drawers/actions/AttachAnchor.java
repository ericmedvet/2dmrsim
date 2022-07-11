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

package it.units.erallab.mrsim.viewer.drawers.actions;

import it.units.erallab.mrsim.core.ActionOutcome;
import it.units.erallab.mrsim.core.bodies.Anchor;
import it.units.erallab.mrsim.util.DoubleRange;

import java.awt.*;
import java.awt.geom.Ellipse2D;
import java.util.function.BiPredicate;

/**
 * @author "Eric Medvet" on 2022/07/11 for 2dmrsim
 */
public class AttachAnchor extends AbstractActionOutcomeDrawer<it.units.erallab.mrsim.core.actions.AttachAnchor,
    Anchor.Link> {

  private final static double DURATION = 0.5;
  private final static Color COLOR = Color.GREEN;

  private final static DoubleRange RADIUS = new DoubleRange(0, 0.2);

  private final double duration;
  private final Color color;


  @SuppressWarnings({"unchecked", "rawtypes"})
  public AttachAnchor(double duration, Color color) {
    super((Class) ActionOutcome.class);
    this.duration = duration;
    this.color = color;
  }

  public AttachAnchor() {
    this(DURATION, COLOR);
  }

  @Override
  protected BiPredicate<Double, Graphics2D> buildTask(
      double t,
      ActionOutcome<it.units.erallab.mrsim.core.actions.AttachAnchor, Anchor.Link> o
  ) {
    return (dT, g) -> {
      g.setColor(color);
      if (o.outcome().isPresent()) {
        double r = RADIUS.denormalize(dT / duration);
        Anchor src = o.outcome().get().source();
        Anchor dst = o.outcome().get().destination();
        g.draw(new Ellipse2D.Double(src.point().x() - r, src.point().y() - r, r * 2d, r * 2d));
        g.draw(new Ellipse2D.Double(dst.point().x() - r, dst.point().y() - r, r * 2d, r * 2d));
      }
      return dT > duration;
    };
  }
}
