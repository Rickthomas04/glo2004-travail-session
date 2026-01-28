package MasterCut.drawing;

import MasterCut.domain.Controller;
import MasterCut.domain.dto.*;
import MasterCut.domain.dto.cuts.BaseCutDTO;
import MasterCut.domain.dto.cuts.RegularCutDTO;
import java.awt.*;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.List;


public class CutDrawer
{
	private final Controller controller;

	public CutDrawer(Controller controller)
	{
		this.controller = controller;
	}

	public void draw(Graphics2D g)
	{
		PanelDTO panel = controller.getPanelDTO();
		int height = (int) controller.getPanelDTO().dimensions.getHeight();

		for (BaseCutDTO cut : panel.cutsMap.values())
			for (RegularCutDTO regularCutDTO : cut.getRegularCuts())
				drawRegularCut(g, regularCutDTO, height);
	}

	private void drawRegularCut(Graphics2D g, RegularCutDTO cut, int height)
	{
		// Get start and end points
		Point2D.Double start = cut.start.point;
		Point2D.Double end = cut.end.point;

		// Set color and thickness for drawing
		g.setColor(cut.color);
		g.setStroke(new BasicStroke(cut.thickness));

		// Draw the line for the cut (inverted Y-axis)
		g.drawLine((int) start.x, height - (int) start.y, (int) end.x, height - (int) end.y);
	}
}
