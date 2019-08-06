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

import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.math.TeraMath;
import org.terasology.math.geom.Vector3i;
import org.terasology.minimap.overlays.MinimapOverlay;
import org.terasology.rendering.nui.databinding.ReadOnlyBinding;
import org.terasology.rendering.nui.layers.hud.CoreHudWidget;
import org.terasology.rendering.nui.widgets.UISlider;
import org.terasology.world.WorldProvider;

import java.util.Set;

/**
 * The minimap container that arranges the actual minimap and the zoon slider
 * in a single HUD element.
 */
public class MinimapHUDElement extends CoreHudWidget {

    private static final String MINIMAP_GRID_WIDGET_ID = "minimapGrid";
    private static final String MINIMAP_ZOOM_SLIDER_WIDGET_ID = "minimapOffsetSlider";

    private UISlider minimapZoomSlider;
    private MinimapGrid minimapGrid;

    @Override
    public void initialise() {
        minimapGrid = find(MINIMAP_GRID_WIDGET_ID, MinimapGrid.class);
        minimapZoomSlider = find(MINIMAP_ZOOM_SLIDER_WIDGET_ID, UISlider.class);

        final UISlider minimapOffsetSlider = find(MINIMAP_ZOOM_SLIDER_WIDGET_ID, UISlider.class);
        minimapOffsetSlider.setValue(0);
        minimapOffsetSlider.setMinimum(-10);
        minimapOffsetSlider.setRange(20);
        minimapOffsetSlider.setIncrement(1);

        minimapGrid.bindZoomFactor(new ReadOnlyBinding<Integer>() {
            @Override
            public Integer get() {
                return (int) minimapOffsetSlider.getValue();
            }
        });
    }

    public void changeZoom(int delta) {
        float min = minimapZoomSlider.getMinimum();
        float range = minimapZoomSlider.getRange();
        float increment = minimapZoomSlider.getIncrement();
        float oldValue = minimapZoomSlider.getValue();
        float newValue = TeraMath.clamp(oldValue + delta * increment, min, min + range);
        minimapZoomSlider.setValue(newValue);
    }

    public void updateLocation(Vector3i worldLocation) {
        minimapGrid.updateLocation(worldLocation);
    }

    public EntityRef getTargetEntity() {
        return minimapGrid.getTargetEntity();
    }

    public void setTargetEntity(EntityRef characterEntity) {
        minimapGrid.setTargetEntity(characterEntity);
    }

    public void bindTargetEntity(ReadOnlyBinding<EntityRef> binding) {
        minimapGrid.bindTargetEntity(binding);
    }

    public void updateAlivePlayerList(Iterable<EntityRef> alivePlayersIterable) {
        minimapGrid.updateAlivePlayerList(alivePlayersIterable);
    }

    public void addAlivePlayer(EntityRef player) {
        minimapGrid.addAlivePlayer(player);
    }

    public void removeAlivePlayer(EntityRef player) {
        minimapGrid.removeAlivePlayer(player);
    }

    public void setHeightRange(int bottom, int top) {
        minimapGrid.setHeightRange(bottom, top);
    }

    public void setWorldProvider(WorldProvider worldProvider) {
        minimapGrid.setWorldProvider(worldProvider);
    }

    /**
     * @param overlay the overlay to add
     */
    public void addOverlay(MinimapOverlay overlay) {
        minimapGrid.addOverlay(overlay);
    }

    /**
     * @param overlay the overlay to remove
     */
    public void removeOverlay(MinimapOverlay overlay) {
        minimapGrid.removeOverlay(overlay);
    }
}
