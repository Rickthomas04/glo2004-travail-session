package MasterCut.domain.cuts;

import MasterCut.domain.Intersection;
import MasterCut.domain.Line;
import MasterCut.domain.Tool;
import MasterCut.domain.dto.DimensionsDTO;
import MasterCut.domain.dto.cuts.BaseCutDTO;
import MasterCut.domain.dto.cuts.LCutDTO;
import MasterCut.domain.dto.cuts.RegularCutDTO;
import MasterCut.domain.utils.Clickable;
import MasterCut.domain.utils.Dimensions;

import static MasterCut.domain.utils.Message.sendMessage;

import MasterCut.domain.utils.enumPackage.CutType;
import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;

import java.awt.*;
import java.awt.geom.Point2D;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "uuid")
//@JsonIdentityReference(alwaysAsId = true)
/*@JsonTypeInfo(
        use = JsonTypeInfo.Id.CLASS
)*/ public class LCut extends IrregularCut implements Serializable
{

	@JsonProperty
	public final String type = "LCut";
	@JsonProperty
	private RegularCut horizontalCut;
	@JsonProperty
	private RegularCut verticalCut;
	@JsonProperty
	double distanceFromReferenceX = 0;
	@JsonProperty
	double distanceFromReferenceY = 0;

	public LCut()
	{
		super(null, 0, null, null, null); // TODO: check for the last argument if is ok
		//this.horizontalCut = null;
		//this.verticalCut = null;
		int tempLCut = 0;
            /*
		if (reference.getX() >= oppositePoint.getX() && reference.getY() >= oppositePoint.getY())
		{
			tempLCut = 2;
		} else if (reference.getX() >= oppositePoint.getX() && reference.getY() <= oppositePoint.getY())
		{
			tempLCut = 3;
		} else if (reference.getX() <= oppositePoint.getX() && reference.getY() >= oppositePoint.getY())
		{
			tempLCut = 1;
		} else
		{
			tempLCut = 4;
		}
                */
	}

	public LCut(Tool tool, double depth, Intersection reference, Intersection intersection1, Intersection intersection2, Intersection intersection3, BaseCut parent)
	{
		super(tool, depth, reference, CutType.L_SHAPE, parent);

		this.horizontalCut = new RegularCut(tool, reference, intersection2, intersection1, depth, this, parent);
		this.horizontalCut.setHorizontal(true);
		this.verticalCut = new RegularCut(tool, reference, intersection3, intersection1, depth, this, parent);
		this.verticalCut.setHorizontal(false);

		cuts.add(horizontalCut);
		cuts.add(verticalCut);
		this.distanceFromReferenceX = intersection1.getX() - reference.getX();
		this.distanceFromReferenceY = intersection1.getY() - reference.getY();
		this.intersections.add(intersection1);
		this.intersections.add(intersection2);
		this.intersections.add(intersection3);

		sendMessage("L Cut created");
	}

	public RegularCut getHorizontalCut()
	{
		return horizontalCut;
	}

	public RegularCut getVerticalCut()
	{
		return verticalCut;
	}

	@Override
	public List<RegularCutDTO> getRegularCutsDTO()
	{
		List<RegularCutDTO> regularCutsDTO = new ArrayList<>();
		regularCutsDTO.addAll(horizontalCut.getRegularCutsDTO());
		regularCutsDTO.addAll(verticalCut.getRegularCutsDTO());
		return regularCutsDTO;
	}

	@Override
	public List<RegularCut> getRegularCuts()
	{
		List<RegularCut> regularCutsDTO = new ArrayList<>();
		regularCutsDTO.add(horizontalCut);
		regularCutsDTO.add(verticalCut);
		return regularCutsDTO;
	}

	@Override
	public Point2D.Double getDistanceFromReference()
	{
		double height = horizontalCut.getDistanceFromReference().getY();
		double width = verticalCut.getDistanceFromReference().getX();

		return new Point2D.Double(width, height);
	}

	@Override
	public void editCutDistance(Point2D.Double point)
	{

		Map<BaseCut, Point2D.Double> oldDistances = new HashMap<>();
		for (RegularCut regularCut : getCuts())
			for (BaseCut baseCut : regularCut.getChildren())
				oldDistances.put(baseCut, baseCut.getDistanceFromReference());

		horizontalCut.editCutDistance(point);
		verticalCut.editCutDistance(point);

		// Réappliquer les distances relatives sauvegardées aux enfants
		for (Map.Entry<BaseCut, Point2D.Double> entry : oldDistances.entrySet())
		{
			BaseCut child = entry.getKey();
			Point2D.Double oldDistance = entry.getValue();
			child.editCutDistance(oldDistance);
		}
	}

	@Override
	public void setTool(Tool tool)
	{
		if (tool != this.tool)
		{

			this.tool = tool;
			this.verticalCut.setTool(tool);
			this.verticalCut.updateTickness();


			this.horizontalCut.setTool(tool);
			this.horizontalCut.updateTickness();
		}
	}

	@Override
	public BaseCutDTO getBaseCutDTO()
	{
		return new LCutDTO(this);
	}

	public RegularCutDTO getHorizontalCutDTO()
	{
		return horizontalCut.getRegularCutsDTO().getFirst();
	}

	public RegularCutDTO getVerticalCutDTO()
	{
		return verticalCut.getRegularCutsDTO().getFirst();
	}


	@Override
	public boolean isPointOnFeature(Point2D.Double point)
	{
		return false;
	}

	@Override
	public void handleDrag(Point2D.Double dragPoint, Dimensions dimensions)
	{
		Map<BaseCut, Point2D.Double> oldDistances = new HashMap<>();
		for (RegularCut regularCut : getCuts())
			for (BaseCut baseCut : regularCut.getChildren())
				oldDistances.put(baseCut, baseCut.getDistanceFromReference());

		Point2D.Double referencePoint = reference.getPoint();
		Point2D.Double opposite = reference.getOppositePoint();

		double minX;
		double maxX;
		double minY;
		double maxY;

		if (reference.isEndOfHorizontalLine() && reference.isEndOfVerticalLine())
		{
			minX = Math.min(referencePoint.x, opposite.x);
			maxX = Math.max(referencePoint.x, opposite.x);
			minY = Math.min(referencePoint.y, opposite.y);
			maxY = Math.max(referencePoint.y, opposite.y);
		} else if (!reference.isEndOfHorizontalLine() && reference.isEndOfVerticalLine())
		{
			minX = Math.min(reference.getHorizontalLine().getStart().getX(), reference.getHorizontalLine().getEnd().getX());
			maxX = Math.max(reference.getHorizontalLine().getStart().getX(), reference.getHorizontalLine().getEnd().getX());
			minY = Math.min(referencePoint.y, opposite.y);
			maxY = Math.max(referencePoint.y, opposite.y);
		} else if (reference.isEndOfHorizontalLine() && !reference.isEndOfVerticalLine())
		{
			minX = Math.min(referencePoint.x, opposite.x);
			maxX = Math.max(referencePoint.x, opposite.x);
			minY = Math.min(reference.getVerticalLine().getStart().getY(), reference.getVerticalLine().getEnd().getY());
			maxY = Math.max(reference.getVerticalLine().getStart().getY(), reference.getVerticalLine().getEnd().getY());
		} else
		{
			minX = Math.min(reference.getHorizontalLine().getStart().getX(), reference.getHorizontalLine().getEnd().getX());
			maxX = Math.max(reference.getHorizontalLine().getStart().getX(), reference.getHorizontalLine().getEnd().getX());
			minY = Math.min(reference.getVerticalLine().getStart().getY(), reference.getVerticalLine().getEnd().getY());
			maxY = Math.max(reference.getVerticalLine().getStart().getY(), reference.getVerticalLine().getEnd().getY());
		}

		if (isWithinBounds(dragPoint, minX, maxX, minY, maxY, dimensions))
		{
			intersections.getFirst().setPoint(dragPoint);

			for (Intersection intersection : horizontalCut.getIntersections())
				intersection.setY(intersections.getFirst().getY());

			for (Intersection intersection : verticalCut.getIntersections())
				intersection.setX(intersections.getFirst().getX());
		}

		for (Map.Entry<BaseCut, Point2D.Double> entry : oldDistances.entrySet())
		{
			BaseCut child = entry.getKey();
			Point2D.Double oldDistance = entry.getValue();
			child.editCutDistance(oldDistance);
		}


	}

	private boolean isWithinBounds(Point2D.Double point, double minX, double maxX, double minY, double maxY, Dimensions dimensions)
	{
		double panelWidth = dimensions.getWidth();
		double panelHeight = dimensions.getHeight();

		return point.x >= minX && point.x <= maxX && point.y >= minY && point.y <= maxY && point.x >= 0 && point.x <= panelWidth && point.y >= 0 && point.y <= panelHeight;
	}

	@Override
	public Clickable onClick()
	{
		horizontalCut.switchSelectionStatus();
		verticalCut.switchSelectionStatus();
		return (horizontalCut.isSelected() && verticalCut.isSelected()) ? this : null;
	}

	@Override
	public void setSelected(boolean b)
	{
		horizontalCut.setSelected(b);
		verticalCut.setSelected(b);
	}

	@Override
	public Object getDTO()
	{
		return getBaseCutDTO();
	}

	public void UpdateTool(Tool selectedTool)
	{
		double toolOffset = tool.getDiameter() / 2.0;
		Point2D.Double length = getDistanceFromReference();
		double horizontalOffset = -toolOffset;
		double verticalOffset = -toolOffset;
		if (horizontalCut.getStart().getX() > horizontalCut.getEnd().getX() && verticalCut.getStart().getY() > verticalCut.getEnd().getY())
		{
			horizontalOffset = -horizontalOffset;
			verticalOffset = -verticalOffset;
		}
		if (horizontalCut.getStart().getX() > horizontalCut.getEnd().getX() && verticalCut.getStart().getY() < verticalCut.getEnd().getY())
		{
			verticalOffset = -verticalOffset;
			horizontalOffset = -horizontalOffset;
		}
		if (horizontalCut.getStart().getX() < horizontalCut.getEnd().getX() && verticalCut.getStart().getY() > verticalCut.getEnd().getY())
		{
			verticalOffset = -verticalOffset;
			horizontalOffset = -horizontalOffset;
		}
		if (horizontalCut.getStart().getX() < horizontalCut.getEnd().getX() && verticalCut.getStart().getY() < verticalCut.getEnd().getY())
		{
			verticalOffset = -verticalOffset;
			horizontalOffset = -horizontalOffset;
		}

		double segment1Length = length.getY() - verticalOffset - verticalOffset;
		double segment2Length = length.getX() - horizontalOffset - horizontalOffset;

		setTool(selectedTool);
		editCutDistance(new Point2D.Double(segment2Length, segment1Length));
		horizontalCut.setTool(selectedTool);
		verticalCut.setTool(selectedTool);
	}

	@Override
	public void changeReference(Intersection intersection)
	{
		this.reference = intersection;
	}

	@Override
	public DimensionsDTO getDimensionOfCut()
	{
		return new DimensionsDTO(getDistanceFromReference().x, getDistanceFromReference().y);
	}

	@Override
	public LCut clone(Map<Intersection, Intersection> intersectionClones, Map<BaseCut, BaseCut> baseCutClones, Map<Line, Line> lineClones) throws CloneNotSupportedException
	{

		if (baseCutClones.containsKey(this))
		{
			return (LCut) baseCutClones.get(this);
		}

		LCut cloned = (LCut) super.clone(intersectionClones, baseCutClones, lineClones);

		baseCutClones.put(this, cloned);

		cloned.horizontalCut = (RegularCut) lineClones.get(this.horizontalCut);
		if (cloned.horizontalCut == null)
		{
			cloned.horizontalCut = this.horizontalCut.clone(intersectionClones, baseCutClones, lineClones);
			lineClones.put(this.horizontalCut, cloned.horizontalCut);
		}

		cloned.verticalCut = (RegularCut) lineClones.get(this.verticalCut);
		if (cloned.verticalCut == null)
		{
			cloned.verticalCut = this.verticalCut.clone(intersectionClones, baseCutClones, lineClones);
			lineClones.put(this.verticalCut, cloned.verticalCut);
		}

		cloned.intersections = new ArrayList<>();
		for (Intersection intersection : this.intersections)
		{
			Intersection clonedIntersection = intersectionClones.get(intersection);
			if (clonedIntersection == null)
			{
				clonedIntersection = intersection.clone(intersectionClones, baseCutClones, lineClones);
				intersectionClones.put(intersection, clonedIntersection);
			}
			cloned.intersections.add(clonedIntersection);
		}

		return cloned;
	}

	public Point2D.Double calculateToolOffsets(Point2D.Double targetPoint)
	{

		double toolDiameter = this.tool.getDiameter();
		double toolOffset = toolDiameter / 2.0;

		double toolOffsetX = toolOffset;
		double toolOffsetY = toolOffset;

		if (targetPoint.getX() < reference.getX())
			toolOffsetX = -toolOffset;

		if (targetPoint.getY() < reference.getY())
			toolOffsetY = -toolOffset;

		return new Point2D.Double(toolOffsetX, toolOffsetY);
	}
}
