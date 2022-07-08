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

package it.units.erallab.mrsim.viewer.drawers.body;

import it.units.erallab.mrsim.core.bodies.RigidBody;
import it.units.erallab.mrsim.core.bodies.SoftBody;
import it.units.erallab.mrsim.viewer.DrawingUtils;

import java.awt.*;
import java.awt.geom.Path2D;

/**
 * @author "Eric Medvet" on 2022/07/07 for 2dmrsim
 */
public class SoftBodyDrawer extends TypeBodyDrawer<SoftBody> {

  private final static Color CONTRACTED_COLOR = new Color(252, 141, 89);
  private final static Color REST_COLOR = new Color(255, 255, 191);
  private final static Color EXPANDED_COLOR = new Color(145, 191, 219);
  private final static Color STROKE_COLOR = Color.BLACK;
  private final static double MIN_AREA_RATIO = 0.6;
  private final static double MAX_AREA_RATIO = 1.4;

  private final Color contractedColor;
  private final Color restColor;
  private final Color expandedColor;
  private final Color strokeColor;
  private final double minAreaRatio;
  private final double maxAreaRatio;

  public SoftBodyDrawer(
      Color contractedColor,
      Color restColor,
      Color expandedColor,
      Color strokeColor,
      double minAreaRatio,
      double maxAreaRatio
  ) {
    super(SoftBody.class);
    this.contractedColor = contractedColor;
    this.restColor = restColor;
    this.expandedColor = expandedColor;
    this.strokeColor = strokeColor;
    this.minAreaRatio = minAreaRatio;
    this.maxAreaRatio = maxAreaRatio;
  }

  public SoftBodyDrawer() {
    this(CONTRACTED_COLOR, REST_COLOR, EXPANDED_COLOR, STROKE_COLOR, MIN_AREA_RATIO, MAX_AREA_RATIO);
  }

  @Override
  protected boolean innerDraw(double t, SoftBody body, int index, Graphics2D g) {
    Path2D path = DrawingUtils.toPath(body.poly(), true);
    g.setColor(DrawingUtils.linear(contractedColor,
        restColor,
        expandedColor,
        (float) minAreaRatio,
        1,
        (float) maxAreaRatio,
        (float) body.areaRatio()));
    g.fill(path);
    g.setColor(strokeColor);
    g.draw(path);
    return true;
  }
}
