package MasterCut.domain.cuts;

import MasterCut.domain.Intersection;
import MasterCut.domain.Line;
import MasterCut.domain.Panel;
import MasterCut.domain.Tool;
import MasterCut.domain.dto.DimensionsDTO;
import MasterCut.domain.dto.cuts.BaseCutDTO;
import MasterCut.domain.dto.cuts.BorderCutDTO;
import MasterCut.domain.dto.cuts.RegularCutDTO;
import MasterCut.domain.utils.Clickable;
import MasterCut.domain.utils.Dimensions;
import MasterCut.domain.utils.enumPackage.CutType;
import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import java.awt.geom.Point2D;
import java.io.Serializable;
import java.util.*;

@JsonIdentityInfo(
        generator = ObjectIdGenerators.PropertyGenerator.class,
        property = "uuid")
public class BorderCut extends IrregularCut implements Serializable {

    @JsonProperty
    public final String type = "BorderCut";
    @JsonProperty
    private Dimensions newDimensions;
    @JsonProperty
    private Intersection topLeftCorner;
    @JsonProperty
    private Intersection topRightCorner;
    @JsonProperty
    private Intersection bottomLeftCorner;
    @JsonProperty
    private Intersection bottomRightCorner;
    @JsonProperty
    private Point2D.Double middleOfPanel;
    @JsonProperty
	private double toolOffset;

    //For Deserialization
    public BorderCut() {
        super(null, 0, null, null, null);
    }

	public BorderCut(Tool tool, double depth, Dimensions newDimensions, Panel panel)
	{
		super(tool, depth, null, CutType.BORDER, null);
		this.newDimensions = newDimensions;
		var borders = panel.getBorders();
		Intersection bottomLeftPanelCorner = borders.get(0).getStart();
		Intersection bottomRightPanelCorner = borders.get(0).getEnd();
		Intersection topLeftPanelCorner = borders.get(1).getStart();
		Intersection topRightPanelCorner = borders.get(1).getEnd();
		Double middleHeight = (topLeftPanelCorner.getY() - bottomLeftPanelCorner.getY()) / 2.0d;
		Double middleLength = (topRightPanelCorner.getX() - topLeftPanelCorner.getX()) / 2.0d;
		Double heightRadius = newDimensions.getHeight() / 2.0d;
		Double lengthRadius = newDimensions.getWidth() / 2.0d;
		Double toolRadius = tool.getDiameter() / 2.0d;
		toolOffset = toolRadius;
		middleOfPanel = new Point2D.Double(middleLength, middleHeight);
		Point2D.Double topLeftCutCorner = new Point2D.Double(middleLength - lengthRadius - toolRadius, middleHeight + heightRadius + toolRadius);
		Point2D.Double topRightCutCorner = new Point2D.Double(middleLength + lengthRadius + toolRadius, middleHeight + heightRadius + toolRadius);
		Point2D.Double bottomLeftCutCorner = new Point2D.Double(middleLength - lengthRadius - toolRadius, middleHeight - heightRadius - toolRadius);
		Point2D.Double bottomRightCutCorner = new Point2D.Double(middleLength + lengthRadius + toolRadius, middleHeight - heightRadius - toolRadius);
		topLeftCorner = new Intersection(topLeftCutCorner);
		intersections.add(topLeftCorner);
		topRightCorner = new Intersection(topRightCutCorner);
		intersections.add(topRightCorner);
		bottomLeftCorner = new Intersection(bottomLeftCutCorner);
		intersections.add(bottomLeftCorner);
		bottomRightCorner = new Intersection(bottomRightCutCorner);
		intersections.add(bottomRightCorner);
		RegularCut topCut = new RegularCut(tool, null, topLeftCorner, topRightCorner, depth, this, null);
		RegularCut bottomCut = new RegularCut(tool, null, bottomLeftCorner, bottomRightCorner, depth, this, null);
		RegularCut leftCut = new RegularCut(tool, null, topLeftCorner, bottomLeftCorner, depth, this, null);
		RegularCut rightCut = new RegularCut(tool, null, topRightCorner, bottomRightCorner, depth, this, null);
		cuts.add(bottomCut);
		cuts.add(topCut);
		cuts.add(leftCut);
		cuts.add(rightCut);
	}

	public void editBorderCut(Tool tool, double newDepth, double newWidth, double newHeight)
	{
		toolOffset = tool.getDiameter() / 2.0d;
		RegularCut topCut = cuts.get(0);
		RegularCut bottomCut = cuts.get(1);
		RegularCut leftCut = cuts.get(2);
		RegularCut rightCut = cuts.get(3);
		double newHeightRadius = newHeight / 2.0d;
		double newWidthRadius = newWidth / 2.0d;

		depth = newDepth;
		newDimensions.setWidth(newWidth);
		newDimensions.setHeight(newHeight);
		System.out.println("topLeftPoint before was : " + intersections.get(0).getPoint());
		System.out.println("topRightPoint before was : " + intersections.get(1).getPoint());
		System.out.println("bottomLeftPoint before was : " + intersections.get(2).getPoint());
		System.out.println("bottomRightPoint before was : " + intersections.get(3).getPoint());
		topLeftCorner.setX(middleOfPanel.getX() - newWidthRadius - toolOffset);
		topLeftCorner.setY(middleOfPanel.getY() + newHeightRadius + toolOffset);
		topRightCorner.setX(middleOfPanel.getX() + newWidthRadius + toolOffset);
		topRightCorner.setY(middleOfPanel.getY() + newHeightRadius + toolOffset);
		bottomLeftCorner.setX(middleOfPanel.getX() - newWidthRadius - toolOffset);
		bottomLeftCorner.setY(middleOfPanel.getY() - newHeightRadius - toolOffset);
		bottomRightCorner.setX(middleOfPanel.getX() + newWidthRadius + toolOffset);
		bottomRightCorner.setY(middleOfPanel.getY() - newHeightRadius - toolOffset);

		System.out.println("topLeftPoint is now : " + intersections.get(0).getPoint());
		System.out.println("topRightPoint is now : " + intersections.get(1).getPoint());
		System.out.println("bottomLeftPoint is now : " + intersections.get(2).getPoint());
		System.out.println("bottomRightPoint is now : " + intersections.get(3).getPoint());

		topCut.setStart(topLeftCorner);
		topCut.setEnd(topRightCorner);
		bottomCut.setStart(bottomLeftCorner);
		bottomCut.setEnd(bottomRightCorner);
		leftCut.setStart(topLeftCorner);
		leftCut.setEnd(bottomLeftCorner);
		rightCut.setStart(topRightCorner);
		rightCut.setEnd(bottomRightCorner);
		topCut.editCutDepth(newDepth);
		bottomCut.editCutDepth(newDepth);
		leftCut.editCutDepth(newDepth);
		rightCut.editCutDepth(newDepth);
	}

	@Override
	public List<Intersection> getIntersections()
	{
		return List.of(topLeftCorner, topRightCorner, bottomLeftCorner, bottomRightCorner);
	}

	@Override
	public boolean isPointOnFeature(Point2D.Double point)
	{
		throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override
	public void handleDrag(Point2D.Double dragPoint, Dimensions dimensions)
	{
		throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override
	public Clickable onClick()
	{
		for (RegularCut cut : cuts)
		{
			cut.switchSelectionStatus();
		}
		if (cuts.get(0).isSelected() && cuts.get(1).isSelected() && cuts.get(2).isSelected() && cuts.get(3).isSelected())
		{
			return this;
		} else
		{
			return null;
		}
	}

	@Override
	public void setSelected(boolean b)
	{
		for (RegularCut cut : cuts)
		{
			cut.setSelected(b);
		}
	}

	@Override
	public UUID getUUID()
	{
		return this.uuid;
	}

	@Override
	public Object getDTO()
	{
		return getBaseCutDTO();
	}

	@Override
	public void editCutDistance(Point2D.Double point)
	{
		throw new UnsupportedOperationException("Not implemented for BorderCut");
	}

	@Override
	public void setTool(Tool tool)
	{
		this.tool = tool;
		toolOffset = tool.getDiameter() / 2.0d;
		cuts.get(0).setTool(tool);
		cuts.get(1).setTool(tool);
		cuts.get(2).setTool(tool);
		cuts.get(3).setTool(tool);
		cuts.get(0).updateTickness();
		cuts.get(1).updateTickness();
		cuts.get(2).updateTickness();
		cuts.get(3).updateTickness();
	}

	@Override
	public Point2D.Double getDistanceFromReference()
	{
		return null;
	}

	@Override
	public List<RegularCut> getRegularCuts()
	{
		return cuts;
	}

	@Override
	public List<RegularCutDTO> getRegularCutsDTO()
	{
		return List.of(new RegularCutDTO(cuts.get(0)), new RegularCutDTO(cuts.get(1)), new RegularCutDTO(cuts.get(2)), new RegularCutDTO(cuts.get(3)));
	}

	@Override
	public BaseCutDTO getBaseCutDTO()
	{
		return new BorderCutDTO(this);
	}

	public Dimensions getNewDimensions()
	{
		return newDimensions;
	}


	@Override
	public void changeReference(Intersection intersection)
	{
		throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
	}

	@Override
	public DimensionsDTO getDimensionOfCut()
	{
		double width = newDimensions.getWidth();
		double height = newDimensions.getHeight();
		return new DimensionsDTO(width, height);
	}

	@Override
	public BorderCut clone(Map<Intersection, Intersection> intersectionClones, Map<BaseCut, BaseCut> baseCutClones, Map<Line, Line> lineClones) throws CloneNotSupportedException
	{
		if (baseCutClones.containsKey(this))
		{
			return (BorderCut) baseCutClones.get(this);
		}

		BorderCut cloned = (BorderCut) super.clone(intersectionClones, baseCutClones, lineClones);

		baseCutClones.put(this, cloned);

		cloned.newDimensions = this.newDimensions.clone();

		cloned.topLeftCorner = intersectionClones.get(this.topLeftCorner);
		if (cloned.topLeftCorner == null)
		{
			cloned.topLeftCorner = this.topLeftCorner.clone(intersectionClones, baseCutClones, lineClones);
			intersectionClones.put(this.topLeftCorner, cloned.topLeftCorner);
		}

		cloned.topRightCorner = intersectionClones.get(this.topRightCorner);
		if (cloned.topRightCorner == null)
		{
			cloned.topRightCorner = this.topRightCorner.clone(intersectionClones, baseCutClones, lineClones);
			intersectionClones.put(this.topRightCorner, cloned.topRightCorner);
		}

		cloned.bottomLeftCorner = intersectionClones.get(this.bottomLeftCorner);
		if (cloned.bottomLeftCorner == null)
		{
			cloned.bottomLeftCorner = this.bottomLeftCorner.clone(intersectionClones, baseCutClones, lineClones);
			intersectionClones.put(this.bottomLeftCorner, cloned.bottomLeftCorner);
		}

		// Cloner 'bottomRightCorner'
		cloned.bottomRightCorner = intersectionClones.get(this.bottomRightCorner);
		if (cloned.bottomRightCorner == null)
		{
			cloned.bottomRightCorner = this.bottomRightCorner.clone(intersectionClones, baseCutClones, lineClones);
			intersectionClones.put(this.bottomRightCorner, cloned.bottomRightCorner);
		}

		cloned.cuts = new ArrayList<>();
		for (RegularCut cut : this.cuts)
		{
			RegularCut clonedCut = (RegularCut) lineClones.get(cut);
			if (clonedCut == null)
			{
				clonedCut = cut.clone(intersectionClones, baseCutClones, lineClones);
				lineClones.put(cut, clonedCut);
			}
			cloned.cuts.add(clonedCut);
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

		cloned.middleOfPanel = (Point2D.Double) this.middleOfPanel.clone();

		cloned.toolOffset = this.toolOffset;

		cloned.tool = this.tool != null ? this.tool.clone() : null;

		return cloned;
	}

	@Override
	public void addOffset()
	{

		double toolOffset = this.tool.getDiameter() / 2;

		bottomLeftCorner.setX(bottomLeftCorner.getX() - toolOffset);
		bottomLeftCorner.setY(bottomLeftCorner.getY() - toolOffset);

		topLeftCorner.setX(topLeftCorner.getX() - toolOffset);
		topLeftCorner.setY(topLeftCorner.getY() + toolOffset);

		topRightCorner.setX(topRightCorner.getX() + toolOffset);
		topRightCorner.setY(topRightCorner.getY() + toolOffset);

		bottomRightCorner.setX(bottomRightCorner.getX() + toolOffset);
		bottomRightCorner.setY(bottomRightCorner.getY() - toolOffset);

	}

	@Override
	public void removeOffset()
	{
		double toolOffset = this.tool.getDiameter() / 2;

		bottomLeftCorner.setX(bottomLeftCorner.getX() + toolOffset);
		bottomLeftCorner.setY(bottomLeftCorner.getY() + toolOffset);

		topLeftCorner.setX(topLeftCorner.getX() + toolOffset);
		topLeftCorner.setY(topLeftCorner.getY() - toolOffset);

		topRightCorner.setX(topRightCorner.getX() - toolOffset);
		topRightCorner.setY(topRightCorner.getY() - toolOffset);

		bottomRightCorner.setX(bottomRightCorner.getX() - toolOffset);
		bottomRightCorner.setY(bottomRightCorner.getY() + toolOffset);


	}
        @Override
        public String toString() {
            return "BorderCut{" +
                   "type='" + type + '\'' +
                   ", newDimensions=" + newDimensions +
                   ", topLeftCorner=" + topLeftCorner +
                   ", topRightCorner=" + topRightCorner +
                   ", bottomLeftCorner=" + bottomLeftCorner +
                   ", bottomRightCorner=" + bottomRightCorner +
                   ", middleOfPanel=" + middleOfPanel +
                   ", toolOffset=" + toolOffset +
                   ", intersections=" + intersections +
                   ", cuts=" + cuts +
                   '}';
        }


}






















