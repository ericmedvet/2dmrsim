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
package io.github.ericmedvet.mrsim2d.viewer.drawers;

import io.github.ericmedvet.mrsim2d.core.NFCMessage;
import io.github.ericmedvet.mrsim2d.core.Snapshot;
import io.github.ericmedvet.mrsim2d.core.geometry.Point;
import io.github.ericmedvet.mrsim2d.viewer.Drawer;
import io.github.ericmedvet.mrsim2d.viewer.DrawingUtils;
import java.awt.Color;
import java.awt.Graphics2D;
import java.util.List;

public class NFCDrawer implements Drawer {
    private static final Color[] COLORS = new Color[] {
        DrawingUtils.alphaed(Color.PINK, 0.5f),
        DrawingUtils.alphaed(Color.CYAN, 0.5f),
        DrawingUtils.alphaed(Color.MAGENTA, 0.5f),
        DrawingUtils.alphaed(Color.YELLOW, 0.5f),
        DrawingUtils.alphaed(Color.ORANGE, 0.5f)
    };
    private static final double MAX_LENGTH = 0.5d;
    private static final double ARROW_LENGTH = 0.1d;
    private static final double ARROW_ANGLE = Math.PI / 6d;

    private final Color[] colors;
    private final double maxLenght;

    public NFCDrawer(Color[] colors, double maxLenght) {
        this.colors = colors;
        this.maxLenght = maxLenght;
    }

    public NFCDrawer() {
        this(COLORS, MAX_LENGTH);
    }

    @Override
    public boolean draw(List<Snapshot> snapshots, Graphics2D g) {
        for (NFCMessage message : snapshots.getLast().nfcMessages()) {
            g.setColor(colors[message.channel() % colors.length]);
            Point src = message.source();
            Point dst = src.sum(new Point(message.direction()).scale(maxLenght * Math.abs(message.value())));
            DrawingUtils.drawLine(g, src, dst);
            double dDirection = message.value() > 0d ? Math.PI : 0d;
            Point arrowEnd1 = dst.sum(new Point(message.direction() + ARROW_ANGLE + dDirection).scale(ARROW_LENGTH));
            Point arrowEnd2 = dst.sum(new Point(message.direction() - ARROW_ANGLE + dDirection).scale(ARROW_LENGTH));
            DrawingUtils.fill(g, dst, arrowEnd1, arrowEnd2);
        }
        return true;
    }
}
