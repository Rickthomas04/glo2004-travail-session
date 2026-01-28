package MasterCut.domain;

import MasterCut.domain.cuts.BaseCut;
import MasterCut.domain.dto.BorderDTO;
import MasterCut.domain.dto.IntersectionDTO;
import MasterCut.domain.utils.Clickable;
import MasterCut.domain.utils.Dimensions;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.awt.*;
import java.awt.geom.Point2D;
import java.io.Serializable;
import java.util.*;
import java.util.List;
public class Border implements Clickable, Cloneable, Serializable, Line
{

    @JsonProperty
    private Intersection start;
	@JsonProperty
	private Intersection end;
	@JsonProperty
	private Color color;
	@JsonProperty
	private int thickness;
	private boolean isSelected = false;
	@JsonProperty
	private boolean isHorizontal;
	@JsonProperty
    private final UUID uuid;

	private final int TOLERANCE = 20;
	private final Color DEFAULT_COLOR = new Color(213, 213, 213);
	private final Color DEFAULT_SELECTED_COLOR = new Color(74, 199, 0, 255);
    private final int DEFAULT_THICKNESS = 5;
    @JsonProperty
	private List<Intersection> intersections = new ArrayList<>();

    //For Deserialization
    public Border() {
        this.uuid = null;
    }

	public Border(Intersection start, Intersection end)
	{
		this.start = start;
		this.end = end;
		this.color = DEFAULT_COLOR;
		this.thickness = DEFAULT_THICKNESS;
		this.uuid = UUID.randomUUID();
		this.isHorizontal = start.getY() == end.getY();

		intersections.add(start);
		intersections.add(end);
    }

        @Override
        public Tool getTool(){
            
            return null;
        }
        
        @Override
        public void addOffset(){}
        
        @Override
        public void removeOffset(){}
                

	public Intersection getStart()
	{
		return start;
	}

	public IntersectionDTO getStartDTO()
	{
		return start.getIntersectionDTO();
	}

	public void setStart(Intersection start)
	{
		this.start = start;
	}

	public Intersection getEnd()
	{
		return end;
	}

	@Override
	public List<Intersection> getIntersections()
	{
		return intersections;
	}

	public IntersectionDTO getEndDTO()
	{
		return end.getIntersectionDTO();
	}

	public void setEnd(Intersection end)
	{
		this.end = end;
	}

	public Color getColor()
	{
		return color;
	}

	public void setColor(Color color)
	{
		this.color = color;
	}

	public int getThickness()
	{
		return thickness;
	}

	public void setThickness(int thickness)
	{
		this.thickness = thickness;
	}

	public boolean isSelected()
	{
		return isSelected;
	}

	@Override
	public void removeIntersection(Intersection intersection)
	{
		this.intersections.remove(intersection);
	}

	@Override
	public double getLenght()
	{
		if (isHorizontal)
			return Math.abs(end.getX() - start.getX());
		else
		{
			return Math.abs(end.getY() - start.getY());
		}
	}

	@Override
	public void setDisplayColor(Color color)
	{
	}

	@Override
	public void addIntersection(Intersection intersection)
	{

	}

	@Override
	public boolean isHorizontal()
	{
		return isHorizontal;
	}

	@Override
	public void addChild(BaseCut baseCut)
	{

	}

	@Override
	public void removeChild(BaseCut baseCut)
	{

	}

	@Override
	public BaseCut getParentCut()
	{
		return null;
	}

	public void setHorizontal(boolean horizontal)
	{
		isHorizontal = horizontal;
	}

	@Override
	public UUID getUUID()
	{
		return uuid;
	}

	@Override
	public Object getDTO()
	{
		return getBorderDTO();
	}

	public BorderDTO getBorderDTO()
	{
		return new BorderDTO(this);
	}

	public void switchSelectionStatus()
	{
		isSelected = !isSelected;
		updateState();
	}

	private void updateState()
	{
		if (isSelected)
		{
			setColor(DEFAULT_SELECTED_COLOR);
		} else
		{
			setColor(DEFAULT_COLOR);
		}
	}

	@Override
	public boolean isPointOnFeature(Point2D.Double point)
	{
		double expandedXMin = Math.min(start.getX(), end.getX()) - TOLERANCE;
		double expandedXMax = Math.max(start.getX(), end.getX()) + TOLERANCE;
		double expandedYMin = Math.min(start.getY(), end.getY()) - TOLERANCE;
		double expandedYMax = Math.max(start.getY(), end.getY()) + TOLERANCE;

		return (point.getX() >= expandedXMin && point.getX() <= expandedXMax) && (point.getY() >= expandedYMin && point.getY() <= expandedYMax);
	}

	@Override
	public void handleDrag(Point2D.Double dragPoint, Dimensions dimensions)
	{

	}

	@Override
	public Clickable onClick()
	{
		switchSelectionStatus();
		return isSelected ? this : null;
	}

	@Override
	public void setSelected(boolean selectionStatus)
	{
		this.isSelected = selectionStatus;
		updateState();
	}





	public List<IntersectionDTO> getIntersectionsDTO()
	{
		List<IntersectionDTO> intersectionsDTO = new ArrayList<>();
		for (Intersection intersection : intersections)
			intersectionsDTO.add(intersection.getIntersectionDTO());
		return intersectionsDTO;
	}
    @Override
    public Border clone(Map<Intersection, Intersection> intersectionClones,
                        Map<BaseCut, BaseCut> baseCutClones,
                        Map<Line, Line> lineClones) throws CloneNotSupportedException {

        if (lineClones.containsKey(this)) {
            return (Border) lineClones.get(this);
        }


        Border cloned = (Border) super.clone();


        lineClones.put(this, cloned);


        cloned.start = intersectionClones.get(this.start);
        if (cloned.start == null) {
            cloned.start = this.start.clone(intersectionClones, baseCutClones, lineClones);
            intersectionClones.put(this.start, cloned.start);
        }


        cloned.end = intersectionClones.get(this.end);
        if (cloned.end == null) {
            cloned.end = this.end.clone(intersectionClones, baseCutClones, lineClones);
            intersectionClones.put(this.end, cloned.end);
        }


        cloned.intersections = new ArrayList<>();
        for (Intersection intersection : this.intersections) {
            Intersection clonedIntersection = intersectionClones.get(intersection);
            if (clonedIntersection == null) {
                clonedIntersection = intersection.clone(intersectionClones, baseCutClones, lineClones);
                intersectionClones.put(intersection, clonedIntersection);
            }
            cloned.intersections.add(clonedIntersection);
        }


        cloned.color = new Color(this.color.getRGB());

        // Copier les autres champs primitifs et immuables
        cloned.isSelected = this.isSelected;
        cloned.isHorizontal = this.isHorizontal;
        cloned.thickness = this.thickness;



        return cloned;
    }

	@Override
    public void setIntersections(List<Intersection> intersections)
    {
            this.intersections = intersections;
    }
@Override
public boolean containsPoint(Point2D.Double point, double tolerance) {
    // Vérifier si la ligne est horizontale
    if (isHorizontal) {
        double minX = Math.min(start.getX(), end.getX());
        double maxX = Math.max(start.getX(), end.getX());
        return point.getY() >= start.getY() - tolerance && point.getY() <= start.getY() + tolerance &&
               point.getX() >= minX - tolerance && point.getX() <= maxX + tolerance;
    } 
    // Si la ligne est verticale
    else {
        double minY = Math.min(start.getY(), end.getY());
        double maxY = Math.max(start.getY(), end.getY());
        return point.getX() >= start.getX() - tolerance && point.getX() <= start.getX() + tolerance &&
               point.getY() >= minY - tolerance && point.getY() <= maxY + tolerance;
    }
}

}
