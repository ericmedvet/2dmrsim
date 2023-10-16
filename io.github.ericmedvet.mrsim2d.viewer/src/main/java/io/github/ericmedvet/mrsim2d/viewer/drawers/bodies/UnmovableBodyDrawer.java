
package io.github.ericmedvet.mrsim2d.viewer.drawers.bodies;

import io.github.ericmedvet.mrsim2d.core.bodies.UnmovableBody;
import io.github.ericmedvet.mrsim2d.viewer.DrawingUtils;
import io.github.ericmedvet.mrsim2d.viewer.drawers.AbstractComponentDrawer;

import java.awt.*;
import java.awt.geom.Path2D;
import java.awt.image.BufferedImage;
public class UnmovableBodyDrawer extends AbstractComponentDrawer<UnmovableBody> {

  private final static Color TEXTURE_COLOR = Color.GRAY;
  private final static Color STROKE_COLOR = Color.BLACK;
  private final static int TEXTURE_SIZE = 2;

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
