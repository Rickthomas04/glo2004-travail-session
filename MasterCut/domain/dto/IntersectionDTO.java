package MasterCut.domain.dto;

import MasterCut.domain.Intersection;
import MasterCut.domain.Line;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.awt.*;
import java.awt.geom.Point2D;
import java.io.Serializable;
import java.util.UUID;

public class IntersectionDTO implements Serializable {

    @JsonProperty
    public Point2D.Double point;
    @JsonProperty
    public final int radius;
    public boolean selectionStatus;
    @JsonProperty
    public Color color;
    @JsonProperty
    public final UUID uuid;
    @JsonProperty
    public Line verticalLine;
    @JsonProperty
    public Line horizontalLine;

    //For Deserialization
    public IntersectionDTO() {
        this.uuid = UUID.randomUUID();
        radius = 20;
    }
	public IntersectionDTO(Intersection intersection)
	{
		point = new Point2D.Double(intersection.getX(), intersection.getY()) ;
		radius = intersection.getRadius();
		selectionStatus = intersection.getSelectionStatus();
		color = intersection.getDisplayColor();
            uuid = intersection.getUUID();
            verticalLine = intersection.getVerticalLine();
            horizontalLine = intersection.getHorizontalLine();
	}
}
