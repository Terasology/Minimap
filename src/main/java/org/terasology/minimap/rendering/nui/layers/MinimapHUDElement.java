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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.input.BindButtonEvent;
import org.terasology.input.binds.minimap.ToggleMinimapAxisButton;
import org.terasology.input.binds.minimap.ToggleMinimapButton;
import org.terasology.logic.players.LocalPlayer;
import org.terasology.registry.In;
import org.terasology.rendering.nui.ControlWidget;
import org.terasology.rendering.nui.layers.hud.CoreHudWidget;

/**
 * @author mkienenb
 */
public class MinimapHUDElement  extends CoreHudWidget implements ControlWidget {

    private static final Logger logger = LoggerFactory.getLogger(MinimapHUDElement.class);

    private static final String MINIMAP_WIDGET_ID = "minimapGrid";

    @In
    private LocalPlayer localPlayer;

    @Override
    public void initialise() {
        MinimapGrid minimapGrid = find(MINIMAP_WIDGET_ID, MinimapGrid.class);
        minimapGrid.setCellOffset(10);

        EntityRef characterEntity = localPlayer.getCharacterEntity();
        minimapGrid.setTargetEntity(characterEntity);
    }

    @Override
    public void onBindEvent(BindButtonEvent event) {
        if (event instanceof ToggleMinimapButton && event.isDown()) {
            setVisible(!isVisible());
            event.consume();
        } else if (event instanceof ToggleMinimapAxisButton && event.isDown()) {
            MinimapGrid minimapGrid = find(MINIMAP_WIDGET_ID, MinimapGrid.class);
            minimapGrid.toggleAxis();
            event.consume();
        }
    }
}
