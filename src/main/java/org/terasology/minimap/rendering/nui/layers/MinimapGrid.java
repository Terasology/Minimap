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


import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.PriorityQueue;
import java.util.function.IntFunction;

import org.terasology.assets.ResourceUrn;
import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.logic.location.LocationComponent;
import org.terasology.math.Border;
import org.terasology.math.ChunkMath;
import org.terasology.math.Region3i;
import org.terasology.math.TeraMath;
import org.terasology.math.geom.BaseVector2i;
import org.terasology.math.geom.ImmutableVector2i;
import org.terasology.math.geom.Quat4f;
import org.terasology.math.geom.Rect2f;
import org.terasology.math.geom.Rect2i;
import org.terasology.math.geom.Vector2f;
import org.terasology.math.geom.Vector2i;
import org.terasology.math.geom.Vector3f;
import org.terasology.math.geom.Vector3i;
import org.terasology.minimap.overlays.MinimapOverlay;
import org.terasology.rendering.assets.material.Material;
import org.terasology.rendering.assets.mesh.Mesh;
import org.terasology.rendering.assets.texture.BasicTextureRegion;
import org.terasology.rendering.assets.texture.Texture;
import org.terasology.rendering.assets.texture.TextureRegion;
import org.terasology.rendering.nui.Canvas;
import org.terasology.rendering.nui.Color;
import org.terasology.rendering.nui.CoreWidget;
import org.terasology.rendering.nui.ScaleMode;
import org.terasology.rendering.nui.SubRegion;
import org.terasology.rendering.nui.databinding.Binding;
import org.terasology.rendering.nui.databinding.DefaultBinding;
import org.terasology.rendering.nui.databinding.ReadOnlyBinding;
import org.terasology.utilities.Assets;
import org.terasology.world.WorldProvider;
import org.terasology.world.block.Block;
import org.terasology.world.block.BlockAppearance;
import org.terasology.world.block.BlockPart;
import org.terasology.world.chunks.ChunkConstants;

import com.google.common.base.Preconditions;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.collect.LinkedHashMultimap;
import com.google.common.collect.Multimap;

/**
 * This is the actual minimap. All rendering-related code is located here.
 */
public class MinimapGrid extends CoreWidget {

    /**
     * The size of a cell (i.e. represents one block)
     */
    private static final ImmutableVector2i CELL_SIZE = new ImmutableVector2i(4, 4);
    private static final ImmutableVector2i BUFFER_SIZE = new ImmutableVector2i(
            CELL_SIZE.getX() * ChunkConstants.SIZE_X, CELL_SIZE.getY() * ChunkConstants.SIZE_Z);

    /**
     * The delta scale factor
     */
    private static final float ZOOM_DELTA = 0.25f;

    private Binding<EntityRef> targetEntityBinding = new DefaultBinding<>(EntityRef.NULL);
    private Binding<Integer> zoomFactorBinding = new DefaultBinding<>(0);

    private Texture textureAtlas;
    private TextureRegion questionMark;

    private Multimap<BaseVector2i, Vector3i> dirtyBlocks = LinkedHashMultimap.create();

    private WorldProvider worldProvider;

    private final Collection<MinimapOverlay> overlays = new PriorityQueue<>((o1, o2) ->
            Integer.compare(o1.getZOrder(), o2.getZOrder()));

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

    private IntFunction<Float> brightness;

    public MinimapGrid() {
        textureAtlas = Assets.getTexture("engine:terrain").get();
        questionMark = Assets.getTextureRegion("engine:items#questionMark").get();
    }

    public void setHeightRange(int bottom, int top) {
        Preconditions.checkArgument(top > bottom);

        float minBright = 0.5f;
        float fac = (1 - minBright) / (top - bottom);
        brightness = y -> TeraMath.clamp(minBright + (y - bottom) * fac);
    }

    public void setWorldProvider(WorldProvider worldProvider) {
        this.worldProvider = worldProvider;
    }

    @Override
    public void update(float delta) {
        super.update(delta);
    }

    public void updateLocation(Vector3i worldLocation) {
        Vector3i chunkPos = ChunkMath.calcChunkPos(worldLocation);
        Vector3i blockPos = ChunkMath.calcBlockPos(worldLocation);
        dirtyBlocks.put(new ImmutableVector2i(chunkPos.getX(), chunkPos.getZ()), blockPos);
    }

    @Override
    public void onDraw(Canvas canvas) {

        EntityRef entity = getTargetEntity();
        LocationComponent locationComponent = entity.getComponent(LocationComponent.class);
        if (locationComponent == null) {
            return;
        }

        Vector3f centerPosition = new Vector3f(locationComponent.getWorldPosition());
        // See what we're walking on, not what's at knee level
        centerPosition.subY(1);

        // the location (0/0) is at the center of the block 0/0
        // we therefore add an offset of 0.5 to the map to reflect this fact
        centerPosition.addX(0.5f);
        centerPosition.addZ(0.5f);

        // define zoom factor
        int zoomLevel = getZoomFactor();
        float zoom = (float) Math.pow(2.0, zoomLevel * ZOOM_DELTA);
        int width = getPreferredContentSize().getX();
        int height = getPreferredContentSize().getY();
        float numberOfRows = height / (zoom * CELL_SIZE.getY());
        float numberOfCols = width / (zoom * CELL_SIZE.getX());

        int rowCenter = TeraMath.ceilToInt(numberOfRows * 0.5f);
        int colCenter = TeraMath.ceilToInt(numberOfCols * 0.5f);

        int centerX = TeraMath.floorToInt(centerPosition.getX());
        int centerY = TeraMath.floorToInt(centerPosition.getY());
        int centerZ = TeraMath.floorToInt(centerPosition.getZ());
        Vector3i minChunkPos = ChunkMath.calcChunkPos(centerX - colCenter, 0, centerZ - rowCenter);
        Vector3i maxChunkPos = ChunkMath.calcChunkPos(centerX + colCenter, 0, centerZ + rowCenter);

        int screenWidth = TeraMath.ceilToInt(BUFFER_SIZE.getX() * zoom);
        int screenHeight = TeraMath.ceilToInt(BUFFER_SIZE.getY() * zoom);

        Vector2i chunkPos = new Vector2i();
        Vector3i chunkDisc = new Vector3i(ChunkConstants.SIZE_X, ChunkConstants.SIZE_Y * 2, ChunkConstants.SIZE_Z);
        float cellWidth = CELL_SIZE.getX() * zoom;
        float cellHeight = CELL_SIZE.getY() * zoom;
        for (int chunkZ = minChunkPos.getZ(); chunkZ <= maxChunkPos.getZ(); chunkZ++) {
            for (int chunkX = minChunkPos.getX(); chunkX <= maxChunkPos.getX(); chunkX++) {
                chunkPos.set(chunkX, chunkZ);
                ResourceUrn urn = new ResourceUrn("Minimap:gridcache" + chunkX + "x" + chunkZ);
                Optional<? extends TextureRegion> opt = Assets.get(urn, Texture.class);

                // create and render to FBO texture asset, if needed
                if (!opt.isPresent()) {
                    Vector3i worldPos = new Vector3i(chunkX * ChunkConstants.SIZE_X, 0, chunkZ * ChunkConstants.SIZE_Z);
                    Region3i region = Region3i.createFromMinAndSize(worldPos, chunkDisc);
                    if (worldProvider.isRegionRelevant(region)) {
                        try (SubRegion ignored = canvas.subRegionFBO(urn, BUFFER_SIZE)) {
                            // use player's center Y pos to start searching for the surface layer
                            renderFullChunk(canvas, chunkX, chunkZ, centerY);
                        }
                        dirtyBlocks.removeAll(chunkPos);
                        opt = Assets.get(urn, Texture.class);
                    }
                }

                // update dirty blocks in cache texture
                Collection<Vector3i> chunkBlocks = dirtyBlocks.get(chunkPos);
                if (!chunkBlocks.isEmpty()) {
                    try (SubRegion ignored = canvas.subRegionFBO(urn, BUFFER_SIZE)) {
                        for (Vector3i pos : chunkBlocks) {
                            renderDirtyBlock(canvas, chunkX, chunkZ, pos);
                        }
                    }
                    dirtyBlocks.removeAll(chunkPos);
                }

                // render the actual chunk FBO texture
                if (opt.isPresent()) {
                    try (SubRegion ignored = canvas.subRegion(canvas.getRegion(), true)) {
                        float tileX = numberOfCols * 0.5f + chunkX * ChunkConstants.SIZE_X - centerPosition.getX();
                        float tileZ = numberOfRows * 0.5f + chunkZ * ChunkConstants.SIZE_Z - centerPosition.getZ();

                        int offX = TeraMath.floorToInt(tileX * cellWidth);
                        int offZ = TeraMath.floorToInt(tileZ * cellHeight);

                        Rect2i screenRegion = Rect2i.createFromMinAndSize(offX, offZ, screenWidth, screenHeight);
                        canvas.drawTextureRaw(opt.get(), screenRegion, ScaleMode.SCALE_FIT, 0f, 1f, 1f, -1f);
                    }
                }

            }
        }

        // render overlays
        Rect2f worldRect = Rect2f.createFromCenterAndSize(
                centerPosition.getX(), centerPosition.getZ(),
                (width / cellWidth), (height / cellHeight));

        try (SubRegion ignored = canvas.subRegion(canvas.getRegion(), true)) {
            for (MinimapOverlay overlay : overlays) {
                overlay.render(canvas, worldRect);
            }
        }

        drawArrow(canvas, locationComponent);
    }


    private void renderFullChunk(Canvas canvas, int chunkX, int chunkZ, int startY) {
        for (int row = 0; row < ChunkConstants.SIZE_Z; row++) {
            for (int column = 0; column < ChunkConstants.SIZE_X; column++) {
                int x = column * CELL_SIZE.getX();
                int y = row * CELL_SIZE.getY();
                Rect2i rect = Rect2i.createFromMinAndSize(x, y, CELL_SIZE.getX(), CELL_SIZE.getY());

                int blockX = chunkX * ChunkConstants.SIZE_X + column;
                int blockZ = chunkZ * ChunkConstants.SIZE_Z + row;
                Vector3i relLocation = new Vector3i(blockX, startY, blockZ);
                renderCell(canvas, rect, relLocation); // the y component of relLocation is modified!
            }
        }
    }

    private void renderDirtyBlock(Canvas canvas, int chunkX, int chunkZ, Vector3i pos) {
        int column = pos.x();
        int row = pos.z();
        int startY = pos.getY();
        int x = column * CELL_SIZE.getX();
        int y = row * CELL_SIZE.getY();
        Rect2i rect = Rect2i.createFromMinAndSize(x, y, CELL_SIZE.getX(), CELL_SIZE.getY());

        int blockX = chunkX * ChunkConstants.SIZE_X + column;
        int blockZ = chunkZ * ChunkConstants.SIZE_Z + row;
        Vector3i relLocation = new Vector3i(blockX, startY, blockZ);
        renderCell(canvas, rect, relLocation); // the y component of relLocation is modified!
    }

    private void drawArrow(Canvas canvas, LocationComponent locationComponent) {
        // draw arrowhead
        Texture arrowhead = Assets.getTexture("Minimap:arrowhead").get();
        // Drawing textures with rotation is not yet supported, see #1926
        // We therefore use a workaround based on mesh drawing
        // The width of the screenArea is doubled to avoid clipping issues when the texture is rotated
        int width = getPreferredContentSize().getX();
        int height = getPreferredContentSize().getY();
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
        Quat4f q = locationComponent.getWorldRotation();
        // convert to Euler yaw angle
        // TODO: move into quaternion
        float rotation = -(float) Math.atan2(2.0 * (q.y * q.w + q.x * q.z), 1.0 - 2.0 * (q.y * q.y - q.z * q.z));
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

    public int getZoomFactor() {
        return zoomFactorBinding.get();
    }

    public void setZoomFactor(int val) {
        zoomFactorBinding.set(val);
    }

    public void bindZoomFactor(ReadOnlyBinding<Integer> offsetBinding) {
        zoomFactorBinding = offsetBinding;
    }

    private void renderCell(Canvas canvas, Rect2i rect, Vector3i pos) {

        Block block = worldProvider.getBlock(pos);
        Block top = block;
        if (isIgnored(block)) {
            do {
                pos.subY(1);
                if (!worldProvider.isBlockRelevant(pos)) {
                    canvas.drawTexture(questionMark, rect);
                    return;
                }
                top = block;
                block = worldProvider.getBlock(pos);
            } while (isIgnored(block));
        } else {
            do {
                pos.addY(1);
                if (!worldProvider.isBlockRelevant(pos)) {
                    canvas.drawTexture(questionMark, rect);
                    return;
                }
                block = top;
                top = worldProvider.getBlock(pos);
            } while (!isIgnored(top));
            pos.subY(1);
        }

        float g = brightness.apply(pos.getY());
        Color color = new Color(g, g, g);

        TextureRegion reg = cache.getUnchecked(block);
        canvas.drawTexture(reg, rect, color);

        if (!top.isInvisible()) {
            reg = cache.getUnchecked(top);
            canvas.drawTexture(reg, rect);
        }
    }

    private static boolean isIgnored(Block block) {
        return block.isPenetrable() && !block.isWater();
    }

    /**
     * @param overlay the overlay to add
     */
    public void addOverlay(MinimapOverlay overlay) {
        overlays.add(overlay);
    }

    /**
     * @param overlay the overlay to remove
     */
    public void removeOverlay(MinimapOverlay overlay) {
        overlays.add(overlay);
    }

}
