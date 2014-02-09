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

import javax.vecmath.Vector2f;

import org.terasology.asset.Assets;
import org.terasology.math.Vector2i;
import org.terasology.math.Vector3i;
import org.terasology.minimap.DisplayAxisType;
import org.terasology.registry.CoreRegistry;
import org.terasology.rendering.assets.texture.BasicTextureRegion;
import org.terasology.rendering.assets.texture.Texture;
import org.terasology.rendering.assets.texture.TextureRegion;
import org.terasology.rendering.nui.Canvas;
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

    private final Texture textureAtlas;
    private final TextureRegion questionMarkTextureRegion;

    private MapLocationIcon mapLocationIcon = new MapLocationIcon();

    private Vector2i relativeCellLocation;
    private Binding<Vector3i> centerLocationBinding = new DefaultBinding<>(null);
    private Binding<DisplayAxisType> displayAxisTypeBinding = new DefaultBinding<>(null);

    public MinimapCell() {

        textureAtlas = Assets.getTexture("engine:terrain");
        questionMarkTextureRegion = Assets.getTextureRegion("engine:items.questionMark");

        mapLocationIcon.bindIcon(new ReadOnlyBinding<TextureRegion>() {
            @Override
            public TextureRegion get() {
                Vector3i centerLocation = getCenterLocation();
                if (null == centerLocation) {
                    return null;
                }

                WorldProvider worldProvider = CoreRegistry.get(WorldProvider.class);

                DisplayAxisType displayAxis = getDisplayAxisType();
//                Vector3i coordinateAdjustment = getCoordinateAdjustment(displayAxis);
                Vector3i relativeLocation;
                switch (displayAxis) {
                    case XZ_AXIS: // top down view
                        relativeLocation = new Vector3i(-relativeCellLocation.x, 0, relativeCellLocation.y);
                        break;
                    case XY_AXIS:
                        relativeLocation = new Vector3i(-relativeCellLocation.y, -relativeCellLocation.x, 0);
                        break;
                    case YZ_AXIS:
                        relativeLocation = new Vector3i(0, -relativeCellLocation.x, -relativeCellLocation.y);
                        break;
                    default:
                        throw new RuntimeException("displayAxisType containts invalid value");
                }
                relativeLocation.add(centerLocation);
//                relativeLocation.add(coordinateAdjustment);
                Block block = worldProvider.getBlock(relativeLocation);
                if (null != block) {
                    // TODO: warning, no block
                    BlockAppearance primaryAppearance = block.getPrimaryAppearance();

                    BlockPart blockPart;
                    switch (displayAxis) {
                        case XZ_AXIS: // top down view
                            blockPart = BlockPart.TOP;
                            break;
                        case XY_AXIS:
                            blockPart = BlockPart.FRONT; // todo: front/left/right/back needs to be picked base on viewpoint
                            break;
                        case YZ_AXIS:
                            blockPart = BlockPart.LEFT; // todo: front/left/right/back needs to be picked base on viewpoint
                            break;
                        default:
                            throw new RuntimeException("displayAxisType containts invalid value");
                    }

                    // TODO: security issues
//                    WorldAtlas worldAtlas = CoreRegistry.get(WorldAtlas.class);
//                    float tileSize = worldAtlas.getRelativeTileSize();

                    float tileSize = 16f / 256f; // 256f could be replaced by textureAtlas.getWidth();
                    
                    Vector2f textureAtlasPos = primaryAppearance.getTextureAtlasPos(blockPart);

                    TextureRegion textureRegion = new BasicTextureRegion(textureAtlas, textureAtlasPos, new Vector2f(tileSize, tileSize));
                    return textureRegion;
                }

                return questionMarkTextureRegion;
            }
        });
    }

    @Override
    public void onDraw(Canvas canvas) {
        canvas.drawWidget(mapLocationIcon);
    }

    @Override
    public Vector2i getPreferredContentSize(Canvas canvas, Vector2i sizeHint) {
        return canvas.calculateRestrictedSize(mapLocationIcon, sizeHint);
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

    public void bindDisplayAxisType(Binding<DisplayAxisType> binding) {
        displayAxisTypeBinding = binding;
    }

    public DisplayAxisType getDisplayAxisType() {
        return displayAxisTypeBinding.get();
    }

    public void setDisplayAxisType(DisplayAxisType displayAxis) {
        displayAxisTypeBinding.set(displayAxis);
    }
}
