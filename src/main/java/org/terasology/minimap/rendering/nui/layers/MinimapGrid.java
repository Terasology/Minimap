// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.minimap.rendering.nui.layers;

import com.google.common.base.Preconditions;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.collect.LinkedHashMultimap;
import com.google.common.collect.Multimap;
import org.joml.Rectanglef;
import org.joml.Rectanglei;
import org.terasology.assets.ResourceUrn;
import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.logic.location.LocationComponent;
import org.terasology.math.JomlUtil;
import org.terasology.nui.Border;
import org.terasology.math.ChunkMath;
import org.terasology.math.Region3i;
import org.terasology.math.TeraMath;
import org.terasology.math.geom.BaseVector2i;
import org.terasology.math.geom.ImmutableVector2i;
import org.terasology.math.geom.Quat4f;
import org.terasology.math.geom.Rect2f;
import org.terasology.math.geom.Rect2i;
import org.terasology.math.geom.Vector2f;
import org.joml.Vector2i;
import org.terasology.math.geom.Vector3f;
import org.terasology.math.geom.Vector3i;
import org.terasology.minimap.MinimapIconComponent;
import org.terasology.minimap.overlays.MinimapOverlay;
import org.terasology.nui.util.RectUtility;
import org.terasology.rendering.assets.texture.BasicTextureRegion;
import org.terasology.rendering.assets.texture.Texture;
import org.terasology.rendering.assets.texture.TextureRegion;
import org.terasology.nui.Canvas;
import org.terasology.nui.Color;
import org.terasology.nui.CoreWidget;
import org.terasology.nui.ScaleMode;
import org.terasology.nui.SubRegion;
import org.terasology.nui.databinding.Binding;
import org.terasology.nui.databinding.DefaultBinding;
import org.terasology.nui.databinding.ReadOnlyBinding;
import org.terasology.rendering.nui.CanvasUtility;
import org.terasology.utilities.Assets;
import org.terasology.world.WorldProvider;
import org.terasology.world.block.Block;
import org.terasology.world.block.BlockAppearance;
import org.terasology.world.block.BlockPart;
import org.terasology.world.chunks.ChunkConstants;

import java.util.Collection;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Optional;
import java.util.PriorityQueue;
import java.util.Set;
import java.util.function.IntFunction;

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

    private final Set<EntityRef> alivePlayers = new HashSet<EntityRef>();

    private Texture textureAtlas;
    private TextureRegion questionMark;

    private Multimap<BaseVector2i, Vector3i> dirtyBlocks = LinkedHashMultimap.create();

    private WorldProvider worldProvider;

    private final Collection<MinimapOverlay> overlays =
            new PriorityQueue<>(Comparator.comparingInt(MinimapOverlay::getZOrder));

    private LoadingCache<Block, TextureRegion> cache =
            CacheBuilder.newBuilder().build(new CacheLoader<Block, TextureRegion>() {

                @Override
                public TextureRegion load(Block block) {
                    BlockAppearance primaryAppearance = block.getPrimaryAppearance();

                    BlockPart blockPart = BlockPart.TOP;

                    // TODO: security issues
                    // WorldAtlas worldAtlas = CoreRegistry.get(WorldAtlas.class);
                    // float tileSize = worldAtlas.getRelativeTileSize();

                    float tileSize = 16f / 256f; // 256f could be replaced by textureAtlas.getWidth();

                    Vector2f textureAtlasPos = primaryAppearance.getTextureAtlasPos(blockPart);

                    return new BasicTextureRegion(textureAtlas, textureAtlasPos, new Vector2f(tileSize, tileSize));
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

    public void updateLocation(org.joml.Vector3i worldLocation) {
        org.joml.Vector3i chunkPos = ChunkMath.calcChunkPos(worldLocation, new org.joml.Vector3i());
        org.joml.Vector3i blockPos = ChunkMath.calcRelativeBlockPos(worldLocation, new org.joml.Vector3i());
        dirtyBlocks.put(new ImmutableVector2i(chunkPos.x(), chunkPos.z()), JomlUtil.from(blockPos));
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
        int width = getPreferredContentSize().x();
        int height = getPreferredContentSize().y();
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
                        try (SubRegion ignored = CanvasUtility.subRegionFBO(canvas, urn, BUFFER_SIZE)) {
                            // use player's center Y pos to start searching for the surface layer
                            renderFullChunk(canvas, chunkX, chunkZ, centerY);
                        }
                        dirtyBlocks.removeAll(JomlUtil.from(chunkPos));
                        opt = Assets.get(urn, Texture.class);
                    }
                }

                // update dirty blocks in cache texture
                Collection<Vector3i> chunkBlocks = dirtyBlocks.get(JomlUtil.from(chunkPos));
                if (!chunkBlocks.isEmpty()) {
                    try (SubRegion ignored = CanvasUtility.subRegionFBO(canvas, urn, BUFFER_SIZE)) {
                        for (Vector3i pos : chunkBlocks) {
                            renderDirtyBlock(canvas, chunkX, chunkZ, pos);
                        }
                    }
                    dirtyBlocks.removeAll(JomlUtil.from(chunkPos));
                }

                // render the actual chunk FBO texture
                if (opt.isPresent()) {
                    try (SubRegion ignored = canvas.subRegion(canvas.getRegion(), true)) {
                        float tileX = numberOfCols * 0.5f + chunkX * ChunkConstants.SIZE_X - centerPosition.getX();
                        float tileZ = numberOfRows * 0.5f + chunkZ * ChunkConstants.SIZE_Z - centerPosition.getZ();

                        int offX = TeraMath.floorToInt(tileX * cellWidth);
                        int offZ = TeraMath.floorToInt(tileZ * cellHeight);

                        Rectanglei screenRegion = JomlUtil.rectangleiFromMinAndSize(offX, offZ, screenWidth, screenHeight);
                        canvas.drawTextureRaw(opt.get(), screenRegion, ScaleMode.SCALE_FIT, 0f, 1f, 1f, -1f);
                    }
                }

            }
        }

        // render overlays
        Rectanglef worldRect = RectUtility.createFromCenterAndSize(
                centerPosition.getX(), centerPosition.getZ(),
                (width / cellWidth), (height / cellHeight));

        try (SubRegion ignored = canvas.subRegion(canvas.getRegion(), true)) {
            for (MinimapOverlay overlay : overlays) {
                overlay.render(canvas, new Rectanglei((int) worldRect.minX, (int) worldRect.minY, (int) worldRect.maxX, (int) worldRect.maxY));
            }
        }

        drawPlayerArrows(canvas, zoom, centerX, centerZ);
    }

    private void renderFullChunk(Canvas canvas, int chunkX, int chunkZ, int startY) {
        for (int row = 0; row < ChunkConstants.SIZE_Z; row++) {
            for (int column = 0; column < ChunkConstants.SIZE_X; column++) {
                int x = column * CELL_SIZE.getX();
                int y = row * CELL_SIZE.getY();
                Rectanglei rect = JomlUtil.rectangleiFromMinAndSize(x, y, CELL_SIZE.getX(), CELL_SIZE.getY());

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
        Rectanglei rect = JomlUtil.rectangleiFromMinAndSize(x, y, CELL_SIZE.getX(), CELL_SIZE.getY());

        int blockX = chunkX * ChunkConstants.SIZE_X + column;
        int blockZ = chunkZ * ChunkConstants.SIZE_Z + row;
        Vector3i relLocation = new Vector3i(blockX, startY, blockZ);
        renderCell(canvas, rect, relLocation); // the y component of relLocation is modified!
    }

    private void drawPlayerArrows(Canvas canvas, float zoom, int centerX, int centerZ) {

        int width = getPreferredContentSize().x();
        int height = getPreferredContentSize().y();

        for (EntityRef alivePlayer : alivePlayers) {
            LocationComponent playerLocationComponent = alivePlayer.getComponent(LocationComponent.class);
            MinimapIconComponent minimapIconComponent = alivePlayer.getComponent(MinimapIconComponent.class);
            if (playerLocationComponent != null && minimapIconComponent != null) {

                Assets.getTexture(minimapIconComponent.iconUrn).ifPresent(icon -> {
                    //TODO UITexture should be used here, but it doesn't work
                    Assets.getMaterial("engine:UILitMesh").ifPresent(material -> {
                        material.setTexture("texture", icon);
                        Assets.getMesh("engine:UIBillboard").ifPresent(mesh -> {
                            // Drawing textures with rotation is not yet supported, see #1926
                            // We therefore use a workaround based on mesh drawing
                            // The width of the screenArea is doubled to avoid clipping issues when the texture is rotated
                            int arrowWidth = icon.getWidth() * 2;
                            int arrowHeight = icon.getHeight() * 2;
                            int arrowX = (width - arrowWidth) / 2;
                            int arrowY = (height - arrowHeight) / 2;
                            //canvas.drawTexture(arrowhead, arrowX, arrowY, rotation);

                            Vector3f playerPosition = new Vector3f(playerLocationComponent.getWorldPosition());
                            int xOffset = TeraMath.floorToInt((playerPosition.getX() - centerX) * CELL_SIZE.getX() * zoom);
                            int zOffset = TeraMath.floorToInt((playerPosition.getZ() - centerZ) * CELL_SIZE.getY() * zoom);

                            if (isInBounds(width, height, xOffset, zOffset)) {
                                // The scaling seems to be completely wrong - 0.8f looks ok
                                Quat4f q = playerLocationComponent.getWorldRotation();
                                // convert to Euler yaw angle
                                // TODO: move into quaternion
                                float rotation = -(float) Math.atan2(2.0 * (q.y * q.w + q.x * q.z), 1.0 - 2.0 * (q.y * q.y - q.z * q.z));
                                Rect2i screenArea = Rect2i.createFromMinAndSize(arrowX + xOffset, arrowY + zOffset, arrowWidth, arrowHeight);
                                CanvasUtility.drawMesh(canvas, mesh, material, screenArea, new Quat4f(0, 0, rotation), new Vector3f(), 0.8f);
                            }
                        });
                    });
                });
            }
        }
    }

    /**
     * Determine whether a coordinate is still in bounds given by `width` x `height` after applying an offset.
     *
     * @return true if the offset coordinates are within the bounds determined by `width`x`height`.
     */
    private boolean isInBounds(int width, int height, int xOffset, int zOffset) {
        return xOffset <= (width / 2) && xOffset >= -(width / 2)
                && zOffset <= (height / 2) && zOffset >= -(height / 2);
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

    private void renderCell(Canvas canvas, Rectanglei rect, Vector3i pos) {
        Block block = worldProvider.getBlock(pos);
        if (isIgnored(block)) {
            do {
                pos.subY(1);
                if (!worldProvider.isBlockRelevant(pos)) {
                    canvas.drawTexture(questionMark, rect);
                    return;
                }
                block = worldProvider.getBlock(pos);
            } while (isIgnored(block));
        } else {
            do {
                pos.addY(1);
                if (!worldProvider.isBlockRelevant(pos)) {
                    canvas.drawTexture(questionMark, rect);
                    return;
                }
                block = worldProvider.getBlock(pos);
            } while (!isIgnored(block));
            pos.subY(1);
        }

        float g = brightness.apply(pos.getY());
        Color color = new Color(g, g, g);

        TextureRegion reg = cache.getUnchecked(worldProvider.getBlock(pos));
        canvas.drawTexture(reg, rect, color);

        pos.addY(1);
        Block top = worldProvider.getBlock(pos);

        if (top.isDestructible()) {
            reg = cache.getUnchecked(top);
            canvas.drawTexture(reg, rect);
        }
    }

    private static boolean isIgnored(Block block) {
        return block.isPenetrable() && !block.isWater();
    }

    public void updateAlivePlayerList(Iterable<EntityRef> alivePlayersIterable) {
        for (EntityRef player : alivePlayersIterable) {
            alivePlayers.add(player);
        }
    }

    public void addAlivePlayer(EntityRef player) {
        alivePlayers.add(player);
    }

    public void removeAlivePlayer(EntityRef player) {
        alivePlayers.remove(player);
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
        overlays.remove(overlay);
    }

}
