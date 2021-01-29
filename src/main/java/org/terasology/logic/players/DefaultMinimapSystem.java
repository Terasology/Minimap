// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.logic.players;

import org.joml.RoundingMode;
import org.joml.Vector3ic;
import org.terasology.engine.modes.loadProcesses.AwaitedLocalCharacterSpawnEvent;
import org.terasology.entitySystem.entity.EntityManager;
import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.entitySystem.event.EventPriority;
import org.terasology.entitySystem.event.ReceiveEvent;
import org.terasology.entitySystem.systems.BaseComponentSystem;
import org.terasology.entitySystem.systems.RegisterSystem;
import org.terasology.input.binds.minimap.DecreaseZoomButton;
import org.terasology.input.binds.minimap.IncreaseZoomButton;
import org.terasology.input.binds.minimap.ToggleMinimapButton;
import org.terasology.logic.characters.AliveCharacterComponent;
import org.terasology.logic.characters.CharacterComponent;
import org.terasology.logic.health.BeforeDestroyEvent;
import org.terasology.logic.health.DoDestroyEvent;
import org.terasology.logic.location.LocationComponent;
import org.terasology.logic.players.event.OnPlayerRespawnedEvent;
import org.terasology.logic.players.event.OnPlayerSpawnedEvent;
import org.terasology.math.geom.Rect2f;
import org.terasology.minimap.overlays.MinimapOverlay;
import org.terasology.minimap.rendering.nui.layers.MinimapHUDElement;
import org.terasology.nui.databinding.ReadOnlyBinding;
import org.terasology.registry.In;
import org.terasology.registry.Share;
import org.terasology.rendering.nui.NUIManager;
import org.terasology.world.WorldProvider;
import org.terasology.world.block.entity.placement.PlaceBlocks;
import org.terasology.world.generator.WorldGenerator;

/**
 * This class represents the connection to the event system and maintains the HUD element.
 */
@RegisterSystem
@Share(MinimapSystem.class)
public class DefaultMinimapSystem extends BaseComponentSystem implements MinimapSystem {

    public static final String HUD_ELEMENT_MAP_ID = "minimap";

    private MinimapHUDElement minimapHUDElement;

    @In
    private NUIManager nuiManager;

    @In
    private LocalPlayer localPlayer;

    @In
    private WorldProvider worldProvider;

    @In
    private WorldGenerator worldGenerator;

    @In
    private EntityManager entityManager;

    @Override
    public void initialise() {
        Rect2f rc = Rect2f.createFromMinAndSize(0, 0, 1, 1);
        minimapHUDElement = nuiManager.getHUD().addHUDElement(HUD_ELEMENT_MAP_ID, MinimapHUDElement.class, rc);
        minimapHUDElement.bindTargetEntity(new ReadOnlyBinding<EntityRef>() {

            @Override
            public EntityRef get() {
                return localPlayer.getCharacterEntity();
            }
        });

        // TODO: get the sea level height from elsewhere (WorldInfo/GameInfo/GameManifest?)
        int seaLevel = worldGenerator == null ? 10 : worldGenerator.getWorld().getSeaLevel();
        minimapHUDElement.setHeightRange(seaLevel, seaLevel + 64);
        minimapHUDElement.setWorldProvider(worldProvider);
        minimapHUDElement.updateAlivePlayerList(entityManager.getEntitiesWith(AliveCharacterComponent.class));
    }

    @Override
    public void addOverlay(MinimapOverlay overlay) {
        minimapHUDElement.addOverlay(overlay);
    }

    @Override
    public void removeOverlay(MinimapOverlay overlay) {
        minimapHUDElement.removeOverlay(overlay);
    }

    @ReceiveEvent(components = {CharacterComponent.class})
    public void onToggleMinimapButton(ToggleMinimapButton event, EntityRef entity) {
        if (event.isDown()) {
            minimapHUDElement.setVisible(!minimapHUDElement.isVisible());
            event.consume();
        }
    }

    @ReceiveEvent(components = {CharacterComponent.class})
    public void onIncreaseZoomButton(IncreaseZoomButton event, EntityRef entity) {
        if (event.isDown()) {
            minimapHUDElement.changeZoom(1);
            event.consume();
        }
    }

    @ReceiveEvent(components = {CharacterComponent.class})
    public void onDecreaseZoomButton(DecreaseZoomButton event, EntityRef entity) {
        if (event.isDown()) {
            minimapHUDElement.changeZoom(-1);
            event.consume();
        }
    }

    @ReceiveEvent
    public void onDestroyBlock(DoDestroyEvent event, EntityRef entity, LocationComponent locationComp) {
        minimapHUDElement.updateLocation(new org.joml.Vector3i(locationComp.getWorldPosition(new org.joml.Vector3f())
                , RoundingMode.CEILING));
    }

    @ReceiveEvent
    public void onPlaceBlock(PlaceBlocks event, EntityRef entity) {
        for (Vector3ic pos : event.getBlocks().keySet()) {
            minimapHUDElement.updateLocation(pos);
        }
    }

    @ReceiveEvent //Spawn, initial spawn on joining a server
    public void onPlayerSpawnEvent(OnPlayerSpawnedEvent event, EntityRef player) {
        if (minimapHUDElement != null) {
            minimapHUDElement.addAlivePlayer(player);
        }
    }

    @ReceiveEvent //Spawn, initial spawn on joining a server
    public void onPlayerReSpawnEvent(OnPlayerRespawnedEvent event, EntityRef player) {
        if (minimapHUDElement != null) {
            minimapHUDElement.addAlivePlayer(player);
        }
    }

    @ReceiveEvent
    public void onAwaitedLocalCharacterSpawnEvent(AwaitedLocalCharacterSpawnEvent event, EntityRef player) {
        if (minimapHUDElement != null && entityManager.getCountOfEntitiesWith(AliveCharacterComponent.class) != 0) {
            minimapHUDElement.updateAlivePlayerList(entityManager.getEntitiesWith(AliveCharacterComponent.class));
        }
    }


    @ReceiveEvent(priority = EventPriority.PRIORITY_HIGH, components = {CharacterComponent.class,
                                    AliveCharacterComponent.class, PlayerCharacterComponent.class})
    public void beforeDestroy(BeforeDestroyEvent event, EntityRef player) {
        if (minimapHUDElement != null) {
            minimapHUDElement.removeAlivePlayer(player);
        }
    }
}
