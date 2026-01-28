package MasterCut.domain.cuts;

import MasterCut.domain.Intersection;
import MasterCut.domain.Line;
import MasterCut.domain.Tool;
import MasterCut.domain.dto.DimensionsDTO;
import MasterCut.domain.dto.cuts.BaseCutDTO;
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
import java.util.*;
import java.util.List;
/*@JsonIdentityInfo(
        generator = ObjectIdGenerators.PropertyGenerator.class,
        property = "uuid")*/
public class RegularCut extends BaseCut implements Clickable, Cloneable, Serializable, Line
{

    @JsonProperty
    public String type = "RegularCut";
    @JsonProperty
    private Intersection start;
	@JsonProperty
	private Intersection end;
	@JsonProperty
	private int thickness;
	private boolean selectionStatus = false;
	@JsonProperty
	private Color displayColor;
	@JsonProperty
	private Color toolColor;
	@JsonProperty
    private boolean isHorizontal;
    @JsonProperty
    private List<Intersection> intersections = new ArrayList<>(); // remove final pour depth copy
    @JsonIdentityInfo(
            generator = ObjectIdGenerators.PropertyGenerator.class,
            property = "uuid")
    @JsonProperty
    private IrregularCut parentCut = null;


	private final int TOLERANCE = 10;
	private final Color DEFAULT_SELECTED_COLOR = new Color(74, 199, 0, 255);
	private final Color DEFAULT_TOOL_COLOR = Color.RED;
	private final int DEFAULT_THICKNESS = 5;

	@Override
	public Tool getTool()
	{//modified
            if(this.tool != null)
		return this.tool;
            else
                return null;
	}

	//For Deserialization
	public RegularCut()
	{
		super(0, null, null, null, null); // TODO: check for the last argument if is ok
		this.isHorizontal = false;
	}

	// Primary constructor for standalone RegularCut
	public RegularCut(Tool tool, Intersection reference, Intersection start, Intersection end, double depth, BaseCut parent)
	{
		super(depth, reference, tool, CutType.REGULAR, parent);
		initialize(tool, start, end);
	}

	// Constructor for RegularCut as part of an IrregularCut
	public RegularCut(Tool tool, Intersection reference, Intersection start, Intersection end, double depth, IrregularCut parentCut, BaseCut parent)
	{
		super(depth, reference, tool, parentCut.getCutType(), parent);
		initialize(tool, start, end);
		this.parentCut = parentCut;
	}

	private void initialize(Tool tool, Intersection start, Intersection end)
	{
		this.tool = tool;

		if(tool != null)
		{
			this.toolColor = tool.getColor();
			this.displayColor = tool.getColor();
			this.thickness = (int) tool.getDiameter();
		}
		else
		{
			this.toolColor = DEFAULT_TOOL_COLOR;
			this.displayColor = DEFAULT_TOOL_COLOR;
			this.thickness = DEFAULT_THICKNESS;
		}
		this.start = start;
		this.end = end;

		// Determine orientation based on intersections
		this.isHorizontal = isIntersectionAreHorizontal();

		// Add start and end intersections
		intersections.add(start);
		intersections.add(end);

		start.setRadius(thickness);
		end.setRadius(thickness);

		sendMessage("Regular Cut created");
	}

	public void setRegParentCut(IrregularCut parentCut)
	{
		this.parentCut = parentCut;
	}

	private boolean isIntersectionAreHorizontal()
	{
		return start.getY() == end.getY();
	}

	public UUID getUUID()
	{
		return uuid;
	}

	@Override
	public Object getDTO()
	{
		return getBaseCutDTO();
	}

	@Override
	public double getLenght()
	{
		if (isHorizontal)
			return Math.abs(end.getX() - start.getX());
		else
			return Math.abs(end.getY() - start.getY());
	}

	@Override
	public BaseCut getParentCut()
	{
		return parentCut;
	}

	@Override
	public List<Intersection> getIntersections()
	{
		return intersections;
	}

	@Override
	public void addIntersection(Intersection intersection)
	{
		this.intersections.add(intersection);
	}

	@Override
	public void removeIntersection(Intersection intersection)
	{
		this.intersections.remove(intersection);
	}

	@Override
	public void setDisplayColor(Color displayColor)
	{
		this.displayColor = displayColor;
	}

	@Override
	public boolean isHorizontal()
	{
		return isHorizontal;
	}

	public Color getDisplayColor()
	{
		return displayColor;
	}

	public boolean isSelected()
	{
		return selectionStatus;
	}


	public void updateTickness()
	{
		if(tool != null)
		{
			this.thickness = (int) tool.getDiameter();
			for (Intersection intersection : intersections)
				intersection.setRadius((int) tool.getDiameter());
		}
	}

	@Override
	public void setSelected(boolean selected)
	{
		this.selectionStatus = selected;
		updateState();
	}

	public int getThickness()
	{
		return thickness;
	}

	public void setThickness(int thickness)
	{
		this.thickness = thickness;
	}

	@Override
	public Intersection getEnd()
	{
		return end;
	}

	public void setEnd(Intersection end)
	{
		this.end = end;
	}

	@Override
	public Intersection getStart()
	{
		return start;
	}

	public void setStart(Intersection start)
	{
		this.start = start;
	}

	@Override
	public boolean isPointOnFeature(Point2D.Double point)
	{
		double halfThickness = DEFAULT_THICKNESS / 2.0;

		double expandedXMin = Math.min(start.getX(), end.getX()) - TOLERANCE - halfThickness;
		double expandedXMax = Math.max(start.getX(), end.getX()) + TOLERANCE + halfThickness;
		double expandedYMin = Math.min(start.getY(), end.getY()) - TOLERANCE - halfThickness;
		double expandedYMax = Math.max(start.getY(), end.getY()) + TOLERANCE + halfThickness;

		return (point.getX() >= expandedXMin && point.getX() <= expandedXMax) && (point.getY() >= expandedYMin && point.getY() <= expandedYMax);
	}

	@Override
	public void addChild(BaseCut baseCut)
	{
		children.add(baseCut);
	}

	@Override
	public void removeChild(BaseCut baseCut)
	{
		children.remove(baseCut);
	}

	@Override
	public void handleDrag(Point2D.Double dragPoint, Dimensions dimensions)
	{
		double maxDimension = isHorizontal ? dimensions.getHeight() : dimensions.getWidth();
		double toolOffset = 0;

		if(tool != null)
			toolOffset = tool.getDiameter() / 2.0;

		// Calculer la nouvelle position selon la référence
		double newPos = calculateNewPosition(dragPoint, toolOffset);

		// Vérifier si la nouvelle position est dans les limites
		if (isWithinBounds(newPos, maxDimension))
		{
			// Sauvegarder les anciennes distances des enfants
			Map<BaseCut, Point2D.Double> oldDistances = new HashMap<>();
			for (BaseCut child : getAllChilds())
				oldDistances.put(child, child.getDistanceFromReference());

			// Mise à jour de la position de cette coupe
			if (isHorizontal)
				for (Intersection intersection : intersections)
					intersection.setY(newPos);
			else
				for (Intersection intersection : intersections)
					intersection.setX(newPos);

			for (Map.Entry<BaseCut, Point2D.Double> entry : oldDistances.entrySet())
			{
				BaseCut child = entry.getKey();
				Point2D.Double oldDistance = entry.getValue();
				child.editCutDistance(oldDistance);
			}
		}
	}


	private double calculateNewPosition(Point2D.Double dragPoint, double toolOffset)
	{
		if (haveNegativeReference())
			return isHorizontal ? dragPoint.getY() - toolOffset : dragPoint.getX() - toolOffset;
		else
			return isHorizontal ? dragPoint.getY() + toolOffset : dragPoint.getX() + toolOffset;
	}

	private boolean isWithinBounds(double value, double max)
	{
		return value >= 0 && value <= max;
	}

	public void switchSelectionStatus()
	{
		selectionStatus = !selectionStatus;
		updateState();
	}

	public void updateState()
	{
		if (selectionStatus)
			setDisplayColor(DEFAULT_SELECTED_COLOR);
		else
			setDisplayColor(toolColor);
	}

	public boolean haveNegativeReference()
	{
		if (isHorizontal)
			return start.getY() < reference.getY();
		else
			return start.getX() < reference.getX();
	}

	@Override
	public void addOffset()
	{
		double toolOffset = this.tool.getDiameter() / 2;

		for (Intersection intersection : intersections)
		{
			if (isHorizontal)
			{
				double newY;
				if (haveNegativeReference())
				{
					newY = intersection.getY() + toolOffset;
					intersection.setY(newY);
				} else
				{
					newY = intersection.getY() - toolOffset;
					intersection.setY(newY);
				}
			} else
			{
				double newX;
				if (haveNegativeReference())
				{
					newX = intersection.getX() + toolOffset;
					intersection.setX(newX);
				} else
				{
					newX = intersection.getX() - toolOffset;
					intersection.setX(newX);
				}
			}
		}
	}

	@Override
	public void removeOffset()
	{
		double toolOffset = this.tool.getDiameter() / 2;

		for (Intersection intersection : intersections)
		{
			if (isHorizontal)
			{
				double newY;
				if (haveNegativeReference())
				{
					newY = intersection.getY() - toolOffset;
					intersection.setY(newY);
				} else
				{
					newY = intersection.getY() + toolOffset;
					intersection.setY(newY);
				}
			} else
			{
				double newX;
				if (haveNegativeReference())
				{
					newX = intersection.getX() - toolOffset;
					intersection.setX(newX);
				} else
				{
					newX = intersection.getX() + toolOffset;
					intersection.setX(newX);
				}
			}
		}
	}

	@Override
	public Clickable onClick()
	{
		switchSelectionStatus();
		return selectionStatus ? this : null;
	}

	@Override
	public void setTool(Tool tool)
	{
		this.tool = tool;

		if(tool != null)
			toolColor = tool.getColor();
		else
			toolColor = Color.MAGENTA;

		this.updateTickness();
	}

	@Override
	public void updateTool(Tool tool)
	{
		this.tool = tool;
		super.updateTool(tool);
	}

	@Override
	public List<RegularCutDTO> getRegularCutsDTO()
	{
		List<RegularCutDTO> regularCutsDTO = new ArrayList<>();
		regularCutsDTO.add(new RegularCutDTO(this));
		return regularCutsDTO;
	}

	@Override
	public BaseCutDTO getBaseCutDTO()
	{
		return new RegularCutDTO(this);
	}

	@Override
	public List<RegularCut> getRegularCuts()
	{
		List<RegularCut> regularCuts = new ArrayList<>();
		regularCuts.add(this);
		return regularCuts;
	}

	@Override
	public Point2D.Double getDistanceFromReference()
	{
		double toolOffset = 0;

		if(tool != null)
			toolOffset = tool.getDiameter() / 2.0;

		// Calcul des offsets des outils des références
		double parentHorizontalOffset = (reference.getHorizontalLine() != null && reference.getHorizontalLine().getTool() != null) ? reference.getHorizontalLine().getTool().getDiameter() / 2.0 : 0.0;
		double parentVerticalOffset = (reference.getVerticalLine() != null && reference.getVerticalLine().getTool() != null) ? reference.getVerticalLine().getTool().getDiameter() / 2.0 : 0.0;

		// Calcul de la distance en fonction de l'orientation
		double referencePosition = isHorizontal ? reference.getY() : reference.getX();
		double startPosition = isHorizontal ? start.getY() : start.getX();
		double parentOffset = isHorizontal ? parentHorizontalOffset : parentVerticalOffset;

		double distanceValue = startPosition - referencePosition - toolOffset - parentOffset;

		// Retourner la distance sous forme de Point2D
		return isHorizontal ? new Point2D.Double(0, distanceValue) : new Point2D.Double(distanceValue, 0);
	}


	@Override
	public void editCutDistance(Point2D.Double distance)
	{
		double targetPosition = calculateTargetPosition(distance);

		Map<BaseCut, Point2D.Double> oldDistances = saveChildrenDistances();

		moveIntersections(targetPosition);
		applyOldDistancesToChildren(oldDistances);
	}

	private double calculateTargetPosition(Point2D.Double distance)
	{
		double toolOffset = tool.getDiameter() / 2.0;

		// Calculer les offsets des outils des références uniquement si nécessaires
		double parentHorizontalOffset = (reference.getHorizontalLine() != null && reference.getHorizontalLine().getTool() != null) ? reference.getHorizontalLine().getTool().getDiameter() / 2.0 : 0.0;

		double parentVerticalOffset = (reference.getVerticalLine() != null && reference.getVerticalLine().getTool() != null) ? reference.getVerticalLine().getTool().getDiameter() / 2.0 : 0.0;

		// Déterminer l'axe en fonction de l'orientation et calculer la position cible
		double referencePosition = isHorizontal ? reference.getY() : reference.getX();
		double distanceValue = isHorizontal ? distance.y : distance.x;
		double parentOffset = isHorizontal ? parentHorizontalOffset : parentVerticalOffset;

		return referencePosition + distanceValue + toolOffset + parentOffset;
	}


	private Map<BaseCut, Point2D.Double> saveChildrenDistances()
	{
		Map<BaseCut, Point2D.Double> oldDistances = new HashMap<>();

		for (BaseCut child : getAllChilds())
			oldDistances.put(child, child.getDistanceFromReference());

		return oldDistances;
	}

	private void moveIntersections(double targetPosition)
	{
		for (Intersection intersection : intersections)
		{
			if (isHorizontal)
				intersection.setY(targetPosition);
			else
				intersection.setX(targetPosition);
		}
	}

	private void applyOldDistancesToChildren(Map<BaseCut, Point2D.Double> oldDistances)
	{
		for (Map.Entry<BaseCut, Point2D.Double> entry : oldDistances.entrySet())
		{
			BaseCut child = entry.getKey();
			Point2D.Double oldDistance = entry.getValue();
			child.editCutDistance(oldDistance);
		}
	}

	public void setToolColor(Color color)
	{
		this.toolColor = color;
	}

	public void setHorizontal(boolean b)
	{
		this.isHorizontal = b;
	}

	@Override
	public void changeReference(Intersection intersection)
	{
		setReference(intersection);
		editCutDistance(start.getPoint());
	}

	@Override
	public DimensionsDTO getDimensionOfCut()
	{
		DimensionsDTO dimensionsDTO = new DimensionsDTO();

		if (isHorizontal)
		{
			dimensionsDTO.width = Math.abs(end.getX() - start.getX());
			dimensionsDTO.height = Math.abs(getDistanceFromReference().y);
		} else
		{
			dimensionsDTO.width = Math.abs(getDistanceFromReference().x);
			dimensionsDTO.height = Math.abs(end.getY() - start.getY());
		}

		return dimensionsDTO;
	}

	@Override
	public void setIntersections(List<Intersection> intersections)
	{
		this.intersections = intersections;
	}

    @Override
    public RegularCut clone(Map<Intersection, Intersection> intersectionClones, 
                            Map<BaseCut, BaseCut> baseCutClones, 
                            Map<Line, Line> lineClones) throws CloneNotSupportedException {
        RegularCut cloned = (RegularCut) super.clone(); // Appeler le clone de la classe parente

        // Cloner les intersections associées
        cloned.intersections = new ArrayList<>();
        for (Intersection intersection : this.getIntersections()) {
            Intersection clonedIntersection = intersectionClones.get(intersection);
            if (clonedIntersection == null) {
                clonedIntersection = intersection.clone(intersectionClones, baseCutClones, lineClones);
                intersectionClones.put(intersection, clonedIntersection);
            }
            cloned.addIntersection(clonedIntersection);
        }



        System.out.println("RegularCut cloné : " + cloned.getUUID() + ", Nombre d'intersections : " + cloned.getIntersections().size());
        return cloned;
    }

        @Override
        public boolean containsPoint(Point2D.Double point, double tolerance) {
            // Obtenir les points de début et de fin de la ligne
            Point2D.Double startPoint = this.getStart().getPoint();
            Point2D.Double endPoint = this.getEnd().getPoint();

            // Vérifier si le point est sur la ligne (distance <= tolérance)
            return distanceFromPointToLine(point, startPoint, endPoint) <= tolerance;
        }

        private double distanceFromPointToLine(Point2D.Double point, Point2D.Double lineStart, Point2D.Double lineEnd) {
            double dx = lineEnd.x - lineStart.x;
            double dy = lineEnd.y - lineStart.y;

            double lineLengthSquared = dx * dx + dy * dy;
            if (lineLengthSquared == 0) {
                // Si la ligne est un point (start == end), calcule la distance au point
                return point.distance(lineStart);
            }

            // Projection du point sur la ligne
            double t = ((point.x - lineStart.x) * dx + (point.y - lineStart.y) * dy) / lineLengthSquared;
            t = Math.max(0, Math.min(1, t)); // Contraindre t entre 0 et 1

            // Calcul des coordonnées du point projeté
            double projectionX = lineStart.x + t * dx;
            double projectionY = lineStart.y + t * dy;

            // Distance entre le point et le point projeté
            return point.distance(projectionX, projectionY);
        }

}
