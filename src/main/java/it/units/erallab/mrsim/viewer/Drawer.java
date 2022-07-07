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

package it.units.erallab.mrsim.viewer;

import it.units.erallab.mrsim.core.Snapshot;
import it.units.erallab.mrsim.core.geometry.BoundingBox;
import it.units.erallab.mrsim.core.geometry.Point;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import java.util.List;

/**
 * @author "Eric Medvet" on 2022/07/07 for 2dmrsim
 */
public interface Drawer {
  boolean draw(Snapshot s, Graphics2D g);

  static Drawer clear() {
    return clear(Color.WHITE);
  }

  static Drawer clear(Color color) {
    return (s, g) -> {
      g.setColor(color);
      g.fill(g.getClip());
      return true;
    };
  }

  static Drawer clip(BoundingBox boundingBox, Drawer drawer) {
    return (s, g) -> {
      Shape shape = g.getClip();
      double clipX = shape.getBounds2D().getX();
      double clipY = shape.getBounds2D().getY();
      double clipW = shape.getBounds2D().getWidth();
      double clipH = shape.getBounds2D().getHeight();
      g.clip(new Rectangle2D.Double(
          clipX + boundingBox.min().x() * clipW,
          clipY + boundingBox.min().y() * clipH,
          clipW * boundingBox.width(),
          clipH * boundingBox.height()
      ));
      //draw
      boolean drawn = drawer.draw(s, g);
      //restore clip and transform
      g.setClip(shape);
      return drawn;
    };
  }

  static Drawer of(Drawer... drawers) {
    return of(java.util.List.of(drawers));
  }

  static Drawer of(List<Drawer> drawers) {
    return (s, g) -> {
      boolean drawn = false;
      for (Drawer drawer : drawers) {
        drawn = drawer.draw(s, g) || drawn;
      }
      return drawn;
    };
  }

  static Drawer text(String s) {
    return text(s, DrawingUtils.Alignment.CENTER);
  }

  static Drawer text(String s, DrawingUtils.Alignment alignment) {
    return text(s, alignment, DrawingUtils.Colors.TEXT);
  }

  static Drawer text(String string, DrawingUtils.Alignment alignment, Color color) {
    return (s, g) -> {
      g.setColor(color);
      g.drawString(string, switch (alignment) {
        case LEFT -> g.getClipBounds().x + 1;
        case CENTER -> g.getClipBounds().x + g.getClipBounds().width / 2 - g.getFontMetrics().stringWidth(string) / 2;
        case RIGHT -> g.getClipBounds().x + g.getClipBounds().width - 1 - g.getFontMetrics().stringWidth(string);
      }, g.getClipBounds().y + 1 + g.getFontMetrics().getMaxAscent());
      return string.isEmpty();
    };
  }

  static Drawer transform(Framer framer, Drawer drawer) {
    return (s, g) -> {
      BoundingBox graphicsFrame = new BoundingBox(
          new Point(
              g.getClip().getBounds2D().getX(),
              g.getClip().getBounds2D().getY()
          ),
          new Point(
              g.getClip().getBounds2D().getMaxX(),
              g.getClip().getBounds2D().getMaxY()
          )
      );
      BoundingBox worldFrame = framer.getFrame(s, graphicsFrame.width() / graphicsFrame.height());
      //save original transform and stroke
      AffineTransform oAt = g.getTransform();
      Stroke oStroke = g.getStroke();
      //prepare transformation
      double xRatio = graphicsFrame.width() / worldFrame.width();
      double yRatio = graphicsFrame.height() / worldFrame.height();
      double ratio = Math.min(xRatio, yRatio);
      AffineTransform at = new AffineTransform();
      at.translate(graphicsFrame.min().x(), graphicsFrame.min().y());
      at.scale(ratio, -ratio);
      at.translate(-worldFrame.min().x(), -worldFrame.max().y());
      //apply transform and stroke
      g.setTransform(at);
      g.setStroke(DrawingUtils.getScaleIndependentStroke(1, (float) ratio));
      //draw
      boolean drawn = drawer.draw(s, g);
      //restore transform
      g.setTransform(oAt);
      g.setStroke(oStroke);
      return drawn;
    };
  }

}
