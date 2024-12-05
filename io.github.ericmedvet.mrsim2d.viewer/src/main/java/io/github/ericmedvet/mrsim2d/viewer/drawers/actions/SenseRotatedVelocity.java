/*-
 * ========================LICENSE_START=================================
 * mrsim2d-viewer
 * %%
 * Copyright (C) 2020 - 2024 Eric Medvet
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

import io.github.ericmedvet.mrsim2d.core.ActionOutcome;
import io.github.ericmedvet.mrsim2d.core.geometry.Point;
import io.github.ericmedvet.mrsim2d.viewer.DrawingUtils;
import java.awt.*;

public class SenseRotatedVelocity
    extends AbstractActionComponentDrawer<io.github.ericmedvet.mrsim2d.core.actions.SenseRotatedVelocity, Double> {

  private static final Color COLOR = Color.GREEN;
  private static final double MULT = 1d;

  private final Color color;

  public SenseRotatedVelocity(Color color) {
    super(io.github.ericmedvet.mrsim2d.core.actions.SenseRotatedVelocity.class);
    this.color = color;
  }

  public SenseRotatedVelocity() {
    this(COLOR);
  }

  @Override
  protected boolean innerDraw(
      double t,
      ActionOutcome<io.github.ericmedvet.mrsim2d.core.actions.SenseRotatedVelocity, Double> ao,
      Graphics2D g) {
    // draw line
    g.setColor(color);
    Point src = ao.action().body().poly().center();
    Point dst =
        src.sum(new Point(ao.action().direction() + ao.action().body().angle())
            .scale(ao.outcome().orElse(0d) * MULT));
    DrawingUtils.drawLine(g, src, dst);
    return true;
  }
}
