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

import java.util.function.Function;

import org.terasology.asset.Assets;
import org.terasology.math.TeraMath;
import org.terasology.math.geom.Vector3i;
import org.terasology.rendering.assets.texture.TextureRegion;
import org.terasology.rendering.nui.Canvas;
import org.terasology.rendering.nui.Color;
import org.terasology.world.WorldProvider;
import org.terasology.world.block.Block;

/**
 * @author Immortius
 */
public class MinimapCell {

    private static final int MINIMAP_TRANSPARENCY = 255;
    private static final Color MINIMAP_TRANSPARENCY_COLOR = Color.WHITE.alterAlpha(MINIMAP_TRANSPARENCY);

    private final TextureRegion questionMark;

    private Function<Block, TextureRegion> textureMap;

    private WorldProvider worldProvider;

    public MinimapCell(WorldProvider worldProvider, Function<Block, TextureRegion> textureMap) {

        this.textureMap = textureMap;
        this.questionMark = Assets.getTextureRegion("engine:items#questionMark").get();
        this.worldProvider = worldProvider;
    }

    public void draw(Canvas canvas, Vector3i startLocation) {
        Vector3i relativeLocation = new Vector3i(startLocation);
        Entry<Vector3i, Block> result = findSurface(relativeLocation);

        TextureRegion reg;
        Color color;
        if (result == null) {
            color = MINIMAP_TRANSPARENCY_COLOR;
            reg = questionMark;
        } else {
            reg = textureMap.apply(result.value);
            int dist = result.key.getY() - relativeLocation.getY();
            int g = 128 + TeraMath.clamp(dist * 10, -128, 127);
            color = new Color(g, g, g, MINIMAP_TRANSPARENCY);
        }

        canvas.drawTexture(reg, color);
    }

    private Entry<Vector3i, Block> findSurface(Vector3i startPos) {

        Vector3i pos = new Vector3i(startPos);

        Block block = worldProvider.getBlock(pos);
        if (isIgnored(block)) {
            do {
                pos.subY(1);
                if (!worldProvider.isBlockRelevant(pos)) {
                    return null;
                }
                block = worldProvider.getBlock(pos);
            } while (isIgnored(block));
        } else {
            Block next = block;
            do {
                pos.addY(1);
                if (!worldProvider.isBlockRelevant(pos)) {
                    return null;
                }
                block = next;
                next = worldProvider.getBlock(pos);
            } while (!isIgnored(next));
        }

        return new Entry<Vector3i, Block>(pos, block);
    }

    private static boolean isIgnored(Block block) {
        return block.isPenetrable() && !block.isWater();
    }

    private static class Entry<K, V> {

        private K key;
        private V value;

        public Entry(K key, V value) {
            this.key = key;
            this.value = value;
        }

    }
}
