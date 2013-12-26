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
import javax.vecmath.Vector3f;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.engine.CoreRegistry;
import org.terasology.logic.players.LocalPlayer;
import org.terasology.math.Vector3i;
import org.terasology.rendering.gui.framework.UIDisplayContainer;

/**
 * A grid of map cells
 *
 * @author Mike Kienenberger <mkienenb@gmail.com>
 */
public class UIMapGrid extends UIDisplayContainer {
    private static final Logger logger = LoggerFactory.getLogger(UIMapGrid.class);

    enum DisplayAxisType {
      XY_AXIS,
      XZ_AXIS,
      YZ_AXIS
    };
    
    private DisplayAxisType displayAxisType = DisplayAxisType.XZ_AXIS;
    
    private int numColumns = 15;
    private int numRows = 15;
    
    private UILabel orientationLabel;

    private UIMapCell[][] cells;

    private Vector2f cellMargin = new Vector2f(0, 0);
    private Vector2f cellSize = new Vector2f(48, 48);
    private Vector3f localPlayerPosition = null;

    public UIMapGrid(int numColumns, int numRows) {
        this.numColumns = numColumns;
        this.numRows = numRows;
        cells = new UIMapCell[numRows][numColumns];
        
        orientationLabel = new UILabel("x-z orientation");
        addDisplayElement(orientationLabel);
    }

    @Override
    public void update() {
        LocalPlayer localPlayer = CoreRegistry.get(LocalPlayer.class);
        Vector3f newLocalPlayerPosition = localPlayer.getPosition();
        if (!newLocalPlayerPosition.equals(localPlayerPosition)) {
        	localPlayerPosition = newLocalPlayerPosition;
        	fillInventoryCells();
        }

        super.update();
    }

    private void fillInventoryCells() {
        //remove old cells
    	for (int row = 0; row < cells.length; row++) {
        	for (int column = 0; column < cells[row].length; column++) {
        		UIMapCell cell = cells[row][column];
        		if (null != cell) {
                    removeDisplayElement(cell);
        		}
    		}
		}

		int mapCenterX;
		int mapCenterY;
		int mapCenterZ;
		switch (displayAxisType) {
			case XY_AXIS:
				mapCenterX = Math.round(localPlayerPosition.x - (numRows / 2));
				mapCenterY = Math.round(localPlayerPosition.y - (numColumns / 2));
				mapCenterZ = Math.round(localPlayerPosition.z);
				break;
			case XZ_AXIS:
				mapCenterX = Math.round(localPlayerPosition.x - (numRows / 2));
				mapCenterY = Math.round(localPlayerPosition.y);
				mapCenterZ = Math.round(localPlayerPosition.z - (numColumns / 2));
				break;
			case YZ_AXIS:
				mapCenterX = Math.round(localPlayerPosition.x);
				mapCenterY = Math.round(localPlayerPosition.y - (numRows / 2));
				mapCenterZ = Math.round(localPlayerPosition.z - (numColumns / 2));
				break;
			default:
				throw new RuntimeException("displayAxisType containts invalid value");
		}

    	cells = new UIMapCell[numRows][numColumns];

    	// TODO: probably need to invert y and maybe x axis in grid
    	
    	for (int row = 0; row < cells.length; row++) {
        	for (int column = 0; column < cells[row].length; column++) {
        		int rowInverted = (cells.length - row);
        		int columnInverted = (cells[row].length - column);
        		Vector3i mapLocationVector;
        		switch (displayAxisType) {
	    			case XZ_AXIS: // top down view
	            		mapLocationVector = new Vector3i((mapCenterX + rowInverted), mapCenterY, (mapCenterZ + column));
	    				break;
	    			case XY_AXIS:
	            		mapLocationVector = new Vector3i((mapCenterX + columnInverted), (mapCenterY + rowInverted), mapCenterZ);
	    				break;
	    			case YZ_AXIS:
	            		mapLocationVector = new Vector3i(mapCenterX,(mapCenterY + rowInverted), (mapCenterZ + columnInverted));
	    				break;
	    			default:
	    				throw new RuntimeException("displayAxisType containts invalid value");
	    		}
        		
        		UIMapCell cell = new UIMapCell(mapLocationVector, cellSize);
                cell.setPosition(new Vector2f(column * (cellSize.x + cellMargin.x), orientationLabel.getSize().y + (row * (cellSize.y + cellMargin.y))));

                cells[row][column] = cell;
                addDisplayElement(cell);
    		}
        }

        setSize(new Vector2f(numColumns * (cellSize.x + cellMargin.x), numRows * (cellSize.y + cellMargin.y)));

		switch (displayAxisType) {
			case XY_AXIS:
		        orientationLabel.setText("x-y orientation");
				break;
			case XZ_AXIS:
		        orientationLabel.setText("x-z orientation");
				break;
			case YZ_AXIS:
		        orientationLabel.setText("y-z orientation");
				break;
		}
    }

    public UIMapCell[][] getCells() {
        return cells;
    }


    public Vector2f getCellMargin() {
        return new Vector2f(cellMargin);
    }

    public void setCellMargin(Vector2f cellMargin) {
        this.cellMargin.set(cellMargin);
        fillInventoryCells();
    }

	public void toggleAxis() {
		switch (displayAxisType) {
			case XY_AXIS:
				displayAxisType = DisplayAxisType.XZ_AXIS;
				break;
			case XZ_AXIS:
				displayAxisType = DisplayAxisType.YZ_AXIS;
				break;
			case YZ_AXIS:
				displayAxisType = DisplayAxisType.XY_AXIS;
				break;
		}
		
		localPlayerPosition = null;
	}
}

