/*
 * Copyright 2015 MovingBlocks
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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.asset.Assets;
import org.terasology.math.geom.Vector2i;
import org.terasology.math.geom.Vector3i;
import org.terasology.math.geom.Vector2f;
import org.terasology.registry.CoreRegistry;
import org.terasology.rendering.assets.texture.BasicTextureRegion;
import org.terasology.rendering.assets.texture.Texture;
import org.terasology.rendering.assets.texture.TextureRegion;
import org.terasology.rendering.nui.Canvas;
import org.terasology.rendering.nui.Color;
import org.terasology.rendering.nui.CoreWidget;
import org.terasology.rendering.nui.databinding.Binding;
import org.terasology.rendering.nui.databinding.DefaultBinding;
import org.terasology.rendering.nui.databinding.ReadOnlyBinding;
import org.terasology.world.WorldProvider;
import org.terasology.world.block.Block;
import org.terasology.world.block.BlockAppearance;
import org.terasology.world.block.BlockPart;

/**
 * @author Immortius
 */
public class MinimapCell extends CoreWidget {

    private static final Logger logger = LoggerFactory.getLogger(MinimapCell.class);

    private static final int MINIMAP_TILE_SIZE = 25;
    private static final float MINIMAP_TRANSPARENCY = 0.5f;
    private static final Color MINIMAP_TRANSPARENCY_COLOR = Color.WHITE.alterAlpha((int) (MINIMAP_TRANSPARENCY * 256));

    private final Texture textureAtlas;
    private final TextureRegion questionMarkTextureRegion;

    private Vector2i relativeCellLocation;
    private Binding<Vector3i> centerLocationBinding = new DefaultBinding<>(null);

    private ReadOnlyBinding<TextureRegion> icon;

    public MinimapCell() {

        textureAtlas = Assets.getTexture("engine:terrain").get();
        questionMarkTextureRegion = Assets.getTextureRegion("engine:items#questionMark").get();

        icon = new ReadOnlyBinding<TextureRegion>() {
            @Override
            public TextureRegion get() {
                Vector3i centerLocation = getCenterLocation();
                if (null == centerLocation) {
                    return null;
                }

                WorldProvider worldProvider = CoreRegistry.get(WorldProvider.class);

                // top down view
                Vector3i relativeLocation = new Vector3i(-relativeCellLocation.x, 0, relativeCellLocation.y);
                relativeLocation.add(centerLocation);
                Block block = worldProvider.getBlock(relativeLocation);
                if (null != block) {
                    BlockAppearance primaryAppearance = block.getPrimaryAppearance();

                    BlockPart blockPart = BlockPart.TOP;

                    // TODO: security issues
                    //                    WorldAtlas worldAtlas = CoreRegistry.get(WorldAtlas.class);
                    //                    float tileSize = worldAtlas.getRelativeTileSize();

                    float tileSize = 16f / 256f; // 256f could be replaced by textureAtlas.getWidth();

                    Vector2f textureAtlasPos = primaryAppearance.getTextureAtlasPos(blockPart);

                    TextureRegion textureRegion = new BasicTextureRegion(textureAtlas, textureAtlasPos, new Vector2f(tileSize, tileSize));
                    return textureRegion;
                }

                logger.info("No block found for location " + relativeLocation);
                return questionMarkTextureRegion;
            }
        };
    }

    @Override
    public void onDraw(Canvas canvas) {
        canvas.drawTexture(icon.get(), MINIMAP_TRANSPARENCY_COLOR);
    }

    @Override
    public Vector2i getPreferredContentSize(Canvas canvas, Vector2i sizeHint) {
        return new Vector2i(MINIMAP_TILE_SIZE, MINIMAP_TILE_SIZE);
    }

    public Vector2i getCellRelativeLocation() {
        return relativeCellLocation;
    }

    public void setRelativeCellLocation(Vector2i relativeLocation) {
        this.relativeCellLocation = relativeLocation;
    }

    public void bindCenterLocation(Binding<Vector3i> binding) {
        centerLocationBinding = binding;
    }

    public Vector3i getCenterLocation() {
        return centerLocationBinding.get();
    }

    public void setCenterLocation(Vector3i location) {
        centerLocationBinding.set(location);
    }
}
