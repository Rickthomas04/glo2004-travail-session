package MasterCut.drawing;

import MasterCut.domain.Controller;
import MasterCut.domain.dto.BorderDTO;
import MasterCut.domain.dto.IntersectionDTO;
import MasterCut.domain.dto.PanelDTO;

import javax.swing.plaf.basic.BasicInternalFrameTitlePane;
import java.awt.*;
import java.awt.geom.Point2D;
import java.util.List;

public class PanelDrawer
{
	private final Controller controller;

	public PanelDrawer(Controller controller)
	{
		this.controller = controller;
	}

	public void draw(Graphics2D g)
	{
		PanelDTO panelDTO = controller.getPanelDTO();
		if (panelDTO == null)
			return;

		int height = (int) panelDTO.dimensions.getHeight();

		drawBorders(g, panelDTO.borders, height);
	}

	private void drawBorders(Graphics2D g, List<BorderDTO> borders, int height)
	{
		for (BorderDTO border : borders)
		{
			Point2D.Double start = border.start.point;
			Point2D.Double end = border.end.point;

			g.setColor(border.color);

			g.setStroke(new BasicStroke(border.thickness));
			g.drawLine((int) start.x, height - (int) start.y, (int) end.x, height - (int) end.y);
		}
	}
}
