package org.terasology.rendering.hudElement;

import java.util.Arrays;
import java.util.List;

import org.terasology.rendering.gui.framework.UIDisplayElement;
import org.terasology.rendering.gui.widgets.UIMapGrid;
import org.terasology.rendering.logic.manager.HUDElement;

public class HUDElementMap implements HUDElement {

	private UIMapGrid mapGrid;

	@Override
	public Object getId() {
		return "terrain:map";
	}

	@Override
	public List<UIDisplayElement> getDisplayElements() {
		return Arrays.asList(new UIDisplayElement[] {
			mapGrid
		});
	}

	@Override
	public void initialise() {
		mapGrid = new UIMapGrid(11, 11);
		mapGrid.update();
	}

	@Override
	public void open() {
		// TODO Auto-generated method stub

	}

	@Override
	public void update() {
		// TODO Auto-generated method stub

	}

	@Override
	public void willShutdown() {
		// TODO Auto-generated method stub

	}

	@Override
	public void shutdown() {
		// TODO Auto-generated method stub

	}

	public void toggleVisibility() {
		mapGrid.setVisible(!mapGrid.isVisible());
	}

    public void toggleMapGridAxis() {
    	mapGrid.toggleAxis();
    }
}
