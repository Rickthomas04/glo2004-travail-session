package MasterCut.domain;

import MasterCut.domain.cuts.BaseCut;
import MasterCut.domain.cuts.IrregularCut;
import MasterCut.domain.cuts.LCut;
import MasterCut.domain.cuts.RegularCut;
import MasterCut.domain.dto.*;
import MasterCut.domain.dto.cuts.BaseCutDTO;
import MasterCut.domain.dto.cuts.BorderCutDTO;
import MasterCut.domain.dto.cuts.RectangularCutDTO;
import MasterCut.domain.utils.Clickable;
import MasterCut.domain.utils.Dimensions;
import static MasterCut.domain.utils.Message.sendMessage;
import MasterCut.domain.utils.enumPackage.ClickType;
import MasterCut.domain.utils.enumPackage.CutType;
import MasterCut.domain.utils.enumPackage.ModifierKey;
import MasterCut.domain.utils.enumPackage.Unit;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.awt.geom.Point2D;
import java.io.Serializable;
import java.util.*;


public class Cnc implements Cloneable, Serializable
{
	@JsonProperty
	private Dimensions dimensions;
	@JsonProperty
	private Map<UUID, Tool> tools = new HashMap<>(); // remove final for depth copy
	@JsonProperty
	private Grid grid;                               // remove final for depth copy
	
	@JsonProperty
	private Panel panel;

	private static final double DEFAULT_HEIGHT_CNC = 1524;
	private static final double DEFAULT_WIDTH_CNC = 3048;

	private static final double DEFAULT_HEIGHT_PANEL = 1219.2;
	private static final double DEFAULT_WIDTH_PANEL = 914.4;
	private static final double DEFAULT_THICKNESS_PANEL = 6.35;

	private static final String DEFAULT_NAME_TOOL = "Default";
	private static final double DEFAULT_DIAMETER = 12.7;
	private static final int DEFAULT_RPM = 4000;
	private static final double DEFAULT_FEEDRATE = 2.5;

	private static final Unit DEFAULT_PANEL_UNIT = Unit.METRIC;
	private static final Unit DEFAULT_TOOL_UNIT = Unit.IMPERIAL;

	private Clickable itemSelected;
	@JsonProperty
	private Tool activeTool;
	@JsonProperty
	private double cutDepth;

	// For RectangularCut
	private Intersection referencePoint = null;
	private Intersection firstPoint = null;
	private Intersection secondPoint = null;
	private boolean isCreatingRectangle = false;
	private boolean isChangingReference = false;

	public Cnc()
	{
		dimensions = new Dimensions(DEFAULT_WIDTH_CNC, DEFAULT_HEIGHT_CNC);
		createPanel(new DimensionsDTO(DEFAULT_WIDTH_PANEL, DEFAULT_HEIGHT_PANEL), DEFAULT_THICKNESS_PANEL, DEFAULT_PANEL_UNIT);

		Tool defaultTool = new Tool(DEFAULT_NAME_TOOL, DEFAULT_DIAMETER, DEFAULT_RPM, DEFAULT_FEEDRATE, DEFAULT_TOOL_UNIT);
		tools.put(defaultTool.getUUID(), defaultTool);
		activeTool = defaultTool;

		this.grid = new Grid(50);
	}

	public void createPanel(DimensionsDTO dimensions, double thickness, Unit usedUnit)
	{
		panel = new Panel(dimensions, thickness, usedUnit);
	}

	public CncDTO getCncDTO()
	{
		return new CncDTO(this);
	}

	public Dimensions getDimensions()
	{
		return dimensions;
	}

	public List<ToolDTO> getToolsDTO()
	{
		List<ToolDTO> toolDTOs = new ArrayList<>();

		for (Tool tool : tools.values())
			toolDTOs.add(tool.getToolDTO());

		return toolDTOs;
	}

	public Tool getTool(UUID uuid)
	{
		return tools.get(uuid);
	}

	public PanelDTO getPanelDTO()
	{
		if (doesPanelExist())
			return panel.getPanelDTO();
		else
			return null;
	}

	public Tool getActiveTool()
	{
		return activeTool;
	}

	public Object getItemSelected()
	{
		if (itemSelected != null)
			return itemSelected.getDTO();
		else
			return null;
	}

	public void setActiveTool(ToolDTO toolDTO)
	{
		if (toolDTO == null)
		{
			throw new IllegalArgumentException("Cannot set null as active tool");
		}
		Tool tool = getTool(toolDTO.uuid);
		if (tool == null)
		{
			throw new IllegalArgumentException("Tool with UUID " + toolDTO.uuid + " not found in tools map");
		}
		this.activeTool = tool;
	}

	public double getCutDepth()
	{
		return cutDepth;
	}

	public void setCutDepth(double cutDepth)
	{
		this.cutDepth = cutDepth;
	}

	public boolean doesPanelExist()
	{
		return panel != null;
	}

	public void removePanel()
	{
		panel = null;
	}

	public void updatePanel(DimensionsDTO dimensions, double thickness, Unit usedUnit)
	{
		panel.updatePanel(dimensions, thickness, usedUnit);
		this.dimensions = new Dimensions(dimensions);
	}

	public Panel getPanel()
	{
		return panel;
	}

	public void addTool(ToolDTO newToolDTO)
	{
		Tool newTool = new Tool(newToolDTO);
		sendMessage("New Tool added");
		tools.put(newTool.getUUID(), newTool);
	}


	public boolean removeTool(ToolDTO toolDTO)
	{
		Tool tool = tools.get(toolDTO.uuid); // Get the tool by UUID
		if (tool != null)
		{
			for (RegularCut regularCut : panel.getRegularCuts().values())
				if (regularCut.getTool() != null && regularCut.getTool().equals(tool))
					regularCut.setTool(null);
		}
		sendMessage("Tool removed");
		return tools.remove(toolDTO.uuid) != null;
	}

	public int getToolsSize()
	{
		return tools.size();
	}

	public boolean updateTool(ToolDTO toolDTO)
	{
		Tool tool = tools.get(toolDTO.uuid); // Get the tool by UUID
		if (tool != null)
		{
			tool.updateTool(toolDTO); // Update the tool

			for (RegularCut regularCut : panel.getRegularCuts().values())
			{
				regularCut.updateTickness();
			}
			panel.updateIntersections();
			return true;
		}
		return false; // Return false if the tool was not found
	}


	public void addRegularCut(Line line)
	{
		itemSelected = panel.addRegularCut(activeTool, line, cutDepth);
	}

	public void addBorderCut(DimensionsDTO dimension)
	{
		itemSelected = panel.addBorderCut(activeTool, cutDepth, dimension);
	}

	public boolean handleMouseClick(Point2D.Double point, ClickType clickType, boolean isDragging, ModifierKey modifierKey)
	{
		double panelHeight = getPanel().getDimensions().getHeight();
		Point2D.Double adjustedPoint = adjustPointToPanelCoordinates(point, panelHeight);
		Clickable object = getClickedObject(adjustedPoint, isDragging);

		if (object == null)
			deselectItem();

		if (object != null)
		{
			if (isChangingReference && object instanceof Intersection intersection)
			{
				if (itemSelected instanceof BaseCut cut)
				{
					cut.changeReference(intersection);
					isChangingReference = false;
					isDragging = false;
					itemSelected = null;
				}
			} else
			{
				isChangingReference = false;
				switch (clickType)
				{
					case SHORTCLICK -> isDragging = handleShortClick(object, adjustedPoint, modifierKey);
					case LONGCLICK -> isDragging = handleLongClick(object, adjustedPoint, modifierKey);
					case INITDRAG -> isDragging = handleInitDrag(object, adjustedPoint, modifierKey);
					case DRAG -> isDragging = handleDrag(object, adjustedPoint, modifierKey);
				}
			}

		} else if (isCreatingRectangle())
		{

			handleRectanglePointCreation(adjustedPoint);
		} else if (modifierKey == ModifierKey.SHIFT)
		{

			handleForbiddenZonePointSelection(adjustedPoint);
		}


		return isDragging;
	}

	private void handleForbiddenZonePointSelection(Point2D.Double adjustedPoint)
	{
		// Créer ou ajuster une intersection en fonction de la grille magnétique
		Intersection point = grid.isMagnetic() ? panel.createIntersection(grid.snapToGrid(adjustedPoint)) : panel.createIntersection(adjustedPoint);
		point.setSelected(true);

		if (firstPoint == null)
		{
			// Si le premier point n'est pas encore sélectionné, le définir comme premier point
			firstPoint = point;
			sendMessage("First point for ForbiddenZone created at: " + adjustedPoint);
		} else if (secondPoint == null)
		{
			// Si le premier point est déjà sélectionné, le définir comme deuxième point
			secondPoint = point;
			sendMessage("Second point for ForbiddenZone created at: " + adjustedPoint);

			// Créer la ForbiddenZone si les deux points sont prêts
			addForbiddenZoneIfReady();
		}
	}

	private void addForbiddenZoneIfReady()
	{
		if (firstPoint != null && secondPoint != null)
		{
			// Calculez les points opposés
			Intersection oppositePoint1 = panel.createIntersection(new Point2D.Double(firstPoint.getPoint().getX(), secondPoint.getPoint().getY()));
			Intersection oppositePoint2 = panel.createIntersection(new Point2D.Double(secondPoint.getPoint().getX(), firstPoint.getPoint().getY()));

			// Créez et ajoutez la ForbiddenZone
			itemSelected = panel.addForbiddenZone(firstPoint, secondPoint, oppositePoint1, oppositePoint2);
			itemSelected.setSelected(true);
			// Réinitialisez les points après la création
			resetForbiddenZoneCreation();
		}
	}

	private void resetForbiddenZoneCreation()
	{
		if (firstPoint != null)
			firstPoint.setSelected(false);
		if (secondPoint != null)
			secondPoint.setSelected(false);

		firstPoint = null;
		secondPoint = null;
	}

	private Point2D.Double adjustPointToPanelCoordinates(Point2D.Double point, double panelHeight)
	{
		double adjustedY = panelHeight - point.y;
		return new Point2D.Double(point.x, adjustedY);
	}

	private Clickable isPointOnClickableFeature(Point2D.Double adjustedPoint)
	{
		for (Intersection intersection : panel.getIntersections().values())
		{
			if (intersection.isPointOnFeature(adjustedPoint))
				return intersection;
		}

		for (RegularCut regularCut : panel.getRegularCuts().values())
		{
			if (regularCut.isPointOnFeature(adjustedPoint))
				return regularCut;
		}

		for (Border border : panel.getBorders())
		{
			if (border.isPointOnFeature(adjustedPoint))
				return border;
		}

		return null;
	}

	public Point2D.Double getDistanceFromReference(BaseCutDTO cut)
	{
		return panel.getDistanceFromReference(cut);
	}

	public void removeCut(BaseCutDTO cut)
	{
		panel.removeCut(cut);
	}

	public void editCutDistance(BaseCutDTO cut, Point2D.Double point)
	{
		panel.editCutDistance(cut, point);
	}

	public void editCutDepth(BaseCutDTO cut, double newDepth)
	{
		panel.editCutDepth(cut, newDepth);
	}

	public Map<UUID, IntersectionDTO> getIntersectionsDTO()
	{
		return panel.getIntersectionsDTO();
	}

	public void addLCut(Intersection reference, Point2D.Double i1)
	{
		itemSelected = panel.addLCut(activeTool, cutDepth, reference, i1);
	}

	public void addRectangularCut(Intersection reference, Intersection i1, Intersection i2)
	{
		itemSelected = panel.addRectangularCut(activeTool, cutDepth, reference, i1, i2);
	}

	public void SetSelectionCut(BaseCutDTO cut, boolean isSelect)
	{
		Panel panel = this.getPanel();
		UUID id = cut.uuid;
		Map<UUID, BaseCut> BaseCuts = panel.getBaseCut();
		BaseCut foundCut = BaseCuts.get(id);
		for (BaseCut baseCut : BaseCuts.values())
		{
			List<RegularCut> cuts = baseCut.getCuts();
			cuts.forEach(element -> element.setSelected(false));
		}
		if (foundCut != null)
		{
			List<RegularCut> cuts = foundCut.getCuts();
			cuts.forEach(element -> {
				element.setSelected(isSelect);
			});
		} else
		{
		}
	}

	public void editRectangularCut(RectangularCutDTO cut, double newWidth, double newHeight, double newDepth)
	{
		panel.editRectangularCut(cut, newWidth, newHeight, newDepth);
	}

	public void addRegularCutFullSize(Line line)
	{
		itemSelected = panel.addRegularCutFullSize(activeTool, line, cutDepth);
	}

	public void editBorderCut(BorderCutDTO cut, double newDepth, double newWidth, double newHeight)
	{
		panel.editBorderCut(cut, activeTool, newDepth, newWidth, newHeight);
	}

	private boolean handleShortClick(Clickable clickable, Point2D.Double adjustedPoint, ModifierKey modifierKey)
	{
		if (isCreatingRectangle)
			selectRectanglePoints(clickable, adjustedPoint);
		else
			handleSelection(clickable, modifierKey);

		return false;
	}

	private void selectRectanglePoints(Clickable clickable, Point2D.Double adjustedPoint)
	{
		if (firstPoint == null)
		{
			firstPoint = (clickable instanceof Intersection) ? (Intersection) clickable : panel.createIntersection(adjustedPoint);
			firstPoint.setSelected(true);
			sendMessage("First point selected: " + firstPoint.getPoint());
		} else if (secondPoint == null)
		{
			secondPoint = (clickable instanceof Intersection) ? (Intersection) clickable : panel.createIntersection(adjustedPoint);
			secondPoint.setSelected(true);
			addRectangularCutIfReady();
		}
	}


	private boolean handleLongClick(Clickable clickable, Point2D.Double adjustedPoint, ModifierKey modifierKey)
	{
		if (!isCreatingRectangle && clickable instanceof Intersection intersection)
		{
			referencePoint = intersection;
			referencePoint.setSelected(true);
			isCreatingRectangle = true;
			sendMessage("Start of creation RectangularCut\nSelect the firstPoint");
		} else if (isCreatingRectangle)
		{
			sendMessage("Creation mode active, use short click");
		}

		return false;
	}

	private void addRectangularCutIfReady()
	{
		if (activeTool != null && referencePoint != null && firstPoint != null && secondPoint != null)
		{
			panel.addRectangularCut(activeTool, cutDepth, referencePoint, firstPoint, secondPoint);
			resetRectangleCreation();
		}
	}

	private void resetRectangleCreation()
	{
		referencePoint.setSelected(false);
		firstPoint.setSelected(false);
		secondPoint.setSelected(false);
		referencePoint = firstPoint = secondPoint = null;
		isCreatingRectangle = false;
	}


	private void handleSelection(Clickable clickedObject, ModifierKey modifierKey)
	{
		if (clickedObject instanceof RegularCut cut && cut.getParentCut() != null)
		{
			if (itemSelected != null && itemSelected != cut.getParentCut())
				itemSelected.onClick();
		} else if ((itemSelected != null && itemSelected != clickedObject))
			itemSelected.onClick();


		if (clickedObject instanceof RegularCut cut)
		{
			CutType cutType;

			if (cut.getParentCut() != null)
				cutType = cut.getParentCut().getCutType();
			else
				cutType = cut.getCutType();

			switch (cutType)
			{
				case REGULAR:
					itemSelected = clickedObject.onClick(); // Select the RegularCut directly
					break;
				case L_SHAPE, RECTANGULAR, BORDER, FORBIDDEN_ZONE:
					if (modifierKey == ModifierKey.CTRL)
						itemSelected = clickedObject.onClick();
					else
						itemSelected = cut.getParentCut().onClick(); // Select the parent L-shaped cut
					break;
				default:
					sendMessage("Unknown cut type");
					break;
			}
		} else
		{
			if (clickedObject != null)
				itemSelected = clickedObject.onClick(); // Select the new object if not null

			else
			{

				itemSelected = null;
				if (modifierKey == ModifierKey.SHIFT)
				{
					// ICI ICI

				}

			} // Deselect if clicked in an empty area
		}
	}

	private boolean handleInitDrag(Clickable object, Point2D.Double adjustedPoint, ModifierKey modifierKey)
	{
		if (itemSelected != null && object.getUUID().equals(itemSelected.getUUID()))
		{
			switch (object)
			{
				case Border border ->
				{
					border.setSelected(false);
					addRegularCut(border);
					handleSelection(itemSelected, modifierKey);
				}
				case Intersection intersection when modifierKey == ModifierKey.SHIFT ->
				{
					intersection.setSelected(false);
					addLCut(intersection, adjustedPoint);
					handleSelection(itemSelected, modifierKey);
				}
				case RegularCut regularCut when modifierKey == ModifierKey.SHIFT ->
				{
					regularCut.setSelected(false);
					addRegularCutFullSize(regularCut);
					handleSelection(itemSelected, modifierKey);
				}
				default -> object.handleDrag(adjustedPoint, panel.getDimensions());
			}
			itemSelected.setSelected(true);
			return true;
		}
		return false;
	}

	private boolean handleDrag(Clickable object, Point2D.Double adjustedPoint, ModifierKey modifierKey)
	{
		boolean isDragging = false;

		if (itemSelected != null && object.getUUID().equals(itemSelected.getUUID()))
		{
			Point2D.Double snapPoint = calculateDragPoint(adjustedPoint);
			itemSelected.handleDrag(snapPoint, panel.getDimensions());
			isDragging = true;
		}
		panel.updateIntersections();
		return isDragging;
	}

	private Point2D.Double calculateDragPoint(Point2D.Double adjustedPoint)
	{
		Point2D.Double snapPoint = grid.isMagnetic() ? grid.snapToGrid(adjustedPoint) : adjustedPoint;

		UUID objectId = itemSelected != null ? itemSelected.getUUID() : null;

		if (objectId != null)
		{
			Intersection intersection = panel.getIntersections().get(objectId);

			if (intersection != null)
			{
				BaseCut baseCut = panel.getBaseCut().get(intersection.getBaseCutRef() != null ? intersection.getBaseCutRef().getUUID() : null);

				if (baseCut instanceof LCut lCut)
				{
					Point2D.Double offset = lCut.calculateToolOffsets(snapPoint);
					snapPoint = new Point2D.Double(snapPoint.x + offset.x, snapPoint.y + offset.y);
				}
			}
		}

		return snapPoint;
	}


	private void handleRectanglePointCreation(Point2D.Double adjustedPoint)
	{
		Intersection point = grid.isMagnetic() ? panel.createIntersection(grid.snapToGrid(adjustedPoint)) : panel.createIntersection(adjustedPoint);
		point.setSelected(true);

		if (firstPoint == null)
		{
			firstPoint = point;
			sendMessage("First point created at: " + adjustedPoint);
		} else if (secondPoint == null)
		{
			secondPoint = point;
			addRectangularCutIfReady();
		}
	}

	public void deselectItem()
	{
		if (itemSelected != null)
		{
			itemSelected.setSelected(false);
			itemSelected = null;
		}
	}

	private Clickable getClickedObject(Point2D.Double adjustedPoint, boolean isDragging)
	{
		if (!isDragging)
			return isPointOnClickableFeature(adjustedPoint);
		else
			return itemSelected;
	}

	public boolean isCreatingRectangle()
	{
		return isCreatingRectangle;
	}

	public ToolDTO getActiveToolDTO()
	{
		return activeTool.getToolDTO();
	}

	public GridDTO getGridDTO()
	{
		return grid.getGridDTO();
	}

	public void showGrid(boolean showGrid)
	{
		grid.setShowGrid(showGrid);
	}

	public void setGridSize(int gridSize)
	{
		grid.setGridSize(gridSize);
	}

	public void setGridMagnetic(boolean magnetic)
	{
		grid.setMagnetic(magnetic);
	}

	public boolean isGridMagnetic()
	{
		return grid.isMagnetic();
	}

	public void setReferenceMode(boolean bool)
	{
		isChangingReference = bool;
	}

	public void updateLcutFromTool(ToolDTO newtool, BaseCutDTO cut)
	{
		Tool findTool = tools.get(newtool.uuid);

		if (findTool == null)
			throw new IllegalArgumentException("The provided cut is not a Tool.");

		panel.updateLcutFromTool(findTool, cut);
	}

	public Map<UUID, Tool> getTools()
	{
		return tools;
	}

	@Override
	public Cnc clone()
	{
		try
		{
			Cnc clonedCnc = (Cnc) super.clone();

			// Clonage en profondeur des dimensions
			clonedCnc.dimensions = (this.dimensions != null) ? this.dimensions.clone() : null;

			// Clonage en profondeur du panneau
			clonedCnc.panel = (this.panel != null) ? this.panel.clone() : null;

			// Clonage en profondeur des outils
			clonedCnc.tools = new HashMap<>(); // Nouvelle instance de map
			for (Map.Entry<UUID, Tool> entry : this.tools.entrySet())
			{
				Tool clonedTool = entry.getValue().clone();
				clonedCnc.tools.put(entry.getKey(), clonedTool);
			}

			// Clonage en profondeur de la grid
			clonedCnc.grid = (this.grid != null) ? this.grid.clone() : null;

			// Réassignation de l'outil actif
			clonedCnc.activeTool = (this.activeTool != null) ? clonedCnc.tools.get(this.activeTool.getUUID()) : null;

			// Clonage de la profondeur de coupe
			clonedCnc.cutDepth = this.cutDepth;

			// Clonage des points de référence (si nécessaire)
			//clonedCnc.referencePoint = (this.referencePoint != null) ? this.referencePoint.clone() : null;
			//clonedCnc.firstPoint = (this.firstPoint != null) ? this.firstPoint.clone() : null;
			//clonedCnc.secondPoint = (this.secondPoint != null) ? this.secondPoint.clone() : null;
			clonedCnc.isCreatingRectangle = this.isCreatingRectangle;

			// Clonage de l'élément sélectionné (si nécessaire)
			//clonedCnc.itemSelected = (this.itemSelected != null) ? this.itemSelected.clone() : null;

			return clonedCnc;
		} catch (CloneNotSupportedException e)
		{
			throw new AssertionError("Cloning not supported", e);
		}
	}

	public void restoreFrom(Cnc other)
	{


		// Dimensions
		if (other.dimensions != null)
		{
			this.dimensions = other.dimensions.clone();

		} else
		{

		}

		// Panel
		if (other.panel != null)
		{
                        for (Intersection intersection : panel.getIntersections().values()) {
                            System.out.println("Intersection : " + intersection.getUUID() + 
                                ", baseCutRef avant : " + (intersection.getBaseCutRef() != null ? intersection.getBaseCutRef().getUUID() : "null"));
                        }
			this.panel = other.panel.clone();
			System.out.println("Panel restauré.");
			this.panel.rebuildLinks();
                        this.panel.updateIntersections();
                       // Imprimer baseCutRef de chaque intersection
                        System.out.println("=== Vérification des baseCutRef des intersections ===");
                        for (Intersection intersection : panel.getIntersections().values()) {
                            System.out.println("Intersection : " + intersection.getUUID() + 
                                ", baseCutRef : " + (intersection.getBaseCutRef() != null ? intersection.getBaseCutRef().getUUID() : "null"));
                        }
System.out.println("=== Liste des baseCuts ===");
for (BaseCut baseCut : panel.getBaseCut().values()) {
    System.out.println("BaseCut : " + baseCut.getUUID() + ", Type : " + baseCut.getClass().getSimpleName() +
        ", Intersections : " + baseCut.getIntersections().size());

    // Vérifie si le BaseCut contient des RegularCut
    if (baseCut instanceof IrregularCut) { // IrregularCut peut contenir des RegularCut
        IrregularCut irregularCut = (IrregularCut) baseCut;
        System.out.println("  Ce BaseCut contient des RegularCuts :");

        // Parcourt les RegularCuts associés à l'IrregularCut
        for (RegularCut regularCut : irregularCut.getRegularCuts()) {
            System.out.println("    RegularCut : " + regularCut.getUUID() + 
                ", Nombre d'intersections : " + regularCut.getIntersections().size());

            // Parcourt les intersections du RegularCut
            for (Intersection intersection : regularCut.getIntersections()) {
                System.out.println("      - Intersection : " + intersection.getUUID() + 
                    ", X : " + intersection.getX() + ", Y : " + intersection.getY() +
                    ", BaseCutRef : " + (intersection.getBaseCutRef() != null ? intersection.getBaseCutRef().getUUID() : "null") +
                    ", HorizontalLine : " + (intersection.getHorizontalLine() != null ? intersection.getHorizontalLine() + "size" + intersection.getHorizontalLine().getIntersections() : "null") +
                    ", VerticalLine : " + (intersection.getVerticalLine() != null ? intersection.getVerticalLine() + "size" + intersection.getVerticalLine().getIntersections() : "null"));
            }
        }
    }
}


                        
		} else
		{
			this.panel = null;
			System.out.println("Aucun panneau à restaurer.");
		}

		// Outils (création d'une map temporaire)
		Map<UUID, Tool> tempTools = new HashMap<>();
		System.out.println("Nombre d'outils à restaurer : " + other.tools.size());
		for (Map.Entry<UUID, Tool> entry : other.tools.entrySet())
		{
			Tool originalTool = entry.getValue();
			Tool clonedTool = originalTool.clone(); // Clonage de l'outil
			tempTools.put(entry.getKey(), clonedTool);
			System.out.println("Clone ajouté : " + clonedTool.getName() + ", UUID : " + clonedTool.getUUID());
		}
		System.out.println("la grosseur 1" + this.tools.size());
		this.tools.clear(); // Vider la map actuelle
		this.tools.putAll(tempTools); // Remplacer avec les outils clonés
		System.out.println("Nombre d'outils après restauration : " + this.tools.size());

		// Grille
		this.grid.setGridSize(other.grid.getGridSize());
		this.grid.setMagnetic(other.grid.isMagnetic());
		System.out.println("Grille restaurée : Taille = " + this.grid.getGridSize() + ", Magnétique = " + this.grid.isMagnetic());

		// Outil actif
		if (other.activeTool != null)
		{
			this.activeTool = other.activeTool.clone();
			System.out.println("Outil actif restauré : " + this.activeTool.getName() + ", UUID : " + this.activeTool.getUUID());
		} else
		{
			this.activeTool = null;
			System.out.println("Aucun outil actif à restaurer.");
		}

		// Profondeur de coupe
		this.cutDepth = other.cutDepth;
		System.out.println("Profondeur de coupe restaurée : " + this.cutDepth);

		System.out.println("Fin de la méthode restoreFrom");
	}

	public void setTool(BaseCutDTO cut, ToolDTO tool)
	{
		Tool findTool = tools.get(tool.uuid);
		panel.setTool(cut, findTool);
	}

	public Object getDTOOnHover(Point2D.Double point)
	{
		Point2D.Double adjustedPoint = adjustPointToPanelCoordinates(point, panel.getDimensions().getHeight());

		Clickable item = isPointOnClickableFeature(adjustedPoint);

		if (item != null)
			if (item instanceof BaseCut cut)
			{
				if (cut.getParentCut() != null)
					return cut.getParentCut().getDTO();
				else
					return cut.getDTO();
			}

		return null;
	}

	public DimensionsDTO getDimensionsOfCut(BaseCutDTO cut)
	{
		return panel.getDimensionsOfCut(cut);
	}

	@Override
	public String toString()
	{
		return "Cnc{" + "dimensions=" + dimensions + ", panel=" + panel + ", tools=" + tools + ", grid=" + grid + ", activeTool=" + activeTool + ", cutDepth=" + cutDepth + ", isCreatingRectangle=" + isCreatingRectangle + '}';
    }

    public boolean areCutsValid() {
        return panel.areCutsValid();
    }

}
