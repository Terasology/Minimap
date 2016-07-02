/*
 * Copyright 2016 MovingBlocks
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.terasology.minimap.overlays;

import java.util.Collection;

import org.terasology.math.TeraMath;
import org.terasology.math.geom.BaseVector2f;
import org.terasology.math.geom.BaseVector2i;
import org.terasology.math.geom.ImmutableVector2f;
import org.terasology.math.geom.Rect2f;
import org.terasology.math.geom.Rect2fTransformer;
import org.terasology.math.geom.Rect2i;
import org.terasology.rendering.assets.texture.Texture;
import org.terasology.rendering.nui.Canvas;
import org.terasology.rendering.nui.Color;

/**
 * TODO Type description
 */
public class MinimapIconOverlay implements MinimapOverlay {

    private ImmutableVector2f iconSize;
    private final Texture icon;
    private Color color = Color.WHITE;
    private Collection<? extends BaseVector2i> points;

    public MinimapIconOverlay(Collection<? extends BaseVector2i> points, Texture icon) {
        this(points, icon, new ImmutableVector2f(icon.getWidth(), icon.getHeight()));
    }

    public MinimapIconOverlay(Collection<? extends BaseVector2i> points, Texture icon, BaseVector2f iconSize) {
        this.points = points;
        this.icon = icon;
        this.iconSize = ImmutableVector2f.createOrUse(iconSize);
    }

    @Override
    public void render(Canvas canvas, Rect2f worldRect, Rect2f screenRect) {
        Rect2fTransformer t = new Rect2fTransformer(worldRect, screenRect);
        float width = (iconSize.getX() * t.getScaleX());
        float height = (iconSize.getY() * t.getScaleY());
        Rect2f expWorldRect = screenRect.expand(width * 0.5f, height * 0.5f);
        for (BaseVector2i center : points) {
            if (expWorldRect.contains(center)) {
                int lx = TeraMath.floorToInt(t.applyX(center.getX()));
                int ly = TeraMath.floorToInt(t.applyY(center.getY()));
                lx -= width / 2;
                ly -= height / 2;
                Rect2i region = Rect2i.createFromMinAndSize(lx, ly, (int) width, (int) height);
                canvas.drawTexture(icon, region, color);
            }
        }
    }

    @Override
    public int getZOrder() {
        return 0;
    }

    public void setColor(Color color) {
        this.color = color;
    }

    public void setIconSize(BaseVector2f iconSize) {
        this.iconSize = ImmutableVector2f.createOrUse(iconSize);
    }
}
