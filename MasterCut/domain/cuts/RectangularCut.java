package MasterCut.domain.cuts;

import MasterCut.domain.Intersection;
import MasterCut.domain.Line;
import MasterCut.domain.Tool;
import MasterCut.domain.dto.DimensionsDTO;
import MasterCut.domain.dto.cuts.BaseCutDTO;
import MasterCut.domain.dto.cuts.RectangularCutDTO;
import MasterCut.domain.dto.cuts.RegularCutDTO;
import MasterCut.domain.utils.Clickable;
import MasterCut.domain.utils.Dimensions;
import static MasterCut.domain.utils.Message.sendMessage;
import MasterCut.domain.utils.enumPackage.CutType;
import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "uuid")
/*@JsonIdentityReference(alwaysAsId = true)
@JsonTypeInfo(
        use = JsonTypeInfo.Id.CLASS
)*/ public class RectangularCut extends IrregularCut
{
	@JsonProperty
	public final String type = "RectangularCut";
	@JsonProperty
	private RegularCut horizontalCut1;
	@JsonProperty
	private RegularCut horizontalCut2;
	@JsonProperty
	private RegularCut verticalCut1;
	@JsonProperty
	private RegularCut verticalCut2;
	@JsonProperty
	private Intersection point1;
	@JsonProperty
	private Intersection point2;
	@JsonProperty
	private double height;
	@JsonProperty
	private double width;
	@JsonProperty
	private double distanceFromReferenceY;
	@JsonProperty
	private double distanceFromReferenceX;
	@JsonProperty
	private Point2D.Double pointReference;
	@JsonProperty
	private ForbiddenZone forbiddenZone;

	public RectangularCut()
	{
		super(null, 0, null, null, null);
	}

	public RectangularCut(Tool tool, double depth, Intersection reference, Intersection point1, Intersection point2, Intersection oppositePoint1, Intersection oppositePoint2, BaseCut parent)
	{
            this(tool, depth, reference, point1, point2, oppositePoint1, oppositePoint2, parent, null);
	}

	public RectangularCut(Tool tool, double depth, Intersection reference, Intersection point1, Intersection point2, Intersection oppositePoint1, Intersection oppositePoint2, BaseCut parent, ForbiddenZone forbiddenZone)
	{
		super(tool, depth, reference, CutType.RECTANGULAR, parent);

		if (forbiddenZone != null)
		{
			super.setCutType(CutType.FORBIDDEN_ZONE);
			this.horizontalCut1 = new RegularCut(tool, reference, point1, oppositePoint2, depth, forbiddenZone, parent);
			this.horizontalCut2 = new RegularCut(tool, point1, point2, oppositePoint1, depth, forbiddenZone, parent);
			this.verticalCut1 = new RegularCut(tool, reference, point1, oppositePoint1, depth, forbiddenZone, parent);
			this.verticalCut2 = new RegularCut(tool, point1, point2, oppositePoint2, depth, forbiddenZone, parent);
		} else
		{
			this.horizontalCut1 = new RegularCut(tool, reference, point1, oppositePoint2, depth, this, parent);
			this.horizontalCut2 = new RegularCut(tool, point1, point2, oppositePoint1, depth, this, parent);
			this.verticalCut1 = new RegularCut(tool, reference, point1, oppositePoint1, depth, this, parent);
			this.verticalCut2 = new RegularCut(tool, point1, point2, oppositePoint2, depth, this, parent);
		}


		this.point1 = point1;
		this.point2 = point2;

		this.height = Math.abs(point1.getY() - point2.getY());
		this.width = Math.abs(point1.getX() - point2.getX());

		this.reference = reference;

		this.distanceFromReferenceY = point1.getY() - reference.getY();
		this.distanceFromReferenceX = point1.getX() - reference.getX();
		this.pointReference = new Point2D.Double(distanceFromReferenceX, distanceFromReferenceY);

		this.horizontalCut1.setHorizontal(true);
		this.horizontalCut2.setHorizontal(true);
		this.verticalCut1.setHorizontal(false);
		this.verticalCut2.setHorizontal(false);

		this.intersections.add(reference);
		this.intersections.add(point1);
		this.intersections.add(point2);
		this.intersections.add(oppositePoint1);
		this.intersections.add(oppositePoint2);

		sendMessage("Rectangle Cut created");
	}

	public RegularCut getHorizontalCut1()
	{
		return horizontalCut1;
	}

	public RegularCut getHorizontalCut2()
	{
		return horizontalCut2;
	}

	public RegularCut getVerticalCut1()
	{
		return verticalCut1;
	}

	public RegularCut getVerticalCut2()
	{
		return verticalCut2;
	}

	@Override
	public List<RegularCut> getRegularCuts()
	{
		return Arrays.asList(horizontalCut1, horizontalCut2, verticalCut1, verticalCut2);
	}

	@Override
	public BaseCutDTO getBaseCutDTO()
	{
		return new RectangularCutDTO(this);
	}

	@Override
	public void editCutDistance(Point2D.Double point)
	{
		Point2D.Double dimensions = new Point2D.Double(width, height);

		Map<BaseCut, Point2D.Double> oldDistances = new HashMap<>();
		for (RegularCut regularCut : getCuts())
			for (BaseCut baseCut : regularCut.getChildren())
				oldDistances.put(baseCut, baseCut.getDistanceFromReference());

		horizontalCut1.editCutDistance(point);
		verticalCut1.editCutDistance(point);
		horizontalCut2.editCutDistance(dimensions);
		verticalCut2.editCutDistance(dimensions);

		// Réappliquer les distances relatives sauvegardées aux enfants
		for (Map.Entry<BaseCut, Point2D.Double> entry : oldDistances.entrySet())
		{
			BaseCut child = entry.getKey();
			Point2D.Double oldDistance = entry.getValue();
			child.editCutDistance(oldDistance);
		}
	}

	@Override
	public void addOffset()
	{

		if (this.tool == null)
		{

			return;

		}

		double toolOffset = tool.getDiameter() / 2.0;
		// Offset basé sur le rayon de l'outil


		if (point1.getX() < point2.getX())
		{
			horizontalCut1.getStart().setX(horizontalCut1.getStart().getX() - toolOffset);
			horizontalCut1.getEnd().setX(horizontalCut1.getEnd().getX() + toolOffset);
			horizontalCut2.getStart().setX(horizontalCut1.getEnd().getX());
			horizontalCut2.getEnd().setX(horizontalCut1.getStart().getX());

			verticalCut1.getStart().setX(horizontalCut1.getStart().getX());
			verticalCut1.getEnd().setX(horizontalCut1.getStart().getX());
			verticalCut2.getStart().setX(horizontalCut1.getEnd().getX());
			verticalCut2.getEnd().setX(horizontalCut1.getEnd().getX());


		} else
		{
			horizontalCut1.getStart().setX(horizontalCut1.getStart().getX() + toolOffset);
			horizontalCut1.getEnd().setX(horizontalCut1.getEnd().getX() - toolOffset);
			horizontalCut2.getStart().setX(horizontalCut1.getEnd().getX());
			horizontalCut2.getEnd().setX(horizontalCut1.getStart().getX());

			verticalCut1.getStart().setX(horizontalCut1.getStart().getX());
			verticalCut1.getEnd().setX(horizontalCut1.getStart().getX());
			verticalCut2.getStart().setX(horizontalCut1.getEnd().getX());
			verticalCut2.getEnd().setX(horizontalCut1.getEnd().getX());

		}

		if (point1.getY() < point2.getY())
		{
			horizontalCut1.getStart().setY(horizontalCut1.getStart().getY() - toolOffset);
			horizontalCut1.getEnd().setY(horizontalCut1.getStart().getY());
			horizontalCut2.getStart().setY(horizontalCut2.getStart().getY() + toolOffset);
			horizontalCut2.getEnd().setY(horizontalCut2.getStart().getY());

			verticalCut1.getStart().setY(horizontalCut1.getStart().getY());
			verticalCut1.getEnd().setY(horizontalCut2.getStart().getY());
			verticalCut2.getStart().setY(horizontalCut2.getStart().getY());
			verticalCut2.getEnd().setY(horizontalCut1.getStart().getY());

		} else
		{
			horizontalCut1.getStart().setY(horizontalCut1.getStart().getY() + toolOffset);
			horizontalCut1.getEnd().setY(horizontalCut1.getStart().getY());
			horizontalCut2.getStart().setY(horizontalCut2.getStart().getY() - toolOffset);
			horizontalCut2.getEnd().setY(horizontalCut2.getStart().getY());

			verticalCut1.getStart().setY(horizontalCut1.getStart().getY());
			verticalCut1.getEnd().setY(horizontalCut2.getStart().getY());
			verticalCut2.getStart().setY(horizontalCut2.getStart().getY());
			verticalCut2.getEnd().setY(horizontalCut1.getStart().getY());
		}

	}

	@Override
	public void removeOffset()
	{
		if (this.tool == null)
		{
			return;
		}

		double toolOffset = tool.getDiameter() / 2.0; // Offset basé sur le rayon de l'outil

		if (point1.getX() < point2.getX())
		{
			horizontalCut1.getStart().setX(horizontalCut1.getStart().getX() + toolOffset);
			horizontalCut2.getEnd().setX(horizontalCut1.getStart().getX());

			horizontalCut1.getEnd().setX(horizontalCut1.getEnd().getX() - toolOffset);
			horizontalCut2.getStart().setX(horizontalCut1.getEnd().getX());

		} else
		{
			horizontalCut1.getStart().setX(horizontalCut1.getStart().getX() - toolOffset);
			horizontalCut2.getEnd().setX(horizontalCut1.getStart().getX());

			horizontalCut1.getEnd().setX(horizontalCut1.getEnd().getX() + toolOffset);
			horizontalCut2.getStart().setX(horizontalCut1.getEnd().getX());

		}

		if (point1.getY() < point2.getY())
		{
			horizontalCut1.getStart().setY(horizontalCut1.getStart().getY() + toolOffset);
			horizontalCut1.getEnd().setY(horizontalCut1.getStart().getY());

			horizontalCut2.getStart().setY(horizontalCut2.getStart().getY() - toolOffset);
			horizontalCut2.getEnd().setY(horizontalCut2.getStart().getY());

		} else
		{
			horizontalCut1.getStart().setY(horizontalCut1.getStart().getY() - toolOffset);
			horizontalCut1.getEnd().setY(horizontalCut1.getStart().getY());

			horizontalCut2.getStart().setY(horizontalCut2.getStart().getY() + toolOffset);
			horizontalCut2.getEnd().setY(horizontalCut2.getStart().getY());
		}
	}

	@Override
	public void setTool(Tool tool)
	{
		this.tool = tool;

		this.horizontalCut1.setTool(tool);
		this.horizontalCut2.setTool(tool);
		this.verticalCut1.setTool(tool);
		this.verticalCut2.setTool(tool);

		this.horizontalCut1.updateTickness();
		this.horizontalCut2.updateTickness();
		this.verticalCut1.updateTickness();
		this.verticalCut2.updateTickness();
	}

	@Override
	public Point2D.Double getDistanceFromReference()
	{
		double x = verticalCut1.getDistanceFromReference().getX();
		double y = horizontalCut1.getDistanceFromReference().getY();

		return new Point2D.Double(x, y);
	}

	@Override
	public List<RegularCutDTO> getRegularCutsDTO()
	{
		List<RegularCutDTO> regularCutsDTO = new ArrayList<>();
		regularCutsDTO.addAll(horizontalCut1.getRegularCutsDTO());
		regularCutsDTO.addAll(horizontalCut2.getRegularCutsDTO());
		regularCutsDTO.addAll(verticalCut1.getRegularCutsDTO());
		regularCutsDTO.addAll(verticalCut2.getRegularCutsDTO());
		return regularCutsDTO;
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
		for (RegularCut regularCut : getRegularCuts())
			for (BaseCut baseCut : regularCut.getChildren())
				oldDistances.put(baseCut, baseCut.getDistanceFromReference());

		point1.setPoint(dragPoint);
		point2.setPoint(new Point2D.Double(dragPoint.getX() + width,dragPoint.getY() + height));
		intersections.get(3).setPoint(new Point2D.Double(dragPoint.getX() + width,dragPoint.getY()));
		intersections.get(4).setPoint(new Point2D.Double(dragPoint.getX(),dragPoint.getY() + height));

		for (Map.Entry<BaseCut, Point2D.Double> entry : oldDistances.entrySet())
		{
			BaseCut child = entry.getKey();
			Point2D.Double oldDistance = entry.getValue();
			child.editCutDistance(oldDistance);
		}
	}

	@Override
	public Clickable onClick()
	{
		horizontalCut1.switchSelectionStatus();
		horizontalCut2.switchSelectionStatus();
		verticalCut1.switchSelectionStatus();
		verticalCut2.switchSelectionStatus();

		if (horizontalCut1.isSelected() && horizontalCut2.isSelected() && verticalCut1.isSelected() && verticalCut2.isSelected())
			return this;
		else
			return null;
	}

	@Override
	public void setSelected(boolean b)
	{
		horizontalCut1.setSelected(b);
		horizontalCut2.setSelected(b);
		verticalCut1.setSelected(b);
		verticalCut2.setSelected(b);
	}

	@Override
	public Object getDTO()
	{
		return getBaseCutDTO();
	}

	public void editRectangularCut(double newWidth, double newHeight, double newDepth)
	{
		// Validation des dimensions
		if (newWidth <= 0 || newHeight <= 0 || newDepth < 0)
		{
			throw new IllegalArgumentException("Width, height, and depth must be positive values.");
		}
		//point 1 is bottom left, point 2 is top right
		if (point1.getX() < point2.getX() && point1.getY() < point2.getY())
		{
			verticalCut1.getEnd().setY(point1.getY() + newHeight);
			horizontalCut2.getEnd().setY(verticalCut1.getEnd().getY());
			horizontalCut2.getStart().setY(verticalCut1.getEnd().getY());
			verticalCut2.getStart().setY(verticalCut1.getEnd().getY());


			horizontalCut1.getEnd().setX((point1.getX() + newWidth));
			verticalCut2.getEnd().setX(horizontalCut1.getEnd().getX());
			verticalCut2.getStart().setX(horizontalCut1.getEnd().getX());
			horizontalCut2.getStart().setX(horizontalCut1.getEnd().getX());

			//point 2 is bottom left, point 1 is top right
		} else if (point1.getX() > point2.getX() && point1.getY() > point2.getY())
		{


			verticalCut1.getEnd().setY(point1.getY() - newHeight);
			horizontalCut2.getEnd().setY(verticalCut1.getEnd().getY());
			horizontalCut2.getStart().setY(verticalCut1.getEnd().getY());
			verticalCut2.getStart().setY(verticalCut1.getEnd().getY());


			horizontalCut1.getEnd().setX(point1.getX() - newWidth);
			verticalCut2.getEnd().setX(horizontalCut1.getEnd().getX());
			verticalCut2.getStart().setX(horizontalCut1.getEnd().getX());
			horizontalCut2.getStart().setX(horizontalCut1.getEnd().getX());

			//point 1 is top left, point 2 is bottom right
		} else if (point1.getX() < point2.getX() && point1.getY() > point2.getY())
		{


			verticalCut1.getEnd().setY(point1.getY() - newHeight);
			horizontalCut2.getEnd().setY(verticalCut1.getEnd().getY());
			horizontalCut2.getStart().setY(verticalCut1.getEnd().getY());
			verticalCut2.getStart().setY(verticalCut1.getEnd().getY());


			horizontalCut1.getEnd().setX(point1.getX() + newWidth);
			verticalCut2.getEnd().setX(horizontalCut1.getEnd().getX());
			verticalCut2.getStart().setX(horizontalCut1.getEnd().getX());
			horizontalCut2.getStart().setX(horizontalCut1.getEnd().getX());
		}

		//point 1 is bottom right, point 2 is top left
		else
		{


			verticalCut1.getEnd().setY(point1.getY() + newHeight);
			horizontalCut2.getEnd().setY(verticalCut1.getEnd().getY());
			horizontalCut2.getStart().setY(verticalCut1.getEnd().getY());
			verticalCut2.getStart().setY(verticalCut1.getEnd().getY());


			horizontalCut1.getEnd().setX(point1.getX() - newWidth);
			verticalCut2.getEnd().setX(horizontalCut1.getEnd().getX());
			verticalCut2.getStart().setX(horizontalCut1.getEnd().getX());
			horizontalCut2.getStart().setX(horizontalCut1.getEnd().getX());
		}

		this.height = newHeight;
		this.width = newWidth;

		if (tool == null)
		{
			this.depth = 0;
		} else
		{
			this.depth = newDepth;
		}

		horizontalCut1.setDepth(newDepth);
		horizontalCut2.setDepth(newDepth);
		verticalCut1.setDepth(newDepth);
		verticalCut2.setDepth(newDepth);

		//expandRectangularCut();
	}

	@Override
	public void changeReference(Intersection intersection)
	{
		setReference(intersection);
		editCutDistance(pointReference);
	}

	@Override
	public DimensionsDTO getDimensionOfCut()
	{
		return new DimensionsDTO(getWidth(), getHeight());
	}

	public double getWidth()
	{
		return this.width;
	}

	public double getHeight()
	{
		return this.height;
	}

	public Intersection getPoint1()
	{
		return this.point1;
	}

	public Intersection getPoint2()
	{
		return this.point2;
	}

	public double getDistanceFromReferenceX()
	{
		return this.distanceFromReferenceX;
	}

	public double getDistanceFromReferenceY()
	{
		return this.distanceFromReferenceY;
	}

	@Override
	public RectangularCut clone(Map<Intersection, Intersection> intersectionClones, Map<BaseCut, BaseCut> baseCutClones, Map<Line, Line> lineClones) throws CloneNotSupportedException
	{

		if (baseCutClones.containsKey(this))
		{
			return (RectangularCut) baseCutClones.get(this);
		}


		RectangularCut cloned = (RectangularCut) super.clone(intersectionClones, baseCutClones, lineClones);


		baseCutClones.put(this, cloned);


		cloned.horizontalCut1 = (RegularCut) lineClones.get(this.horizontalCut1);
		if (cloned.horizontalCut1 == null)
		{
			cloned.horizontalCut1 = this.horizontalCut1.clone(intersectionClones, baseCutClones, lineClones);
			lineClones.put(this.horizontalCut1, cloned.horizontalCut1);
		}


		cloned.horizontalCut2 = (RegularCut) lineClones.get(this.horizontalCut2);
		if (cloned.horizontalCut2 == null)
		{
			cloned.horizontalCut2 = this.horizontalCut2.clone(intersectionClones, baseCutClones, lineClones);
			lineClones.put(this.horizontalCut2, cloned.horizontalCut2);
		}


		cloned.verticalCut1 = (RegularCut) lineClones.get(this.verticalCut1);
		if (cloned.verticalCut1 == null)
		{
			cloned.verticalCut1 = this.verticalCut1.clone(intersectionClones, baseCutClones, lineClones);
			lineClones.put(this.verticalCut1, cloned.verticalCut1);
		}


		cloned.verticalCut2 = (RegularCut) lineClones.get(this.verticalCut2);
		if (cloned.verticalCut2 == null)
		{
			cloned.verticalCut2 = this.verticalCut2.clone(intersectionClones, baseCutClones, lineClones);
			lineClones.put(this.verticalCut2, cloned.verticalCut2);
		}


		cloned.point1 = intersectionClones.get(this.point1);
		if (cloned.point1 == null)
		{
			cloned.point1 = this.point1.clone(intersectionClones, baseCutClones, lineClones);
			intersectionClones.put(this.point1, cloned.point1);
		}


		cloned.point2 = intersectionClones.get(this.point2);
		if (cloned.point2 == null)
		{
			cloned.point2 = this.point2.clone(intersectionClones, baseCutClones, lineClones);
			intersectionClones.put(this.point2, cloned.point2);
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


		cloned.height = this.height;
		cloned.width = this.width;

		cloned.tool = this.tool != null ? this.tool.clone() : null;

		return cloned;
	}

}
