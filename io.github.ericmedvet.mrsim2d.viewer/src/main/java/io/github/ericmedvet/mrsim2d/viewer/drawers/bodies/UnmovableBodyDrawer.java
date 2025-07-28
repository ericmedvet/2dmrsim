/*-
 * ========================LICENSE_START=================================
 * mrsim2d-viewer
 * %%
 * Copyright (C) 2020 - 2025 Eric Medvet
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

import io.github.ericmedvet.mrsim2d.core.bodies.UnmovableBody;
import io.github.ericmedvet.mrsim2d.viewer.DrawingUtils;
import io.github.ericmedvet.mrsim2d.viewer.drawers.AbstractComponentDrawer;
import java.awt.*;
import java.awt.geom.Path2D;
import java.awt.image.BufferedImage;

public class UnmovableBodyDrawer extends AbstractComponentDrawer<UnmovableBody> {

  private static final Color TEXTURE_COLOR = Color.GRAY;
  private static final Color STROKE_COLOR = Color.BLACK;
  private static final int TEXTURE_SIZE = 2;

  private final Color strokeColor;
  private final TexturePaint texturePaint;

  public UnmovableBodyDrawer(Color textureColor, Color strokeColor) {
    super(UnmovableBody.class);
    this.strokeColor = strokeColor;
    texturePaint = createTexturePaint(textureColor);
  }

  public UnmovableBodyDrawer() {
    this(TEXTURE_COLOR, STROKE_COLOR);
  }

  private static TexturePaint createTexturePaint(Color textureColor) {
    BufferedImage texture = new BufferedImage(2, 2, BufferedImage.TYPE_4BYTE_ABGR);
    Graphics2D g = texture.createGraphics();
    g.setColor(DrawingUtils.alphaed(textureColor, 0.25f));
    g.fillRect(0, 0, 2, 2);
    g.setColor(DrawingUtils.alphaed(textureColor, 0.5f));
    g.fillRect(1, 0, 1, 1);
    g.fillRect(0, 1, 1, 1);
    g.dispose();
    return new TexturePaint(texture, new Rectangle(0, 0, TEXTURE_SIZE, TEXTURE_SIZE));
  }

  @Override
  protected boolean innerDraw(double t, UnmovableBody body, Graphics2D g) {
    Path2D path = DrawingUtils.toPath(body.poly(), true);
    g.setPaint(texturePaint);
    g.fill(path);
    g.setColor(strokeColor);
    g.draw(path);
    return true;
  }
}
