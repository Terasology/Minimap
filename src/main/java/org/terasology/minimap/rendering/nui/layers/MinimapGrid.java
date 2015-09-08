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


import java.util.Optional;

import org.terasology.asset.Assets;
import org.terasology.assets.ResourceUrn;
import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.logic.characters.CharacterComponent;
import org.terasology.logic.location.LocationComponent;
import org.terasology.logic.players.LocalPlayer;
import org.terasology.math.Border;
import org.terasology.math.ChunkMath;
import org.terasology.math.TeraMath;
import org.terasology.math.geom.Quat4f;
import org.terasology.math.geom.Rect2i;
import org.terasology.math.geom.Vector2f;
import org.terasology.math.geom.Vector2i;
import org.terasology.math.geom.Vector3i;
import org.terasology.math.geom.Vector3f;
import org.terasology.registry.CoreRegistry;
import org.terasology.rendering.assets.material.Material;
import org.terasology.rendering.assets.mesh.Mesh;
import org.terasology.rendering.assets.texture.BasicTextureRegion;
import org.terasology.rendering.assets.texture.Texture;
import org.terasology.rendering.assets.texture.TextureRegion;
import org.terasology.rendering.nui.Canvas;
import org.terasology.rendering.nui.CoreWidget;
import org.terasology.rendering.nui.ScaleMode;
import org.terasology.rendering.nui.SubRegion;
import org.terasology.rendering.nui.databinding.Binding;
import org.terasology.rendering.nui.databinding.DefaultBinding;
import org.terasology.rendering.nui.databinding.ReadOnlyBinding;
import org.terasology.world.WorldProvider;
import org.terasology.world.block.Block;
import org.terasology.world.block.BlockAppearance;
import org.terasology.world.block.BlockPart;
import org.terasology.world.chunks.ChunkConstants;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;

/**
 * @author mkienenb
 */
public class MinimapGrid extends CoreWidget {

    private static final Vector2i CELL_SIZE = new Vector2i(8, 8);

    private MinimapCell cell;

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
        textureAtlas = Assets.getTexture("engine:terrain").get();

        WorldProvider worldProvider = CoreRegistry.get(WorldProvider.class);
        cell = new MinimapCell(worldProvider, cache::getUnchecked);
    }


    @Override
    public void update(float delta) {
        super.update(delta);
    }

    @Override
    public void onDraw(Canvas canvas) {

        Vector3f worldPosition = null;

        LocalPlayer localPlayer = CoreRegistry.get(LocalPlayer.class);
        EntityRef entity = localPlayer.getCharacterEntity();
        LocationComponent locationComponent = entity.getComponent(LocationComponent.class);
        CharacterComponent character = localPlayer.getCharacterEntity().getComponent(CharacterComponent.class);
        float rotation = (float) ((character != null) ? -character.yaw * Math.PI / 180f : 0);
        if (null != locationComponent) {
            worldPosition = locationComponent.getWorldPosition();
        } else {
            return;
        }

        Vector3i centerPosition = new Vector3i(worldPosition);
        // From top view, see what we're walking on, not what's at knee level
        centerPosition.sub(0, 1, 0);

        // define zoom factor
        int zoomLevel = getViewingAxisOffset();
        float zoomDelta = 0.25f;
        float zoom = 1; //(float) Math.pow(2.0, zoomLevel * zoomDelta);
        int width = getPreferredContentSize().getX();
        int height = getPreferredContentSize().getY();
        int numberOfRows = TeraMath.ceilToInt(height / (zoom * CELL_SIZE.getY()));
        int numberOfCols = TeraMath.ceilToInt(width / (zoom * CELL_SIZE.getX()));

        int rowCenter = (int) ((numberOfRows + 0.5f) / 2f);
        int columnCenter = (int) ((numberOfCols + 0.5f) / 2f);

        int centerX = centerPosition.getX();
        int centerZ = centerPosition.getZ();
        Vector3i minChunkPos = ChunkMath.calcChunkPos(centerX - columnCenter, 0, centerZ - rowCenter);
        Vector3i maxChunkPos = ChunkMath.calcChunkPos(centerX + columnCenter, 0, centerZ + rowCenter);

        for (int chunkZ = minChunkPos.getZ(); chunkZ <= maxChunkPos.getZ(); chunkZ++) {
            for (int chunkX = minChunkPos.getX(); chunkX <= maxChunkPos.getX(); chunkX++) {

                ResourceUrn urn = new ResourceUrn("Minimap:gridcache" + chunkX + "x" + chunkZ);
                Optional<Texture> opt = Assets.get(urn, Texture.class);
                int bufferWidth = CELL_SIZE.getX() * ChunkConstants.SIZE_X;
                int bufferHeight = CELL_SIZE.getY() * ChunkConstants.SIZE_Z;
                Vector2i size = new Vector2i(bufferWidth, bufferHeight);
                if (!opt.isPresent()) {
                    try (SubRegion ignored = canvas.subRegionFBO(urn, size)) {
                        for (int row = 0; row < ChunkConstants.SIZE_Z; row++) {
                            for (int column = 0; column < ChunkConstants.SIZE_X; column++) {
                                int x = column * CELL_SIZE.x;
                                int y = row * CELL_SIZE.y;
                                Rect2i rect = Rect2i.createFromMinAndSize(x, y, CELL_SIZE.x, CELL_SIZE.y);

                                int blockX = chunkX * ChunkConstants.SIZE_X + column;
                                int blockZ = chunkZ * ChunkConstants.SIZE_Z + row;
                                Vector3i relLocation = new Vector3i(blockX, 9, blockZ);

                                try (SubRegion ignored2 = canvas.subRegion(rect, false)) {
                                    cell.draw(canvas, relLocation);
                                }
                            }
                        }
                    }

                    opt = Assets.get(urn, Texture.class);
                }

                try (SubRegion ignored = canvas.subRegion(canvas.getRegion(), true)) {
                    int offX = centerX - columnCenter - chunkX * ChunkConstants.SIZE_X;
                    int offZ = centerZ - rowCenter - chunkZ * ChunkConstants.SIZE_Z;
                    offX *= CELL_SIZE.x;
                    offZ *= CELL_SIZE.y;

                    Rect2i screenRegion = Rect2i.createFromMinAndSize(-offX, -offZ, size.getX(), size.getY());
                    canvas.drawTextureRaw(opt.get(), screenRegion, ScaleMode.SCALE_FIT, 0, 1f, 1f, -1f);
                }
            }
        }

        // draw arrowhead
        Texture arrowhead = Assets.getTexture("Minimap:arrowhead").get();
        // Drawing textures with rotation is not yet supported, see #1926
        // We therefore use a workaround based on mesh drawing
        // The width of the screenArea is doubled to avoid clipping issues when the texture is rotated
        int arrowWidth = arrowhead.getWidth() * 2;
        int arrowHeight = arrowhead.getHeight() * 2;
        int arrowX = (width - arrowWidth) / 2;
        int arrowY = (height - arrowHeight) / 2;
        Rect2i screenArea = Rect2i.createFromMinAndSize(arrowX, arrowY, arrowWidth, arrowHeight);
//        canvas.drawTexture(arrowhead, arrowX, arrowY, rotation);

        // UITexture should be used here, but it doesn't work
        Material material = Assets.getMaterial("engine:UILitMesh").get();
        material.setTexture("texture", arrowhead);
        Mesh mesh = Assets.getMesh("engine:UIBillboard").get();
        // The scaling seems to be completely wrong - 0.8f looks ok
        canvas.drawMesh(mesh, material, screenArea, new Quat4f(0, 0, rotation), new Vector3f(), 0.8f);
    }

    @Override
    public Vector2i getPreferredContentSize(Canvas canvas, Vector2i sizeHint) {
        Border border = canvas.getCurrentStyle().getBackgroundBorder();
        Vector2i size = getPreferredContentSize();
        int width = size.x + border.getTotalWidth();
        int height = size.y + border.getTotalHeight();
        return new Vector2i(width, height);
    }

    public Vector2i getPreferredContentSize() {
        return new Vector2i(320, 200);
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