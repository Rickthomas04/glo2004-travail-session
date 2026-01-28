package MasterCut.domain;

import MasterCut.domain.cuts.BaseCut;
import MasterCut.domain.cuts.LCut;
import MasterCut.domain.cuts.RegularCut;
import MasterCut.domain.dto.IntersectionDTO;
import MasterCut.domain.utils.Clickable;
import MasterCut.domain.utils.Dimensions;
import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import java.awt.*;
import java.awt.geom.Point2D;
import java.io.Serializable;
import java.util.*;
@JsonIdentityInfo(
        generator = ObjectIdGenerators.PropertyGenerator.class,
        property = "uuid")
//@JsonIdentityReference(alwaysAsId = true)
public class Intersection implements Clickable, Cloneable, Serializable {

	@JsonProperty
	private Point2D.Double point;
	@JsonProperty
	private int radius;
	@JsonProperty
	private boolean selectionStatus;
	@JsonProperty
	private boolean mainIntersection = false;
	@JsonProperty
	private Color color;
	@JsonProperty
    private Color displayColor;
    @JsonIdentityInfo(
            generator = ObjectIdGenerators.PropertyGenerator.class,
            property = "uuid")
	@JsonProperty
    private BaseCut baseCutRef;
    @JsonSubTypes({
        @JsonSubTypes.Type(value = RegularCut.class, name = "RegularCut"),
        @JsonSubTypes.Type(value = Border.class, name = "Border")
    })
    @JsonProperty
    private Line horizontalLine;
    @JsonSubTypes({
        @JsonSubTypes.Type(value = RegularCut.class, name = "RegularCut"),
        @JsonSubTypes.Type(value = Border.class, name = "Border")
    })
    @JsonProperty
	private Line verticalLine;
	@JsonProperty
    private UUID uuid;

	private final Color DEFAULT_COLOR = Color.BLUE;
	private final Color DEFAULT_SELECTED_COLOR = new Color(74, 199, 0, 255);
	private final int DEFAULT_RADIUS = 20;
    private final double TOLERANCE = 8.0;
    //For Deserialization
    public Intersection() {
    }

	// Basic constructor
	public Intersection(Point2D.Double point)
	{
		this.point = point;
		this.radius = DEFAULT_RADIUS;
		this.color = DEFAULT_COLOR;
		this.displayColor = DEFAULT_COLOR;
		this.selectionStatus = false;
		this.uuid = UUID.randomUUID();
	}

	public void setRadius(int radius)
	{
		this.radius = radius + 10;
	}

	public Intersection(Intersection intersection)
	{
		this.point = new Point2D.Double(intersection.getPoint().getX(), intersection.getPoint().getY());
		this.radius = intersection.getRadius();
		this.color = intersection.getColor();
		this.displayColor = intersection.getDisplayColor();
		this.selectionStatus = false;
		this.horizontalLine = intersection.getHorizontalLine();
		this.verticalLine = intersection.getVerticalLine();
		this.mainIntersection = intersection.isMainIntersection();
		this.baseCutRef = intersection.getBaseCutRef();
		this.uuid = UUID.randomUUID();
	}

	public void setHorizontalLine(Line horizontalLine)
	{
		this.horizontalLine = horizontalLine;
	}

	public void setVerticalLine(Line verticalLine)
	{
		this.verticalLine = verticalLine;
	}

	public Line getVerticalLine()
	{
		return verticalLine;
	}

	public Line getHorizontalLine()
	{
		return horizontalLine;
	}

	public Intersection(double x, double y)
	{
		this(new Point2D.Double(x, y));
	}

	public Color getDisplayColor()
	{
		return displayColor;
	}

	public boolean isMainIntersection()
	{
		return mainIntersection;
	}

	public void setMainIntersection(boolean mainIntersection)
	{
		this.mainIntersection = mainIntersection;
	}

	public BaseCut getBaseCutRef()
	{
		return baseCutRef;
	}

	public void setBaseCutRef(BaseCut baseCutRef)
	{
		this.baseCutRef = baseCutRef;
	}

	public void setDisplayColor(Color displayColor)
	{
		this.displayColor = displayColor;
	}

	public void setX(double x)
	{
		this.point.x = x;
	}

	public void setY(double y)
	{
		this.point.y = y;
	}

	public IntersectionDTO getIntersectionDTO()
	{
		return new IntersectionDTO(this);
	}

	public int getRadius()
	{
		return radius;
	}

	public Point2D.Double getPoint()
	{
		return point;
	}

	public void setPoint(Point2D.Double point)
	{
		this.point = point;
	}

	public double getX()
	{
		return point.x;
	}

	public double getY()
	{
		return point.y;
	}

	public boolean getSelectionStatus()
	{
		return selectionStatus;
	}

	public Color getColor()
	{
		return color;
	}

	public UUID getUUID()
	{
		return uuid;
	}

	@Override
	public Object getDTO()
	{
		return getIntersectionDTO();
	}

	public boolean isSelected()
	{
		return selectionStatus;
	}

	public void setSelected(boolean selected)
	{
		this.selectionStatus = selected;
		updateState();
	}

	public void switchSelectionStatue()
	{
		selectionStatus = !selectionStatus;
		updateState();
	}

	public boolean haveBothLineSet()
	{
		return horizontalLine != null && verticalLine != null;
	}

	public void setColor(Color color)
	{
		this.color = color;
	}

	public void updateState()
	{
		if (selectionStatus)
			setDisplayColor(DEFAULT_SELECTED_COLOR);
		else
			setDisplayColor(color);
	}

	@Override
	public boolean isPointOnFeature(Point2D.Double point)
	{
		double distance = Math.sqrt(Math.pow(point.getX() - this.point.x, 2) + Math.pow(point.getY() - this.point.y, 2));
		return distance <= (radius + TOLERANCE);
	}

	@Override
	public String toString()
	{
		return String.format("Intersection[x=%.2f, y=%.2f]", point.x, point.y);
	}

	@Override
	public void handleDrag(Point2D.Double dragPoint, Dimensions dimensions)
	{
		if (mainIntersection && baseCutRef instanceof LCut cut)
			cut.handleDrag(dragPoint, dimensions);
	}

	public boolean isEndOfHorizontalLine()
	{
		if (horizontalLine == null)
			return true;

		return horizontalLine.getStart() == this || horizontalLine.getEnd() == this;
	}

	public boolean isEndOfVerticalLine()
	{
		if (horizontalLine == null || verticalLine == null)
			return true;

		return verticalLine.getStart() == this || verticalLine.getEnd() == this;
	}

	public Point2D.Double getOppositePoint()
	{
		if (horizontalLine == null || verticalLine == null)
			return null;

		double x;
		double y;

		if (horizontalLine.getStart().getX() == point.getX())
			x = horizontalLine.getEnd().getX();
		else
			x = horizontalLine.getStart().getX();

		if (verticalLine.getStart().getY() == point.getY())
			y = verticalLine.getEnd().getY();
		else
			y = verticalLine.getStart().getY();

		return new Point2D.Double(x, y);
	}

	@Override
	public Clickable onClick()
	{
		switchSelectionStatue();
		return selectionStatus ? this : null;
	}

	@Override
	public boolean equals(Object o)
	{
		if (this == o)
			return true;
		if (o == null || getClass() != o.getClass())
			return false;
		Intersection that = (Intersection) o;
		return Objects.equals(point, that.point);
	}

	@Override
	public int hashCode()
	{
		return Objects.hashCode(point);
	}

	public Intersection clone(Map<Intersection, Intersection> intersectionClones, Map<BaseCut, BaseCut> baseCutClones, Map<Line, Line> lineClones) throws CloneNotSupportedException
	{
		if (intersectionClones.containsKey(this))
			return intersectionClones.get(this);

		Intersection cloned = (Intersection) super.clone();

		cloned.point = (Point2D.Double) this.point.clone();

		intersectionClones.put(this, cloned);

		cloned.horizontalLine = null;
		cloned.verticalLine = null;
		cloned.baseCutRef = null;

		return cloned;
	}
public void rebuildLinks(Map<UUID, Line> lines) {
    this.horizontalLine = null;
    this.verticalLine = null;

    for (Line line : lines.values()) {
        if (line.containsPoint(this.getPoint(), 0.01)) {
            if (line.isHorizontal()) {
                this.horizontalLine = line;
            } else {
                this.verticalLine = line;
            }
        }
    }

    if (this.horizontalLine == null || this.verticalLine == null) {
        System.err.println("Erreur : Intersection non liée correctement pour le point " + this.getPoint());
    }
}



//        @Override
//        public String toString() {
//            return "Intersection{" +
//                   "point=" + point +
//                   ", radius=" + radius +
//                   ", selectionStatus=" + selectionStatus +
//                   ", mainIntersection=" + mainIntersection +
//                   ", color=" + color +
//                   ", displayColor=" + displayColor +
//                   ", baseCutRef=" + (baseCutRef != null ? baseCutRef.getClass().getSimpleName() + "@" + baseCutRef.getUUID() : "null") +
//                   ", horizontalLine=" + (horizontalLine != null ? horizontalLine.getClass().getSimpleName() + "@" + horizontalLine.getUUID() : "null") +
//                   ", verticalLine=" + (verticalLine != null ? verticalLine.getClass().getSimpleName() + "@" + verticalLine.getUUID() : "null") +
//                   ", uuid=" + uuid +
//                   '}';
//        }

}
