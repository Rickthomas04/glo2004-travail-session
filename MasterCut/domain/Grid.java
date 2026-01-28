package MasterCut.domain;

import MasterCut.domain.dto.GridDTO;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.awt.*;
import java.awt.geom.Point2D;
import java.io.Serializable;

public class Grid implements Cloneable, Serializable {
    private static final long serialVersionUID = 1L; 

    @JsonProperty
    private int gridSize;

    private boolean showGrid = true;
    private boolean isMagnetic = false;

    @JsonProperty
    private Color gridColor; 

    private static final Color DEFAULT_GRID_COLOR = new Color(35, 35, 35);
    private static final int LINECOUNT = 100000;


    public Grid() {
    }

    public Grid(int gridSize, Color gridColor) {
        this.gridSize = gridSize;
        this.gridColor = gridColor;

        if (gridColor == null)
            this.gridColor = DEFAULT_GRID_COLOR;
    }

    public Grid(int gridSize) {
        this(gridSize, null);
    }

    public Color getGridColor() {
        return gridColor;
    }

    public void setGridColor(Color gridColor) {
        this.gridColor = gridColor;
    }

    public int getGridSize() {
        return gridSize;
    }

    public void setGridSize(int gridSize) {
        this.gridSize = gridSize;
    }

    public boolean isMagnetic() {
        return isMagnetic;
    }

    public void setMagnetic(boolean magnetic) {
        isMagnetic = magnetic;
    }

    public boolean isShowGrid() {
        return showGrid;
    }

    public void setShowGrid(boolean showGrid) {
        this.showGrid = showGrid;
    }

    public int getLineCount() {
        return this.LINECOUNT;
    }

    public GridDTO getGridDTO() {
        return new GridDTO(this);
    }

    public Point2D.Double snapToGrid(Point2D.Double point) {
        return new Point2D.Double(
                Math.round(point.x / gridSize) * gridSize,
                Math.round(point.y / gridSize) * gridSize
        );
    }

    @Override
    public Grid clone() {
        try {
            return (Grid) super.clone();
        } catch (CloneNotSupportedException e) {
            throw new AssertionError("Cloning not supported", e);
        }
    }


    private void writeObject(java.io.ObjectOutputStream out) throws java.io.IOException {
        out.defaultWriteObject();
        out.writeInt(gridColor.getRGB()); 
    }

    private void readObject(java.io.ObjectInputStream in) throws java.io.IOException, ClassNotFoundException {
        in.defaultReadObject();
        gridColor = new Color(in.readInt()); 
    }
}
