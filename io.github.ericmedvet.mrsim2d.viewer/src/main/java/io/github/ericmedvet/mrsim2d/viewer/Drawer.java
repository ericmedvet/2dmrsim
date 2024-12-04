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

package io.github.ericmedvet.mrsim2d.viewer;

import io.github.ericmedvet.mrsim2d.core.Snapshot;
import io.github.ericmedvet.mrsim2d.core.geometry.BoundingBox;
import io.github.ericmedvet.mrsim2d.core.geometry.Point;
import io.github.ericmedvet.mrsim2d.core.util.AtomicDouble;
import io.github.ericmedvet.mrsim2d.core.util.Profiled;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.geom.AffineTransform;
import java.awt.geom.Line2D;
import java.awt.geom.Rectangle2D;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Map;

public interface Drawer {

    enum Direction {
        HORIZONTAL,
        VERTICAL
    }

    enum HorizontalPosition {
        LEFT,
        RIGHT
    }

    enum VerticalPosition {
        TOP,
        BOTTOM
    }

    interface ProfiledDrawer extends Drawer, Profiled {}

    boolean draw(List<Snapshot> snapshots, Graphics2D g);

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
        return (snapshots, g) -> {
            Shape shape = g.getClip();
            double clipX = shape.getBounds2D().getX();
            double clipY = shape.getBounds2D().getY();
            double clipW = shape.getBounds2D().getWidth();
            double clipH = shape.getBounds2D().getHeight();
            g.clip(new Rectangle2D.Double(
                    clipX + boundingBox.min().x() * clipW,
                    clipY + boundingBox.min().y() * clipH,
                    clipW * boundingBox.width(),
                    clipH * boundingBox.height()));
            // draw
            boolean drawn = drawer.draw(snapshots, g);
            // restore clip and transform
            g.setClip(shape);
            return drawn;
        };
    }

    static Drawer diagonals() {
        return (snapshots, g) -> {
            BoundingBox gBB = DrawingUtils.getBoundingBox(g);
            g.setColor(DrawingUtils.Colors.AXES);
            g.draw(new Rectangle2D.Double(gBB.min().x(), gBB.min().y(), gBB.width(), gBB.height()));
            g.draw(new Line2D.Double(
                    gBB.min().x(), gBB.min().y(), gBB.max().x(), gBB.max().y()));
            g.draw(new Line2D.Double(
                    gBB.min().x(), gBB.max().y(), gBB.max().x(), gBB.min().y()));
            return true;
        };
    }

    static Drawer of(Drawer... drawers) {
        return of(List.of(drawers));
    }

    static Drawer of(List<Drawer> drawers) {
        return (snapshots, g) -> {
            boolean drawn = false;
            for (Drawer drawer : drawers) {
                drawn = drawer.draw(snapshots, g) || drawn;
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
        return (snapshots, g) -> {
            g.setColor(color);
            g.drawString(
                    string,
                    switch (alignment) {
                        case LEFT -> g.getClipBounds().x + 1;
                        case CENTER -> g.getClipBounds().x
                                + g.getClipBounds().width / 2
                                - g.getFontMetrics().stringWidth(string) / 2;
                        case RIGHT -> g.getClipBounds().x
                                + g.getClipBounds().width
                                - 1
                                - g.getFontMetrics().stringWidth(string);
                    },
                    g.getClipBounds().y + 1 + g.getFontMetrics().getMaxAscent());
            return string.isEmpty();
        };
    }

    static Drawer transform(Framer<Snapshot> framer, Drawer drawer) {
        return (snapshots, g) -> {
            BoundingBox graphicsFrame = new BoundingBox(
                    new Point(
                            g.getClip().getBounds2D().getX(),
                            g.getClip().getBounds2D().getY()),
                    new Point(
                            g.getClip().getBounds2D().getMaxX(),
                            g.getClip().getBounds2D().getMaxY()));
            Snapshot lastSnapshot = snapshots.getLast();
            BoundingBox worldFrame =
                    framer.getFrame(lastSnapshot.t(), lastSnapshot, graphicsFrame.width() / graphicsFrame.height());
            // save original transform and stroke
            AffineTransform oAt = g.getTransform();
            Stroke oStroke = g.getStroke();
            // prepare transformation
            double xRatio = graphicsFrame.width() / worldFrame.width();
            double yRatio = graphicsFrame.height() / worldFrame.height();
            double ratio = Math.min(xRatio, yRatio);
            AffineTransform at = new AffineTransform();
            at.translate(graphicsFrame.center().x(), graphicsFrame.center().y());
            at.scale(ratio, -ratio);
            at.translate(-worldFrame.center().x(), -worldFrame.center().y());
            // apply transform and stroke
            g.setTransform(at);
            g.setStroke(DrawingUtils.getScaleIndependentStroke(1, (float) ratio));
            // draw
            boolean drawn = drawer.draw(snapshots, g);
            // restore transform
            g.setTransform(oAt);
            g.setStroke(oStroke);
            return drawn;
        };
    }

    default Drawer onLastSnapshot() {
        Drawer thisDrawer = this;
        return (snapshots, g) -> thisDrawer.draw(snapshots.subList(snapshots.size() - 1, snapshots.size()), g);
    }

    default ProfiledDrawer profiled() {
        Drawer thisDrawer = this;
        AtomicDouble drawingT = new AtomicDouble(0d);
        return new ProfiledDrawer() {
            @Override
            public boolean draw(List<Snapshot> snapshots, Graphics2D g) {
                Instant startingT = Instant.now();
                boolean drawn = thisDrawer.draw(snapshots, g);
                drawingT.add(Duration.between(startingT, Instant.now()).toNanos() / 1000000000d);
                return drawn;
            }

            @Override
            public Map<String, Number> values() {
                return Map.ofEntries(Map.entry("drawingT", drawingT.get()));
            }
        };
    }
}
