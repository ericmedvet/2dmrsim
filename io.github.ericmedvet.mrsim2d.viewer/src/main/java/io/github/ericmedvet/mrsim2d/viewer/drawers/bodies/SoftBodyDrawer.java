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

package io.github.ericmedvet.mrsim2d.viewer.drawers.bodies;

import io.github.ericmedvet.jsdynsym.core.DoubleRange;
import io.github.ericmedvet.mrsim2d.core.bodies.SoftBody;
import io.github.ericmedvet.mrsim2d.core.geometry.Point;
import io.github.ericmedvet.mrsim2d.core.geometry.Poly;
import io.github.ericmedvet.mrsim2d.viewer.DrawingUtils;
import io.github.ericmedvet.mrsim2d.viewer.drawers.AbstractComponentDrawer;
import java.awt.*;
import java.awt.geom.Line2D;
import java.awt.geom.Path2D;

public class SoftBodyDrawer extends AbstractComponentDrawer<SoftBody> {

  private static final Color CONTRACTED_COLOR = new Color(252, 141, 89);
  private static final Color REST_COLOR = new Color(255, 255, 191);
  private static final Color EXPANDED_COLOR = new Color(145, 191, 219);
  private static final Color BORDER_COLOR = Color.BLACK;
  private static final DoubleRange AREA_RATIO_RANGE = new DoubleRange(0.75, 1.25);

  private final Color contractedColor;
  private final Color restColor;
  private final Color expandedColor;
  private final Color borderColor;
  private final DoubleRange areaRatioRange;

  public SoftBodyDrawer(
      Color contractedColor,
      Color restColor,
      Color expandedColor,
      Color borderColor,
      DoubleRange areaRatioRange) {
    super(SoftBody.class);
    this.contractedColor = contractedColor;
    this.restColor = restColor;
    this.expandedColor = expandedColor;
    this.borderColor = borderColor;
    this.areaRatioRange = areaRatioRange;
  }

  public SoftBodyDrawer() {
    this(CONTRACTED_COLOR, REST_COLOR, EXPANDED_COLOR, BORDER_COLOR, AREA_RATIO_RANGE);
  }

  @Override
  protected boolean innerDraw(double t, SoftBody body, Graphics2D g) {
    Poly poly = body.poly();
    Path2D path = DrawingUtils.toPath(poly, true);
    g.setColor(
        DrawingUtils.linear(
            contractedColor,
            restColor,
            expandedColor,
            (float) areaRatioRange.min(),
            1,
            (float) areaRatioRange.max(),
            (float) body.areaRatio()));
    g.fill(path);
    g.setColor(borderColor);
    g.draw(path);
    // angle line
    Point center = poly.center();
    Point firstSideMeanPoint = Point.average(poly.vertexes()[0], poly.vertexes()[1]);
    g.draw(
        new Line2D.Double(center.x(), center.y(), firstSideMeanPoint.x(), firstSideMeanPoint.y()));
    return true;
  }
}
