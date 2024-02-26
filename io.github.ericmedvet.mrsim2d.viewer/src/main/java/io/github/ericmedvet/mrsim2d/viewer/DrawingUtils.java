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

package io.github.ericmedvet.mrsim2d.viewer;

import io.github.ericmedvet.jnb.datastructure.DoubleRange;
import io.github.ericmedvet.mrsim2d.core.geometry.BoundingBox;
import io.github.ericmedvet.mrsim2d.core.geometry.Point;
import io.github.ericmedvet.mrsim2d.core.geometry.Poly;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Stroke;
import java.awt.geom.Line2D;
import java.awt.geom.Path2D;
import java.awt.geom.Rectangle2D;

public class DrawingUtils {

  public static final Font FONT = new Font("Monospaced", Font.PLAIN, 12);

  private DrawingUtils() {}

  public enum Alignment {
    LEFT,
    CENTER,
    RIGHT
  }

  public static class Colors {
    public static final Color TEXT = Color.BLUE;
    public static final Color AXES = Color.BLACK;
    public static final Color DATA = Color.RED;
    public static final Color DATA_POSITIVE = Color.BLUE;
    public static final Color DATA_NEGATIVE = Color.YELLOW;
    public static final Color DATA_ZERO = Color.BLACK;
    public static final Color DATA_BACKGROUND = Color.WHITE;
  }

  public static Color alphaed(Color color, float alpha) {
    return new Color(
        (float) color.getRed() / 255f, (float) color.getGreen() / 255f, (float) color.getBlue() / 255f, alpha);
  }

  public static void drawFilledBar(
      double x,
      double y,
      double w,
      double h,
      double value,
      DoubleRange range,
      String format,
      Graphics2D g,
      Color lineColor,
      Color fillColor,
      Color bgColor) {
    if (bgColor != null) {
      g.setColor(bgColor);
      g.fill(new Rectangle2D.Double(x, y, w, h));
    }
    g.setColor(fillColor);
    g.fill(new Rectangle2D.Double(x, y, w * range.normalize(value), h));
    g.setColor(lineColor);
    g.draw(new Rectangle2D.Double(x, y, w, h));
    if (format != null) {
      String s = String.format(format, value);
      g.setColor(Colors.TEXT);
      g.drawString(s, Math.round(x + g.getFontMetrics().charWidth('x') + w), Math.round(y + h));
    }
  }

  public static void drawFilledBar(
      double x, double y, double w, double h, double value, DoubleRange range, String format, Graphics2D g) {
    drawFilledBar(x, y, w, h, value, range, format, g, Colors.AXES, Colors.DATA, Colors.DATA_BACKGROUND);
  }

  public static void drawLine(Graphics2D g, Point src, Point dst) {
    g.draw(new Line2D.Double(src.x(), src.y(), dst.x(), dst.y()));
  }

  public static void fill(Graphics2D g, Point... points) {
    g.fill(toPath(points));
  }

  public static BoundingBox getBoundingBox(Graphics2D g) {
    return new BoundingBox(
        new Point(g.getClipBounds().getMinX(), g.getClipBounds().getMinY()),
        new Point(g.getClipBounds().getMaxX(), g.getClipBounds().getMaxY()));
  }

  public static Stroke getScaleIndependentStroke(float thickness, float scale) {
    return new BasicStroke(thickness / scale);
  }

  public static Color linear(final Color c1, final Color c2, float min, float max, float x) {
    x = (x - min) / (max - min);
    x = Float.max(0f, Float.min(1f, x));
    final float r1 = c1.getRed() / 255f;
    final float g1 = c1.getGreen() / 255f;
    final float b1 = c1.getBlue() / 255f;
    final float a1 = c1.getAlpha() / 255f;
    final float r2 = c2.getRed() / 255f;
    final float g2 = c2.getGreen() / 255f;
    final float b2 = c2.getBlue() / 255f;
    final float a2 = c2.getAlpha() / 255f;
    final float r = r1 + (r2 - r1) * x;
    final float g = g1 + (g2 - g1) * x;
    final float b = b1 + (b2 - b1) * x;
    final float a = a1 + (a2 - a1) * x;
    return new Color(r, g, b, a);
  }

  public static Color linear(final Color c1, final Color c2, final Color c3, float x1, float x2, float x3, float x) {
    if (x < x2) {
      return linear(c1, c2, x1, x2, x);
    }
    return linear(c2, c3, x2, x3, x);
  }

  public static Path2D toPath(Point... points) {
    Path2D path = new Path2D.Double();
    path.moveTo(points[0].x(), points[0].y());
    for (int i = 1; i < points.length; i++) {
      path.lineTo(points[i].x(), points[i].y());
    }
    return path;
  }

  public static Path2D toPath(Poly poly, boolean close) {
    Path2D path = toPath(poly.vertexes());
    if (close) {
      path.closePath();
    }
    return path;
  }
}
