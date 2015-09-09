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

    private final TextureRegion questionMark;

    private Function<Block, TextureRegion> textureMap;

    private WorldProvider worldProvider;

    public MinimapCell(WorldProvider worldProvider, Function<Block, TextureRegion> textureMap) {

        this.textureMap = textureMap;
        this.questionMark = Assets.getTextureRegion("engine:items#questionMark").get();
        this.worldProvider = worldProvider;
    }

    public void draw(Canvas canvas, Vector3i startLocation) {
        Vector3i pos = new Vector3i(startLocation);

        Block block = worldProvider.getBlock(pos);
        Block top = block;
        if (isIgnored(block)) {
            do {
                pos.subY(1);
                if (!worldProvider.isBlockRelevant(pos)) {
                    canvas.drawTexture(questionMark);
                    return;
                }
                top = block;
                block = worldProvider.getBlock(pos);
            } while (isIgnored(block));
        } else {
            do {
                pos.addY(1);
                if (!worldProvider.isBlockRelevant(pos)) {
                    canvas.drawTexture(questionMark);
                    return;
                }
                block = top;
                top = worldProvider.getBlock(pos);
            } while (!isIgnored(top));
        }

        int dist = pos.getY() - startLocation.getY();
        int g = 128 + TeraMath.clamp(dist * 10, -128, 127);
        Color color = new Color(g, g, g);

        TextureRegion reg = textureMap.apply(block);
        canvas.drawTexture(reg, color);

        if (!top.isInvisible()) {
            reg = textureMap.apply(top);
            canvas.drawTexture(reg, color);
        }
    }

    private static boolean isIgnored(Block block) {
        return block.isPenetrable() && !block.isWater();
    }
}
