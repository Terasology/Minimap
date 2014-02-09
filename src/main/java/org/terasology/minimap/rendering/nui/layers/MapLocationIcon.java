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

import org.terasology.math.Vector2i;
import org.terasology.rendering.assets.texture.TextureRegion;
import org.terasology.rendering.nui.Canvas;
import org.terasology.rendering.nui.CoreWidget;
import org.terasology.rendering.nui.databinding.Binding;
import org.terasology.rendering.nui.databinding.DefaultBinding;

/**
 * @author mkienenb
 */
public class MapLocationIcon extends CoreWidget {

    private Binding<TextureRegion> textureRegionBinding = new DefaultBinding<>();

    @Override
    public void onDraw(Canvas canvas) {
        if (getIcon() != null) {
            canvas.drawTexture(getIcon());
        }
    }

    @Override
    public Vector2i getPreferredContentSize(Canvas canvas, Vector2i sizeHint) {
        // TODO: should we have a better way to determine this?
        return new Vector2i(16,16);
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
