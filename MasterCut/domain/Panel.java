package MasterCut.domain;

import MasterCut.domain.cuts.BaseCut;
import MasterCut.domain.cuts.BorderCut;
import MasterCut.domain.cuts.ForbiddenZone;
import MasterCut.domain.cuts.IrregularCut;
import MasterCut.domain.cuts.LCut;
import MasterCut.domain.cuts.RectangularCut;
import MasterCut.domain.cuts.RegularCut;
import MasterCut.domain.dto.*;
import MasterCut.domain.dto.cuts.BaseCutDTO;
import MasterCut.domain.dto.cuts.BorderCutDTO;
import MasterCut.domain.dto.cuts.RectangularCutDTO;
import MasterCut.domain.dto.cuts.RegularCutDTO;
import MasterCut.domain.utils.Dimensions;
import MasterCut.domain.utils.UnitConverter;
import MasterCut.domain.utils.enumPackage.CutType;
import MasterCut.domain.utils.enumPackage.Unit;
import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;

import java.awt.geom.Point2D;
import java.io.Serializable;
import java.util.*;


public class Panel implements Cloneable, Serializable
{
	@JsonProperty
	private Dimensions dimensions;
	@JsonProperty
	private List<Border> borders = new ArrayList<>();
	@JsonProperty
	private Map<UUID, RegularCut> regularCuts = new HashMap<>();
	@JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "uuid")
	@JsonProperty
	private Map<UUID, BaseCut> baseCuts = new HashMap<>();
	@JsonProperty
	private Map<UUID, Intersection> intersections = new HashMap<>();
	@JsonProperty
	private double thickness;
	@JsonProperty
	private Unit usedUnit;
	@JsonProperty
	private Map<UUID, ForbiddenZone> forbiddenZones = new HashMap<>();

	//For Deserialization
	public Panel()
	{

	}

	public Panel(DimensionsDTO dimensions, double thickness, Unit usedUnit)
	{
		updatePanel(dimensions, thickness, usedUnit);
	}

	public void updatePanel(DimensionsDTO dimensions, double thickness, Unit usedUnit)
	{
		Dimensions newDimensions = new Dimensions(dimensions);

		this.usedUnit = usedUnit;

		if (dimensions.getWidth() <= 0 || dimensions.getHeight() <= 0 || thickness <= 0)
		{
			throw new IllegalArgumentException("The dimensions and thickness should be greater than 0");
		}

		//		if (usedUnit == Unit.IMPERIAL)
		//		{
		//			this.dimensions = UnitConverter.convertToMetric(newDimensions);
		//			this.thickness = UnitConverter.convertToMetric(thickness);
		//		} else
		//		{
		this.dimensions = newDimensions;
		this.thickness = thickness;
		//		}

		removeAllCuts();
		updateBorders();
	}

	public RegularCut addRegularCut(Tool tool, Line line, double cutDepth)
	{
		Intersection start = createIntersection(line.getStart().getPoint());
		Intersection end = createIntersection(line.getEnd().getPoint());

		BaseCut parent = line.getParentCut();

		return finalizeRegularCut(tool, line, start, end, cutDepth, parent);
	}

	public Intersection addLCut(Tool tool, double cutDepth, Intersection reference, Point2D.Double point)
	{
		// Créer les intersections nécessaires
		Intersection lPoint = createIntersection(point);
		Intersection yIntersection = createIntersection(new Point2D.Double(reference.getX(), reference.getY()));
		Intersection xIntersection = createIntersection(new Point2D.Double(reference.getX(), reference.getY()));

		BaseCut parent = reference.getBaseCutRef();

		// Créer le LCut et lier les intersections
		LCut newCut = new LCut(tool, cutDepth, reference, lPoint, yIntersection, xIntersection, parent);
		baseCuts.put(newCut.getUUID(), newCut);

		lPoint.setBaseCutRef(newCut);
		yIntersection.setBaseCutRef(newCut);
		xIntersection.setBaseCutRef(newCut);

		// Associer les intersections aux lignes correspondantes
		xIntersection.setHorizontalLine(reference.getHorizontalLine());
		yIntersection.setVerticalLine(reference.getVerticalLine());

		xIntersection.getHorizontalLine().addIntersection(xIntersection);
		yIntersection.getVerticalLine().addIntersection(yIntersection);

		// Associer les RegularCuts au LCut
		for (RegularCut regularCut : newCut.getRegularCuts())
		{
			regularCuts.put(regularCut.getUUID(), regularCut);

			if (regularCut.isHorizontal())
			{
				lPoint.setHorizontalLine(regularCut);
				yIntersection.setHorizontalLine(regularCut);
			} else
			{
				lPoint.setVerticalLine(regularCut);
				xIntersection.setVerticalLine(regularCut);
			}
		}

		// Ajouter comme enfant de la coupe de référence
		addChild(newCut);

		// Marquer le point principal
		lPoint.setMainIntersection(true);

		return lPoint;
	}

	private void addChild(BaseCut baseCut)
	{
		Intersection ref = baseCut.getReference();
		System.out.println("horizontal" + ref.getHorizontalLine());
		System.out.println("vertical" + ref.getVerticalLine());
		System.out.println("basecutref" + ref.getBaseCutRef());
		System.out.println("mainintersection" + ref.isMainIntersection());
		ref.getHorizontalLine().addChild(baseCut);
		ref.getVerticalLine().addChild(baseCut);
	}

	public void editCutDistance(BaseCutDTO cut, Point2D.Double point)
	{
		BaseCut findCut = baseCuts.get(cut.uuid);
		findCut.editCutDistance(point);
	}

	public void setTool(BaseCutDTO cut, Tool tool)
	{
		BaseCut findCut = baseCuts.get(cut.uuid);
		CutType type = findCut.getCutType();
		if (type == CutType.REGULAR)
		{
			findCut.updateTool(tool);
		} else
		{
			findCut.setTool(tool);
		}
	}

	public void editCutDepth(BaseCutDTO cut, double newDepth)
	{
		BaseCut findCut = baseCuts.get(cut.uuid);
		findCut.editCutDepth(newDepth);
	}

	private void updateBorders()
	{
		borders.clear();

		Intersection bottomLeft = createIntersection(new Point2D.Double(0.0, 0.0));
		Intersection bottomRight = createIntersection(new Point2D.Double(dimensions.getWidth(), 0));
		Intersection topRight = createIntersection(new Point2D.Double(dimensions.getWidth(), dimensions.getHeight()));
		Intersection topLeft = createIntersection(new Point2D.Double(0, dimensions.getHeight()));

		Border bottom = createBorder(bottomLeft, bottomRight);
		Border top = createBorder(topLeft, topRight);
		Border left = createBorder(topLeft, bottomLeft);
		Border right = createBorder(topRight, bottomRight);

		bottomLeft.setVerticalLine(left);
		bottomLeft.setHorizontalLine(bottom);

		bottomRight.setVerticalLine(right);
		bottomRight.setHorizontalLine(bottom);

		topLeft.setVerticalLine(left);
		topLeft.setHorizontalLine(top);

		topRight.setVerticalLine(right);
		topRight.setHorizontalLine(top);
	}

	private Border createBorder(Intersection i1, Intersection i2)
	{
		Border border = new Border(i1, i2);
		borders.add(border);
		return border;
	}

	public RectangularCut addRectangularCut(Tool tool, double cutDepth, Intersection referencePoint, Intersection firstPoint, Intersection secondPoint)
	{
		Intersection opposite1 = createIntersection(new Point2D.Double(firstPoint.getPoint().getX(), secondPoint.getPoint().getY()));
		Intersection opposite2 = createIntersection(new Point2D.Double(secondPoint.getPoint().getX(), firstPoint.getPoint().getY()));

		BaseCut parent = referencePoint.getBaseCutRef();

		RectangularCut rectangularCut = new RectangularCut(tool, cutDepth, referencePoint, firstPoint, secondPoint, opposite1, opposite2, parent);
		baseCuts.put(rectangularCut.getUUID(), rectangularCut);

		assignIntersectionReferences(rectangularCut, firstPoint, secondPoint, opposite1, opposite2);

		for (RegularCut regularCut : rectangularCut.getRegularCuts())
			regularCuts.put(regularCut.getUUID(), regularCut);

		addChild(rectangularCut);

		return rectangularCut;
	}

	public ForbiddenZone addForbiddenZone(Intersection point1, Intersection point2, Intersection oppositePoint1, Intersection oppositePoint2)
	{
		ForbiddenZone forbiddenZone = new ForbiddenZone(point1, point2, oppositePoint1, oppositePoint2);
		baseCuts.put(forbiddenZone.getUUID(), forbiddenZone);
		forbiddenZones.put(forbiddenZone.getUUID(), forbiddenZone);
		//forbiddenZone.setCutType(CutType.FORBIDDEN_ZONE);

		for (RegularCut regularCut : forbiddenZone.getRegularCuts())
			regularCuts.put(regularCut.getUUID(), regularCut);

		assignIntersectionReferences(forbiddenZone, point1, point2, oppositePoint1, oppositePoint2);
		return forbiddenZone;
	}

	public boolean areCutsValid()
	{

		int numberOfInvalidCuts = 0;

		for (RegularCut regularCut : regularCuts.values())
		{
			if(regularCut.getCutType() != CutType.FORBIDDEN_ZONE)
				regularCut.setValid(true);

			if(regularCut.getTool() == null && regularCut.getCutType() != CutType.FORBIDDEN_ZONE)
			{
				regularCut.setValid(false);
				numberOfInvalidCuts += 1;
				continue;
			}

			for (ForbiddenZone forbiddenZone : forbiddenZones.values())
			{

				if (forbiddenZone.getRegularCuts().contains(regularCut))
					continue;

				if (regularCut.isHorizontal())
				{
					if ((forbiddenZone.getHorizontalCut1().getStart().getY() <= regularCut.getStart().getY()) && (regularCut.getStart().getY() <= forbiddenZone.getHorizontalCut2().getEnd().getY()))
					{//on est bien dans le range en y, on essait de détecter une coupe reguliere horizontale et une coupe d'une ligne verticale d'une zone interdite
						if (((regularCut.getStart().getX() <= forbiddenZone.getHorizontalCut1().getStart().getX()) && (regularCut.getEnd().getX() >= forbiddenZone.getHorizontalCut1().getStart().getX())) || ((regularCut.getEnd().getX() <= forbiddenZone.getHorizontalCut1().getStart().getX()) && (regularCut.getStart().getX() >= forbiddenZone.getHorizontalCut1().getStart().getX())) || ((regularCut.getStart().getX() <= forbiddenZone.getHorizontalCut1().getEnd().getX()) && (regularCut.getEnd().getX() >= forbiddenZone.getHorizontalCut1().getEnd().getX())) || ((regularCut.getEnd().getX() <= forbiddenZone.getHorizontalCut1().getEnd().getX()) && (regularCut.getStart().getX() >= forbiddenZone.getHorizontalCut1().getEnd().getX())))
						{
							regularCut.setValid(false);
							numberOfInvalidCuts += 1;
						}
					}
				}

				if (!regularCut.isHorizontal())
				{
					if ((forbiddenZone.getHorizontalCut1().getStart().getX() <= regularCut.getStart().getX()) && (regularCut.getStart().getX() <= forbiddenZone.getHorizontalCut1().getEnd().getX()))
					{//on est bien dans le range en x, on essait de détecter une coupe reguliere verticale et une coupe d'une ligne horizontale d'une zone interdite
						if (((regularCut.getStart().getY() >= forbiddenZone.getVerticalCut1().getEnd().getY()) && (regularCut.getEnd().getY() <= forbiddenZone.getVerticalCut1().getEnd().getY())) || ((regularCut.getEnd().getY() >= forbiddenZone.getVerticalCut1().getEnd().getY()) && (regularCut.getStart().getY() <= forbiddenZone.getVerticalCut1().getEnd().getY())) || ((regularCut.getStart().getY() >= forbiddenZone.getVerticalCut1().getStart().getY()) && (regularCut.getEnd().getY() <= forbiddenZone.getVerticalCut1().getStart().getY())) || ((regularCut.getEnd().getY() >= forbiddenZone.getVerticalCut1().getStart().getY()) && (regularCut.getStart().getY() <= forbiddenZone.getVerticalCut1().getStart().getY())))
						{
							regularCut.setValid(false);
							numberOfInvalidCuts += 1;
						}
					}
				}
			}


		}



		System.out.println("you have; " + numberOfInvalidCuts + " invalid cuts");

		return numberOfInvalidCuts == 0;
	}


	private void assignIntersectionReferences(RectangularCut rectangularCut, Intersection int1, Intersection int2, Intersection opp1, Intersection opp2)
	{
		for (Intersection intersection : Arrays.asList(int1, int2, opp1, opp2))
			intersection.setBaseCutRef(rectangularCut);

		int1.setMainIntersection(true);
		// Associe les coupes horizontales et verticales aux intersections correspondantes
		int1.setHorizontalLine(rectangularCut.getHorizontalCut1());
		int1.setVerticalLine(rectangularCut.getVerticalCut1());

		int2.setHorizontalLine(rectangularCut.getHorizontalCut2());
		int2.setVerticalLine(rectangularCut.getVerticalCut2());

		opp1.setHorizontalLine(rectangularCut.getHorizontalCut1());
		opp1.setVerticalLine(rectangularCut.getVerticalCut2());

		opp2.setHorizontalLine(rectangularCut.getHorizontalCut2());
		opp2.setVerticalLine(rectangularCut.getVerticalCut1());
	}

	public BorderCut addBorderCut(Tool tool, double cutDepth, DimensionsDTO newDimensions)
	{
		Dimensions dim = new Dimensions(newDimensions);

		BorderCut cut = new BorderCut(tool, cutDepth, dim, this);
		baseCuts.put(cut.getUUID(), cut);

		for (RegularCut regularcut : cut.getRegularCuts())
			regularCuts.put(regularcut.getUUID(), regularcut);

		updateIntersections();
		return cut;
	}

	public Intersection createIntersection(Point2D.Double point)
	{
		Intersection intersection = new Intersection(new Point2D.Double(point.x, point.y));
		intersections.put(intersection.getUUID(), intersection);
		return intersection;
	}

	public void setDimensions(Dimensions dimensions)
	{
		this.dimensions = dimensions;
		updateBorders();
	}

	public void setThickness(double thickness)
	{
		this.thickness = thickness;
	}

	public void setUsedUnit(Unit usedUnit)
	{
		this.usedUnit = usedUnit;
	}

	public Point2D.Double getDistanceFromReference(BaseCutDTO cut)
	{
		BaseCut findCut = baseCuts.get(cut.uuid);
		if (findCut != null)
			return findCut.getDistanceFromReference();
		else if (cut instanceof RegularCutDTO)
		{
			RegularCut regularCut = regularCuts.get(cut.uuid);
			return regularCut.getDistanceFromReference();
		}
		return null;
	}

	public Map<UUID, Intersection> getIntersections()
	{
		return intersections;
	}

	public Dimensions getDimensions()
	{
		return dimensions;
	}

	public double getThickness()
	{
		return thickness;
	}

	public List<Border> getBorders()
	{
		return borders;
	}

	public Map<UUID, BaseCut> getBaseCut()
	{
		return baseCuts;
	}

	public Unit getUsedUnit()
	{
		return usedUnit;
	}

	public Map<UUID, RegularCut> getRegularCuts()
	{
		return regularCuts;
	}

	public List<BorderDTO> getBordersDTO()
	{
		List<BorderDTO> borderDTOs = new ArrayList<>();

		for (Border b : borders)
			borderDTOs.add(b.getBorderDTO());

		return borderDTOs;
	}

	public PanelDTO getPanelDTO()
	{
		return new PanelDTO(this);
	}

	public Map<UUID, IntersectionDTO> getIntersectionsDTO()
	{
		Map<UUID, IntersectionDTO> intersectionDTOs = new HashMap<>();

		for (Intersection i : intersections.values())
			intersectionDTOs.put(i.getUUID(), i.getIntersectionDTO());

		return intersectionDTOs;
	}

	public Map<UUID, BaseCutDTO> getBaseCutDTO()
	{
		Map<UUID, BaseCutDTO> baseCutDTOMap = new HashMap<>();

		for (BaseCut cut : baseCuts.values())
			baseCutDTOMap.put(cut.getUUID(), cut.getBaseCutDTO());

		return baseCutDTOMap;
	}

	public void removeAllCuts()
	{
		baseCuts.clear();
		regularCuts.clear();
		intersections.clear();
	}

	public void removeCut(BaseCutDTO cut)
	{
		BaseCut findCut = baseCuts.get(cut.uuid);

		if (findCut == null)
		{
			System.out.println("Cut not found: " + cut.uuid);
			return;
		}

		// Parcourir tous les RegularCuts associés
		for (RegularCut regularCut : findCut.getRegularCuts())
		{
			// Parcourir les intersections associées
			for (Intersection intersection : new ArrayList<>(regularCut.getIntersections()))
			{
				// Supprimer l'intersection de la liste globale
				intersections.remove(intersection.getUUID());

				// Nettoyer les références sur les lignes verticales et horizontales
				if (intersection.getVerticalLine() != null)
				{
					intersection.getVerticalLine().removeIntersection(intersection);
					intersection.setVerticalLine(null);
				}
				if (intersection.getHorizontalLine() != null)
				{
					intersection.getHorizontalLine().removeIntersection(intersection);
					intersection.setHorizontalLine(null);
				}

				// Supprimer l'intersection du RegularCut
				regularCut.removeIntersection(intersection);
			}
			// Supprimer le RegularCut de la liste globale
			regularCuts.remove(regularCut.getUUID());
		}

		for (BaseCut baseCut : baseCuts.values())
		{
			for (BaseCut child : baseCut.getChildren())
			{
				if (child.getUUID().equals(findCut.getUUID()))
				{
					baseCut.removeChild(child);
				}
			}
		}

		baseCuts.remove(findCut.getUUID());
	}

	private boolean isPointInLine(Line cut1, Line cut2, Point2D.Double intersectionPoint)
	{
		// Vérification si le point d'intersection est à l'intérieur des deux segments de coupe
		return isPointOnSegment(cut1, intersectionPoint) && isPointOnSegment(cut2, intersectionPoint);
	}

	private boolean isPointOnSegment(Line line, Point2D.Double point)
	{
		Intersection start = line.getStart();
		Intersection end = line.getEnd();

		if (line.isHorizontal())
			return point.getX() >= Math.min(start.getX(), end.getX()) && point.getX() <= Math.max(start.getX(), end.getX());
		else
			return point.getY() >= Math.min(start.getY(), end.getY()) && point.getY() <= Math.max(start.getY(), end.getY());
	}

	public RegularCut addRegularCutFullSize(Tool tool, Line line, double cutDepth)
	{
		Intersection start, end;

		if (line.isHorizontal())
		{
			start = createIntersection(new Point2D.Double(0, line.getStart().getPoint().getY()));
			end = createIntersection(new Point2D.Double(dimensions.getWidth(), line.getStart().getPoint().getY()));
		} else
		{
			start = createIntersection(new Point2D.Double(line.getEnd().getPoint().getX(), 0));
			end = createIntersection(new Point2D.Double(line.getEnd().getPoint().getX(), dimensions.getHeight()));
		}

		return finalizeRegularCut(tool, line, start, end, cutDepth, line.getParentCut());
	}


	private RegularCut finalizeRegularCut(Tool tool, Line line, Intersection start, Intersection end, double cutDepth, BaseCut parent)
	{
		RegularCut newCut = new RegularCut(tool, line.getStart(), start, end, cutDepth, parent);

		start.setBaseCutRef(newCut);
		end.setBaseCutRef(newCut);

		baseCuts.put(newCut.getUUID(), newCut);
		regularCuts.put(newCut.getUUID(), newCut);

		start.setBaseCutRef(newCut);
		end.setBaseCutRef(newCut);

		if (newCut.isHorizontal())
		{
			start.setHorizontalLine(newCut);
			end.setHorizontalLine(newCut);
			start.setVerticalLine(line.getStart().getVerticalLine());
			end.setVerticalLine(line.getStart().getVerticalLine());
		} else
		{
			start.setVerticalLine(newCut);
			end.setVerticalLine(newCut);
			start.setHorizontalLine(line.getStart().getHorizontalLine());
			end.setHorizontalLine(line.getStart().getHorizontalLine());
		}

		addChild(newCut);

		return newCut;
	}


	public void checkForNewIntersections()
	{
		Set<RegularCut> processedCuts = new HashSet<>();

		for (RegularCut cut1 : regularCuts.values())
		{
			for (RegularCut cut2 : regularCuts.values())
			{
				// Éviter de comparer un objet avec lui-même ou des paires déjà traitées
				if (cut1 == cut2 || processedCuts.contains(cut2))
					continue;

				// Vérifier si les orientations sont compatibles pour une intersection
				if (cut1.isHorizontal() == cut2.isHorizontal())
					continue;

				Point2D.Double intersectionPoint = calculateIntersectionPoint(cut1, cut2);
				if (intersectionPoint == null)
					continue;

				// Vérifier si une intersection existe déjà
				Intersection existingIntersection = findIntersection(intersectionPoint);
				if (existingIntersection != null && existingIntersection.haveBothLineSet())
					continue;

				// Créer une nouvelle intersection
				createIntersection(cut1, cut2, intersectionPoint);
			}
			processedCuts.add(cut1); // Marquer cette coupe comme traitée
		}
	}

	private boolean arePointsClose(Point2D.Double p1, Point2D.Double p2, double tolerance)
	{
		return Math.abs(p1.x - p2.x) <= tolerance && Math.abs(p1.y - p2.y) <= tolerance;
	}

	private Intersection findIntersection(Point2D.Double intersectionPoint)
	{
		double tolerance = 1e-12;
		for (Intersection intersection : intersections.values())
		{
			if (arePointsClose(intersection.getPoint(), intersectionPoint, tolerance))
				return intersection;
		}
		return null;
	}

	private void createIntersection(RegularCut cut1, RegularCut cut2, Point2D.Double intersectionPoint)
	{
		Intersection newIntersection = createIntersection(intersectionPoint);
		newIntersection.setBaseCutRef(cut1);
		newIntersection.setBaseCutRef(cut2);
		cut1.addIntersection(newIntersection);
		cut2.addIntersection(newIntersection);
		newIntersection.setRadius(cut1.getThickness());
		newIntersection.setHorizontalLine(cut1.isHorizontal() ? cut1 : cut2);
		newIntersection.setVerticalLine(cut1.isHorizontal() ? cut2 : cut1);
	}

	private Point2D.Double calculateIntersectionPoint(RegularCut cut1, RegularCut cut2)
	{
		if (cut1.isHorizontal() == cut2.isHorizontal())
			return null;

		double x, y;

		if (cut1.isHorizontal())
		{
			x = cut2.getStart().getX();
			y = cut1.getStart().getY();
		} else
		{
			x = cut1.getStart().getX();
			y = cut2.getStart().getY();
		}

		// Vérifier si le point d'intersection est dans les limites des deux coupes
		if (isPointInLine(cut1, cut2, new Point2D.Double(x, y)))
		{
			return new Point2D.Double(x, y);
		}

		return null;
	}

	public void updateIntersections()
	{
		checkForNewIntersections();
		cleanUpIntersection();
		removeNullIntersections();
	}

	private void removeNullIntersections()
	{
		Iterator<Map.Entry<UUID, Intersection>> iterator = intersections.entrySet().iterator();

		while (iterator.hasNext())
		{
			Map.Entry<UUID, Intersection> entry = iterator.next();
			Intersection value = entry.getValue();

			if (value.getHorizontalLine() == null && value.getVerticalLine() == null)
			{
				iterator.remove();
			}
		}
	}

	private void cleanUpIntersection()
	{
		List<Line> lines = new ArrayList<>();
		lines.addAll(regularCuts.values());
		lines.addAll(borders);

		for (int i = 0; i < lines.size(); i++)
		{
			Line line1 = lines.get(i);
			for (int j = i + 1; j < lines.size(); j++)
			{
				Line line2 = lines.get(j);
				Intersection existingIntersection = findExistingIntersection(line1, line2);
				if (existingIntersection != null)
				{
					if (!isValidIntersection(line1, line2, existingIntersection))
						removeIntersection(existingIntersection, line1, line2);
				}
			}
		}
		areCutsValid();
	}

	private boolean isValidIntersection(Line line1, Line line2, Intersection intersection)
	{
		Point2D.Double point = intersection.getPoint();
		return isPointInLine(line1, line2, point);
	}

	private void removeIntersection(Intersection intersection, Line line1, Line line2)
	{
		intersections.remove(intersection.getUUID());

		line1.removeIntersection(intersection);
		line2.removeIntersection(intersection);
	}


	private Intersection findExistingIntersection(Line cut1, Line cut2)
	{
		for (Intersection intersection : cut1.getIntersections())
		{
			if ((intersection.getHorizontalLine() == cut1 && intersection.getVerticalLine() == cut2) || (intersection.getHorizontalLine() == cut2 && intersection.getVerticalLine() == cut1))
				return intersection; // Intersection trouvée
		}
		return null;
	}

	public void editRectangularCut(RectangularCutDTO cutDTO, double newWidth, double newHeight, double newDepth)
	{
		// Récupère la coupe correspondante
		BaseCut findCut = baseCuts.get(cutDTO.uuid);

		// Vérifie que c'est une RectangularCut
		if (!(findCut instanceof RectangularCut rectangularCut))
			throw new IllegalArgumentException("The provided cut is not a RectangularCut.");


		// Cast et modification
		rectangularCut.editRectangularCut(newWidth, newHeight, newDepth);
		updateIntersections();
	}

	public void editBorderCut(BorderCutDTO cut, Tool tool, double newDepth, double newWidth, double newHeight)
	{
		BaseCut findCut = baseCuts.get(cut.uuid);
		if (!(findCut instanceof BorderCut borderCut))
		{
			throw new IllegalArgumentException("The provided cut is not a BorderCut.");
		}
		for (Intersection inter : borderCut.getIntersections())
		{
			Intersection temp = findIntersection(inter.getPoint());
			removeIntersection(temp, temp.getHorizontalLine(), temp.getVerticalLine());
		}
		borderCut.editBorderCut(tool, newDepth, newWidth, newHeight);
		updateIntersections();
	}

	public DimensionsDTO getDimensionsDTO()
	{
		return new DimensionsDTO(this.dimensions);
	}

	void updateLcutFromTool(Tool newtool, BaseCutDTO cut)
	{
		BaseCut findCut = baseCuts.get(cut.uuid);

		if (!(findCut instanceof LCut Lcut))
		{
			throw new IllegalArgumentException("The provided cut is not a LCut.");
		}
		Lcut.UpdateTool(newtool);
	}

	@Override
	public Panel clone()
	{
		try
		{
			Panel cloned = (Panel) super.clone();

			cloned.dimensions = (this.dimensions != null) ? this.dimensions.clone() : null;

			Map<Intersection, Intersection> intersectionClones = new HashMap<>();
			Map<BaseCut, BaseCut> baseCutClones = new HashMap<>();
			Map<Line, Line> lineClones = new HashMap<>();

			// Cloner 'borders'
			cloned.borders = new ArrayList<>();
			for (Border border : this.borders)
			{
				Border clonedBorder = (Border) lineClones.get(border);
				if (clonedBorder == null)
				{
					clonedBorder = border.clone(intersectionClones, baseCutClones, lineClones);
					lineClones.put(border, clonedBorder);
				}
				cloned.borders.add(clonedBorder);
			}

			// Cloner 'intersections'
			cloned.intersections = new HashMap<>();
			for (Map.Entry<UUID, Intersection> entry : this.intersections.entrySet())
			{
				Intersection originalIntersection = entry.getValue();
				Intersection clonedIntersection = intersectionClones.get(originalIntersection);

				// Si l'intersection clonée n'existe pas encore, on la clone
				if (clonedIntersection == null)
				{
					clonedIntersection = originalIntersection.clone(intersectionClones, baseCutClones, lineClones);
					intersectionClones.put(originalIntersection, clonedIntersection);
				}

				cloned.intersections.put(entry.getKey(), clonedIntersection);
			}

			// Cloner 'baseCuts'
			cloned.baseCuts = new HashMap<>();
			for (Map.Entry<UUID, BaseCut> entry : this.baseCuts.entrySet())
			{
				BaseCut originalBaseCut = entry.getValue();
				BaseCut clonedBaseCut = baseCutClones.get(originalBaseCut);
				if (clonedBaseCut == null)
				{
					clonedBaseCut = originalBaseCut.clone(intersectionClones, baseCutClones, lineClones);
					baseCutClones.put(originalBaseCut, clonedBaseCut);
				}
				cloned.baseCuts.put(entry.getKey(), clonedBaseCut);
			}

			// Cloner 'regularCuts'
			cloned.regularCuts = new HashMap<>();
			for (Map.Entry<UUID, RegularCut> entry : this.regularCuts.entrySet())
			{
				RegularCut originalRegularCut = entry.getValue();
				RegularCut clonedRegularCut = (RegularCut) lineClones.get(originalRegularCut);
				if (clonedRegularCut == null)
				{
					clonedRegularCut = originalRegularCut.clone(intersectionClones, baseCutClones, lineClones);
					lineClones.put(originalRegularCut, clonedRegularCut);
				}
				cloned.regularCuts.put(entry.getKey(), clonedRegularCut);
			}

			cloned.thickness = this.thickness;
			cloned.usedUnit = this.usedUnit;

			return cloned;
		} catch (CloneNotSupportedException e)
		{
			throw new AssertionError("Cloning not supported for Panel", e);
		}
	}


	public DimensionsDTO getDimensionsOfCut(BaseCutDTO cut)
	{
		BaseCut findCut = baseCuts.get(cut.uuid);
		if (findCut == null)
			throw new IllegalArgumentException("The provided cut is not a BaseCut.");
		return findCut.getDimensionOfCut();
	}

	@Override
	public String toString()
	{
		return "Panel{" + "dimensions=" + dimensions + ", borders=" + borders + ", intersections=" + intersections + ", regularCuts=" + regularCuts + ", baseCuts=" + baseCuts + ", thickness=" + thickness + ", usedUnit=" + usedUnit + '}';
	}

	public Map<UUID, Line> getLines()
	{
		Map<UUID, Line> allLines = new HashMap<>();

		// Ajouter toutes les RegularCuts
		for (RegularCut regularCut : regularCuts.values())
		{
			allLines.put(regularCut.getUUID(), regularCut);
		}

		// Ajouter toutes les Borders
		for (Border border : borders)
		{
			allLines.put(border.getUUID(), border);
		}

		return allLines;
	}

	public void rebuildLinks()
	{
		Map<UUID, Line> lines = this.getLines();

		// Pour chaque intersection existante
		for (Intersection intersection : new ArrayList<>(this.getIntersections().values()))
		{
			Line foundHorizontalLine = null;
			Line foundVerticalLine = null;

			// Chercher la ligne horizontale correspondant au Y de l'intersection
			for (Line line : lines.values())
			{
				if (line.isHorizontal())
				{
					double lineY = line.getStart().getY();
					double interY = intersection.getY();
					if (Math.abs(lineY - interY) < 0.001 && // tolérance
							intersection.getX() >= Math.min(line.getStart().getX(), line.getEnd().getX()) && intersection.getX() <= Math.max(line.getStart().getX(), line.getEnd().getX()))
					{
						foundHorizontalLine = line;
					}
				} else
				{
					double lineX = line.getStart().getX();
					double interX = intersection.getX();
					if (Math.abs(lineX - interX) < 0.001 && // tolérance
							intersection.getY() >= Math.min(line.getStart().getY(), line.getEnd().getY()) && intersection.getY() <= Math.max(line.getStart().getY(), line.getEnd().getY()))
					{
						foundVerticalLine = line;
					}
				}
			}

			intersection.setHorizontalLine(foundHorizontalLine);
			intersection.setVerticalLine(foundVerticalLine);

			if (foundHorizontalLine != null)
			{
				foundHorizontalLine.addIntersection(intersection);
			}
			if (foundVerticalLine != null)
			{
				foundVerticalLine.addIntersection(intersection);
			}
		}

		// Réassigner baseCutRef aux intersections
		for (BaseCut baseCut : new ArrayList<>(baseCuts.values()))
		{
			for (Intersection intersection : new ArrayList<>(baseCut.getIntersections()))
			{
				intersection.setBaseCutRef(baseCut);
			}

			if (baseCut instanceof IrregularCut irregularCut)
			{
				for (RegularCut regCut : irregularCut.getRegularCuts())
				{
					for (Intersection intersection : new ArrayList<>(regCut.getIntersections()))
					{
						if (intersection.getBaseCutRef() == null || !intersection.getBaseCutRef().getUUID().equals(regCut.getUUID()))
						{
							intersection.setBaseCutRef(regCut);
							regCut.addIntersection(intersection);
						}
					}
				}
			}
		}

		for (RegularCut regularCut : new ArrayList<>(regularCuts.values()))
		{
			for (Intersection intersection : new ArrayList<>(regularCut.getIntersections()))
			{
				if (intersection.getBaseCutRef() == null || !intersection.getBaseCutRef().getUUID().equals(regularCut.getUUID()))
				{
					intersection.setBaseCutRef(regularCut);
				}
			}
		}
	}


}