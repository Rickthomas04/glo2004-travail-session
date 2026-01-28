package MasterCut.domain.dto;

import MasterCut.domain.Grid;
import java.awt.*;
import java.io.Serializable;

public class GridDTO implements Serializable {
	public int gridSize;
	public boolean showGrid;
	public boolean isMagnetic;
	public Color gridColor;
	public int LINECOUNT;

	public GridDTO(Grid grid)
	{
		this.gridSize = grid.getGridSize();
		this.gridColor = grid.getGridColor();
		this.showGrid = grid.isShowGrid();
		this.isMagnetic = grid.isMagnetic();
		this.LINECOUNT = grid.getLineCount();
	}
}
