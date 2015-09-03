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

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.terasology.asset.Assets;
import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.logic.players.LocalPlayer;
import org.terasology.math.geom.Rect2i;
import org.terasology.math.geom.Vector2f;
import org.terasology.math.geom.Vector2i;
import org.terasology.math.geom.Vector3i;
import org.terasology.math.geom.Vector3f;
import org.terasology.registry.CoreRegistry;
import org.terasology.rendering.assets.texture.BasicTextureRegion;
import org.terasology.rendering.assets.texture.Texture;
import org.terasology.rendering.assets.texture.TextureRegion;
import org.terasology.rendering.nui.Canvas;
import org.terasology.rendering.nui.CoreWidget;
import org.terasology.rendering.nui.UIWidget;
import org.terasology.rendering.nui.databinding.Binding;
import org.terasology.rendering.nui.databinding.DefaultBinding;
import org.terasology.rendering.nui.databinding.ReadOnlyBinding;
import org.terasology.world.WorldProvider;
import org.terasology.world.block.Block;
import org.terasology.world.block.BlockAppearance;
import org.terasology.world.block.BlockPart;

import com.google.common.base.Function;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.collect.Iterators;

/**
 * @author mkienenb
 */
public class MinimapGrid extends CoreWidget {

    private int numberOfColumns = 15;
    private int numberOfRows = 15;

    private MinimapCell[][] cells;

    private Binding<EntityRef> targetEntityBinding = new DefaultBinding<>(EntityRef.NULL);
    private Binding<Integer> cellOffsetBinding = new DefaultBinding<>(0);
    private Binding<Integer> viewingAxisOffsetBinding = new DefaultBinding<>(0);

    private Texture textureAtlas;

    private LoadingCache<Block, TextureRegion> cache = CacheBuilder.newBuilder().build(new CacheLoader<Block, TextureRegion>() {

        @Override
        public TextureRegion load(Block block) {
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

    });

    public MinimapGrid() {
    }


    public MinimapGrid(int numberOfColumns, int numberOfRows) {
        this.numberOfColumns = numberOfColumns;
        this.numberOfRows = numberOfRows;
    }

    @Override
    public void update(float delta) {
        super.update(delta);
        if (null == cells) {
            initialize();
        }
    }

    private void initialize() {

        textureAtlas = Assets.getTexture("engine:terrain").get();

        int rowCenter = (int) ((numberOfRows + 0.5f) / 2f);
        int columnCenter = (int) ((numberOfColumns + 0.5f) / 2f);

        WorldProvider worldProvider = CoreRegistry.get(WorldProvider.class);

        cells = new MinimapCell[numberOfRows][numberOfColumns];
        for (int row = 0; row < numberOfRows; row++) {
            cells[row] = new MinimapCell[numberOfColumns];

            for (int column = 0; column < numberOfColumns; column++) {
                MinimapCell cell = new MinimapCell(worldProvider, cache::getUnchecked);
                cells[row][column] = cell;

                cell.setRelativeCellLocation(new Vector2i((column - columnCenter), (row - rowCenter)));

                cell.bindCenterLocation(new ReadOnlyBinding<Vector3i>() {
                    @Override
                    public Vector3i get() {

                        Vector3f worldPosition = null;

                        // TODO: Figure out how to fix this
                        // Currently the character entity doesn't have a valid LocationComponent, which seems weird.
                        // So skip allowing arbitrary entities for now.

//                            EntityRef entity = getTargetEntity();
//                            if (null != entity) {
//                                LocationComponent locationComponent = entity.getComponent(LocationComponent.class);
//                                if (null != locationComponent) {
//                                    worldPosition = locationComponent.getWorldPosition();
//                                } else {
//                                    logger.error("No locationComponent for target entity " + entity);
//                                }
//                            } else {
//                                logger.error("No target entity");
//                            }

                        if (null == worldPosition) {
                            LocalPlayer localPlayer = CoreRegistry.get(LocalPlayer.class);
                            worldPosition = localPlayer.getPosition();
                        }

                        Vector3i blockPosition = new Vector3i(Math.round(worldPosition.x), Math.round(worldPosition.y), Math.round(worldPosition.z));
                        // From top view, see what we're walking on, not what's at knee level
                        blockPosition.sub(0, 1, 0);

                        int offset = getViewingAxisOffset();
                        blockPosition.add(0, 0, offset);
                        return blockPosition;
                    }
                });
            }
        }
    }

    @Override
    public void onDraw(Canvas canvas) {
        if (null == cells) {
            initialize();
        }

        if (null != cells) {
            Vector2i cellSize = canvas.calculatePreferredSize(cells[0][0]);

            canvas.drawBackground();

            for (int row = 0; row < numberOfRows; row++) {
                for (int column = 0; column < numberOfColumns; column++) {
                    MinimapCell cell = cells[row][column];
                    int horizPos = row;
                    int vertPos = column;
                    canvas.drawWidget(cell, Rect2i.createFromMinAndSize(horizPos * cellSize.x, vertPos * cellSize.y, cellSize.x, cellSize.y));
                }
            }
        }
    }

    @Override
    public Vector2i getPreferredContentSize(Canvas canvas, Vector2i sizeHint) {
        if (null == cells) {
            initialize();
        }

        if (null != cells) {
            Vector2i cellSize = canvas.calculatePreferredSize(cells[0][0]);
            return new Vector2i(numberOfRows * cellSize.x, numberOfColumns * cellSize.y);
        }
        return Vector2i.zero();
    }

    @Override
    public Iterator<UIWidget> iterator() {
        if (null == cells) {
            initialize();
        }

        List<MinimapCell> cellList;
        if (null != cells) {
            cellList = new ArrayList<MinimapCell>(numberOfRows * numberOfColumns);

            for (int row = 0; row < numberOfRows; row++) {
                for (int column = 0; column < numberOfColumns; column++) {
                    MinimapCell cell = cells[row][column];
                    cellList.add(cell);
                }
            }
        } else {
            cellList = Collections.emptyList();
        }

        return Iterators.transform(cellList.iterator(), new Function<UIWidget, UIWidget>() {
            @Override
            public UIWidget apply(UIWidget input) {
                return input;
            }
        });
    }

    public void bindTargetEntity(Binding<EntityRef> binding) {
        targetEntityBinding = binding;
    }

    public EntityRef getTargetEntity() {
        return targetEntityBinding.get();
    }

    public void setTargetEntity(EntityRef val) {
        targetEntityBinding.set(val);
    }

    public void bindCellOffset(Binding<Integer> binding) {
        cellOffsetBinding = binding;
    }

    public int getCellOffset() {
        return cellOffsetBinding.get();
    }

    public void setCellOffset(int val) {
        cellOffsetBinding.set(val);
    }

    public int getViewingAxisOffset() {
        return viewingAxisOffsetBinding.get();
    }

    public void setViewingAxisOffset(int val) {
        viewingAxisOffsetBinding.set(val);
    }

    public void bindViewingAxisOffset(ReadOnlyBinding<Integer> offsetBinding) {
        viewingAxisOffsetBinding = offsetBinding;
    }
}
