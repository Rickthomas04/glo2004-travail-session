package MasterCut.drawing;

import MasterCut.domain.Controller;
import MasterCut.domain.dto.GridDTO;

import java.awt.*;

public class GridDrawer
{

	private final Controller controller;

	public GridDrawer(Controller controller)
	{
		this.controller = controller;
	}

	public void drawGrid(Graphics2D g, int panelHeight)
	{
		GridDTO grid = controller.getGridDTO();
		g.setColor(grid.gridColor);
                int gridsize = controller.getGridDTO().gridSize;
		if (grid.showGrid)
		{

                        // Lignes horizontales
                    for (int y = 0; y <= grid.LINECOUNT; y += grid.gridSize) {
                        g.drawLine(-grid.LINECOUNT, panelHeight - y, grid.LINECOUNT, panelHeight - y);
                        g.drawLine(-grid.LINECOUNT, panelHeight + y, grid.LINECOUNT, panelHeight + y);
                    }

                        // Lignes verticales
                        for (int x = 0; x <= grid.LINECOUNT; x += grid.gridSize) {
                            g.drawLine(x, panelHeight + (grid.LINECOUNT), x, panelHeight - (grid.LINECOUNT));
                            g.drawLine(-x, panelHeight + (grid.LINECOUNT), -x, panelHeight - (grid.LINECOUNT));
                        }
			g.setColor(Color.green);
			g.drawLine(0, -grid.LINECOUNT, 0, grid.LINECOUNT);
			g.drawLine(-grid.LINECOUNT, 0 + panelHeight, grid.LINECOUNT, 0 + panelHeight);

		}
	}
}

