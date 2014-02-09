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
import org.terasology.rendering.nui.databinding.ReadOnlyBinding;
import org.terasology.rendering.nui.layers.hud.CoreHudWidget;
import org.terasology.rendering.nui.widgets.UILabel;

/**
 * @author mkienenb
 */
public class MinimapHUDElement  extends CoreHudWidget implements ControlWidget {

    private static final Logger logger = LoggerFactory.getLogger(MinimapHUDElement.class);

    private static final String MINIMAP_GRID_WIDGET_ID = "minimapGrid";
    private static final String MINIMAP_LABEL_WIDGET_ID = "orientationLabel";

    @In
    private LocalPlayer localPlayer;

    @Override
    public void initialise() {
        final MinimapGrid minimapGrid = find(MINIMAP_GRID_WIDGET_ID, MinimapGrid.class);
        minimapGrid.setCellOffset(10);

        EntityRef characterEntity = localPlayer.getCharacterEntity();
        minimapGrid.setTargetEntity(characterEntity);

        UILabel orientationLabel = find(MINIMAP_LABEL_WIDGET_ID, UILabel.class);
        orientationLabel.bindText(new ReadOnlyBinding<String>() {
            
            @Override
            public String get() {
                switch (minimapGrid.getDisplayAxisType()) {
                    case XY_AXIS:
                        return "x-y orientation";
                    case XZ_AXIS:
                        return "x-z orientation";
                    case YZ_AXIS:
                        return "y-z orientation";
                    default:
                        return "error determining display orientation";
                }
            }
        });

    }

    @Override
    public void onBindEvent(BindButtonEvent event) {
        if (event instanceof ToggleMinimapButton && event.isDown()) {
            setVisible(!isVisible());
            event.consume();
        } else if (event instanceof ToggleMinimapAxisButton && event.isDown()) {
            MinimapGrid minimapGrid = find(MINIMAP_GRID_WIDGET_ID, MinimapGrid.class);
            minimapGrid.toggleAxis();
            event.consume();
        }
    }
}
