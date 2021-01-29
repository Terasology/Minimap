// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.minimap.overlays;

import org.joml.Vector2f;
import org.joml.Vector2fc;
import org.joml.Vector2i;
import org.joml.Vector2ic;
import org.terasology.joml.geom.Rectanglei;
import org.terasology.nui.Canvas;
import org.terasology.nui.Color;
import org.terasology.nui.util.RectUtility;
import org.terasology.rendering.assets.texture.Texture;

import java.util.Collection;

/**
 * TODO Type description
 */
public class MinimapIconOverlay implements MinimapOverlay {

    private Vector2fc iconSize;
    private final Texture icon;
    private Color color = new Color(Color.white);
    private Collection<? extends Vector2ic> points;

    public MinimapIconOverlay(Collection<? extends Vector2ic> points, Texture icon) {
        this(points, icon, new Vector2i(icon.getWidth(), icon.getHeight()));
    }

    public MinimapIconOverlay(Collection<? extends Vector2ic> points, Texture icon, Vector2ic iconSize) {
        this.points = points;
        this.icon = icon;
        this.iconSize = new Vector2f(iconSize);
    }

    public void render(Canvas canvas, Rectanglei worldRect) {
        float scaleX = (float) canvas.getRegion().lengthX() / (float) worldRect.lengthX();
        float scaleY = (float) canvas.getRegion().lengthY() / (float) worldRect.lengthY();

        float width = (iconSize.x() * scaleX);
        float height = (iconSize.y() * scaleY);
        Rectanglei expWorldRect = RectUtility.expand(canvas.getRegion(), (int) (width * .5f), (int) (height * .5f));

        Vector2i tempMap = new Vector2i();
        for (Vector2ic center : points) {
            if (expWorldRect.containsPoint(center)) {
                RectUtility.map(worldRect, canvas.getRegion(), center, tempMap);
                tempMap.x -= width / 2;
                tempMap.y -= height / 2;
                Rectanglei region = RectUtility.createFromMinAndSize(tempMap.x, tempMap.y, (int) width, (int) height);
                canvas.drawTexture(icon, region, color);
            }
        }
    }

    public int getZOrder() {
        return 0;
    }

    public void setColor(Color color) {
        this.color = color;
    }

    public void setIconSize(Vector2fc iconSize) {
        this.iconSize = new Vector2f(iconSize);
    }
}
