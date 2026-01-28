package MasterCut.drawing;

import MasterCut.domain.Controller;
import MasterCut.domain.dto.IntersectionDTO;
import MasterCut.domain.dto.PanelDTO;

import java.awt.*;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class IntersectionDrawer
{
	private final Controller controller;

	public IntersectionDrawer(Controller controller)
	{
		this.controller = controller;
	}

	public void draw(Graphics2D g)
	{
		PanelDTO panelDTO = controller.getPanelDTO();
		if (panelDTO == null)
			return;

		int height = (int) panelDTO.dimensions.getHeight();

		drawIntersections(g, controller.getIntersectionsDTO(), height);
	}

	private void drawIntersections(Graphics2D g, Map<UUID,IntersectionDTO> intersections, int height)
	{
		for (IntersectionDTO intersection : intersections.values())
		{
			g.setColor(intersection.color);
			int x = (int) intersection.point.x;
			int y = height - (int) intersection.point.y;
			int diameter = intersection.radius;
			g.fillOval(x - diameter / 2, y - diameter / 2, diameter, diameter);
		}
	}
}
