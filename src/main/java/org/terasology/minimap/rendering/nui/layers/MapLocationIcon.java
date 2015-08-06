/*
 * Copyright 2014 MovingBlocks
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
package org.terasology.minimap.rendering.nui.layers;

import org.terasology.math.geom.Vector2i;
import org.terasology.rendering.assets.texture.TextureRegion;
import org.terasology.rendering.nui.Canvas;
import org.terasology.rendering.nui.Color;
import org.terasology.rendering.nui.CoreWidget;
import org.terasology.rendering.nui.databinding.Binding;
import org.terasology.rendering.nui.databinding.DefaultBinding;

/**
 * @author mkienenb
 */
public class MapLocationIcon extends CoreWidget {

    private static final int MINIMAP_TILE_SIZE = 16;
    private static final float MINIMAP_TRANSPARENCY = 0.5f;
    private static final Color MINIMAP_TRANSPARENCY_COLOR = Color.WHITE.alterAlpha((int) (MINIMAP_TRANSPARENCY * 256));

    private Binding<TextureRegion> textureRegionBinding = new DefaultBinding<>();

    @Override
    public void onDraw(Canvas canvas) {
        if (getIcon() != null) {
            canvas.drawTexture(getIcon(), MINIMAP_TRANSPARENCY_COLOR);
        }
    }

    @Override
    public Vector2i getPreferredContentSize(Canvas canvas, Vector2i sizeHint) {
        return new Vector2i(MINIMAP_TILE_SIZE, MINIMAP_TILE_SIZE);
    }

    public void bindIcon(Binding<TextureRegion> binding) {
        textureRegionBinding = binding;
    }

    public TextureRegion getIcon() {
        return textureRegionBinding.get();
    }

    public void setIcon(TextureRegion val) {
        textureRegionBinding.set(val);
    }
}
