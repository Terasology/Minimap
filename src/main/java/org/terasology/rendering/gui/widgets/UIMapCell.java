/*
 * Copyright 2013 MovingBlocks
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.terasology.rendering.gui.widgets;

import javax.vecmath.Vector2f;

import org.terasology.asset.Assets;
import org.terasology.engine.CoreRegistry;
import org.terasology.entitySystem.entity.EntityManager;
import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.input.events.KeyEvent;
import org.terasology.math.Vector3i;
import org.terasology.rendering.assets.texture.Texture;
import org.terasology.rendering.gui.framework.UIDisplayContainer;
import org.terasology.rendering.gui.framework.UIDisplayElement;
import org.terasology.rendering.gui.framework.events.KeyListener;
import org.terasology.rendering.gui.framework.events.MouseButtonListener;
import org.terasology.rendering.gui.framework.events.MouseMoveListener;
import org.terasology.world.WorldProvider;
import org.terasology.world.block.Block;
import org.terasology.world.block.family.BlockFamily;
import org.terasology.world.block.items.BlockItemFactory;

/**
 * A cell which is linked to a slot-based inventory.
 */
public class UIMapCell extends UIDisplayContainer {
    private static final Vector2f DEFAULT_ICON_POSITION = new Vector2f(2f, 2f);

    private Vector3i mapLocationVector;

    //sub elements
    private final UIImage selectionRectangle;
    private UIMapIcon icon;
 
    private MouseMoveListener mouseMoveListener = new MouseMoveListener() {
        @Override
        public void leave(UIDisplayElement element) {
        }

        @Override
        public void hover(UIDisplayElement element) {

        }

        @Override
        public void enter(UIDisplayElement element) {
        }

        @Override
        public void move(UIDisplayElement element) {
        }
    };

    private MouseButtonListener mouseButtonListener = new MouseButtonListener() {
        @Override
        public void wheel(UIDisplayElement element, int wheel, boolean intersect) {
        }

        @Override
        public void up(UIDisplayElement element, int button, boolean intersect) {

        }

        @Override
        public void down(UIDisplayElement element, int button, boolean intersect) {
        }
    };

    private KeyListener keyListener = new KeyListener() {
        @Override
        public void key(UIDisplayElement element, KeyEvent event) {
        }
    };

    public UIMapCell(Vector3i mapLocationVector, Vector2f size) {
        this(mapLocationVector, size, DEFAULT_ICON_POSITION);
    }

    /**
     * Create a single item cell which is capable of holding an item.
     *
     * @param inventoryEntity The inventoryEntity of this item.
     * @param size            The size of the icon cell.
     * @param iconPosition    The position of the icon cell.
     */
    public UIMapCell(Vector3i mapLocationVector, Vector2f size, Vector2f iconPosition) {
    	this.mapLocationVector = mapLocationVector;
    	
        setSize(size);

        Texture guiTex = Assets.getTexture("engine:gui");

        selectionRectangle = new UIImage(guiTex);
        selectionRectangle.setTextureSize(new Vector2f(22f, 22f));
        selectionRectangle.setTextureOrigin(new Vector2f(1f, 23f));
        selectionRectangle.setSize(new Vector2f(getSize().x, getSize().y));
        selectionRectangle.setVisible(false);

        icon = new UIMapIcon();
        icon.setPosition(iconPosition);
        icon.setVisible(true);

        addMouseMoveListener(mouseMoveListener);
        addMouseButtonListener(mouseButtonListener);
        addKeyListener(keyListener);

        addDisplayElement(icon);
        addDisplayElement(selectionRectangle);
        
        update();
    }

    @Override
    public void update() {
        WorldProvider worldProvider = CoreRegistry.get(WorldProvider.class);
        BlockItemFactory blockItemFactory = new BlockItemFactory(CoreRegistry.get(EntityManager.class));

        Block block = worldProvider.getBlock(mapLocationVector);
		BlockFamily blockFamily = block.getBlockFamily();
		

        EntityRef item = blockItemFactory.newInstance(blockFamily, 1);
        if (!item.exists()) {
            return;
        }

		icon.setItem(item);

		super.update();
    }
}
