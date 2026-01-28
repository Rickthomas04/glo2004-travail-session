package MasterCut.domain;

import MasterCut.domain.dto.*;
import MasterCut.domain.dto.cuts.BaseCutDTO;
import MasterCut.domain.dto.cuts.BorderCutDTO;
import MasterCut.domain.dto.cuts.RectangularCutDTO;
import MasterCut.domain.dto.cuts.RegularCutDTO;
import MasterCut.domain.utils.GCodeGenerator;
import MasterCut.domain.utils.Message;
import MasterCut.domain.utils.TextParser;
import MasterCut.domain.utils.enumPackage.ClickType;
import MasterCut.domain.utils.enumPackage.ModifierKey;
import MasterCut.domain.utils.enumPackage.Unit;
import java.awt.geom.Point2D;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import javax.swing.tree.DefaultMutableTreeNode;

public class Controller implements Serializable
{

	private Cnc cnc; //check si on doit le laisser final ou non
	private final MemoryManager memoryManager;
	private final SaveManager saveManager = new SaveManager();

	public Controller()
	{
		this.cnc = new Cnc();
		this.memoryManager = new MemoryManager(this);
	}

	public PanelDTO getPanelDTO()
	{
		return cnc.getPanelDTO();
	}

	public double getPanelDTOHeight()
	{
		return cnc.getPanelDTO().dimensions.getHeight();
	}

	public GridDTO getGridDTO()
	{
		return cnc.getGridDTO();
	}

	public boolean doesPanelExist()
	{
		return cnc.doesPanelExist();
	}

	public void removePanel()   
	{       
                memoryManager.add(cnc,getToolsDTO()); 
		cnc.removePanel();
	}

	public Object getItemSelectedDTO()
	{
		return cnc.getItemSelected();
	}

	public void updatePanel(DimensionsDTO dimensionsDTO, double thickness, Unit usedUnit)
	{       

		cnc.updatePanel(dimensionsDTO, thickness, usedUnit);
	}

	public void createPanel(DimensionsDTO dimensions, double thickness, Unit usedUnit)
	{       memoryManager.clearStacks();
		cnc.createPanel(dimensions, thickness, usedUnit);
	}

	public Map<UUID, IntersectionDTO> getIntersectionsDTO()
	{
		return cnc.getIntersectionsDTO();
	}

	public void showGrid(boolean show)
	{
		cnc.showGrid(show);
	}

	public List<ToolDTO> getToolsDTO()
	{
		return cnc.getToolsDTO();
	}

	public ToolDTO getActiveToolDTO()
	{
		return cnc.getActiveToolDTO();
	}

	public void addTool(ToolDTO newTool)
	{       
                memoryManager.add(cnc,getToolsDTO());  
		cnc.addTool(newTool);
	}

        public boolean removeTool(ToolDTO tool) {
                memoryManager.add(cnc,getToolsDTO());  
                return cnc.removeTool(tool); 
        }

	public int getToolsSize()
	{       
		return cnc.getToolsSize();
	}

	public boolean updateTool(ToolDTO tool)
	{       memoryManager.add(cnc,getToolsDTO()); 
		return cnc.updateTool(tool);
	}

	public void setActiveTool(ToolDTO tool)
	{
		cnc.setActiveTool(tool);
	}

	public void setCutDepth(Double cutDepth)
	{       memoryManager.add(cnc,getToolsDTO()); 
		cnc.setCutDepth(cutDepth);
	}

	public Point2D.Double getDistanceFromReference(BaseCutDTO cut)
	{
		return cnc.getDistanceFromReference(cut);
	}

	public void removeCut(BaseCutDTO cut)
	{   
            memoryManager.add(cnc,getToolsDTO()); 
            cnc.removeCut(cut);
	}

	public void editCutDistance(BaseCutDTO cut, Point2D.Double point)
	{  
                memoryManager.add(cnc,getToolsDTO()); 
		cnc.editCutDistance(cut, point);
	}

	public void setTool(BaseCutDTO cut, ToolDTO tool)
	{           
                memoryManager.add(cnc,getToolsDTO()); 
		cnc.setTool(cut, tool);
	}

	public void editCutDepth(BaseCutDTO cut, double newDepth)
	{       //memoryManager.add(cnc);
		cnc.editCutDepth(cut, newDepth);
	}

	public void setUsedUnit(Unit unit)
	{
		cnc.getPanel().setUsedUnit(unit);
	}

	public void setGridSize(int gridSize)
	{
		cnc.setGridSize(gridSize);
	}

	public void setGridMagnetic(boolean magnetic)
	{
		cnc.setGridMagnetic(magnetic);
	}

	public boolean isGridMagnetic()
	{
		return cnc.isGridMagnetic();
	}

	public DefaultMutableTreeNode getToolsTree()
	{
		DefaultMutableTreeNode root = new DefaultMutableTreeNode("Tools");


		List<ToolDTO> tools = getToolsDTO();
		PanelDTO panelDTO = getPanelDTO();
		Map<UUID, BaseCutDTO> cutsMap = panelDTO.cutsMap;

		for (ToolDTO tool : tools)
		{
			DefaultMutableTreeNode toolNode = new DefaultMutableTreeNode(tool.name);

			// Ça fonctionne pas
			int cutCount = 1;
			for (BaseCutDTO cut : cutsMap.values())
			{
				if (cut.tool == null)
					continue;

				if (cut.tool.uuid.equals(tool.uuid))
				{
					DefaultMutableTreeNode cutNode = new DefaultMutableTreeNode(cutCount + ". " + cut.name);
					toolNode.add(cutNode);
					cutCount++;
				}

			}

			root.add(toolNode);
		}

		return root;
	}

	public void addCut(RegularCutDTO cut)
	{
                memoryManager.add(cnc,getToolsDTO()); 
		Intersection start = new Intersection(cut.start.point);
		Intersection end = new Intersection(cut.end.point);
		Border border = new Border(start, end);
		cnc.addRegularCut(border); // TODO CHECK PARENT NULL
	}

	public void addCut(DimensionsDTO dimension)
	{
                memoryManager.add(cnc,getToolsDTO()); 
		cnc.addBorderCut(dimension);
	}

	public void SetSelectionCut(BaseCutDTO Cut, boolean isSelect)
	{       memoryManager.add(cnc,getToolsDTO()); 
		cnc.SetSelectionCut(Cut, isSelect);
	}

	public void SetSelectionCut(String NodeTextChild, String NodeTextParent, boolean isSelect)
	{
		ToolDTO Tool = getSelectedTools(NodeTextParent);
		if (Tool != null)
		{
			BaseCutDTO Cut = getSelectedCut(NodeTextChild, Tool);
			if (Cut != null)
			{
				SetSelectionCut(Cut, isSelect);

			}
			System.out.println("Fin");
		}
	}

	public ToolDTO getSelectedTools(String nodeText)
	{
		List<ToolDTO> tools = cnc.getToolsDTO();

		for (ToolDTO tool : tools)
			if (tool.name.equals(nodeText))
				return tool;

		return null;
	}

	public BaseCutDTO getSelectedCut(String nodeText, ToolDTO tool)
	{
		int cutIndex = TextParser.extractCutIndex(nodeText);
		UUID toolUuid = tool.uuid;

		if (cutIndex >= 0)
		{
			PanelDTO panelDTO = getPanelDTO();
			List<BaseCutDTO> cuts = new ArrayList<>(panelDTO.cutsMap.values());

			List<BaseCutDTO> filteredCuts = new ArrayList<>();
			for (BaseCutDTO cut : cuts)
				if (cut.tool.uuid.equals(toolUuid))
					filteredCuts.add(cut);

			if (cutIndex < filteredCuts.size())
				return filteredCuts.get(cutIndex);
			else
				System.out.println("Index de coupe invalide.");
		} else
			System.out.println("Format de texte de nœud invalide.");

		return null;
	}

	public void editRectangularCut(RectangularCutDTO cut, double newWidth, double newHeight, double newDepth)
	{       memoryManager.add(cnc,getToolsDTO()); 
		cnc.editRectangularCut(cut, newWidth, newHeight, newDepth);
	}

	public void changeReferenceMode()
	{       memoryManager.add(cnc,getToolsDTO()); 
		cnc.setReferenceMode(true);
	}

	public void editBorderCut(BorderCutDTO cut, double newDepth, double newWidth, double newHeight)
	{       memoryManager.add(cnc,getToolsDTO()); 
		cnc.editBorderCut(cut, newDepth, newWidth, newHeight);
	}

	public boolean handleMouseClick(Point2D.Double aDouble, ClickType clickType, boolean isDragging, ModifierKey modifierKey)
	{
		int sizebefore = cnc.getPanel().getIntersections().size();
                memoryManager.add(cnc,getToolsDTO()); 
		boolean handleMouse = cnc.handleMouseClick(aDouble, clickType, isDragging, modifierKey);
		int sizeafter = cnc.getPanel().getIntersections().size();
		int difference = Math.abs(sizeafter - sizebefore);
		if (difference <= 1)
		{       
			memoryManager.remove();
		}

		return handleMouse;
	}

	public void save()
	{
		//memoryManager.add(cnc);
		System.out.println("sauvegarde effectué");
	}

	public void undo()
	{       memoryManager.addredo(cnc,getToolsDTO());
                
		//cnc = memoryManager.undo();
                Cnc cncUndo = memoryManager.undo();
                if (cncUndo != null) cnc = cncUndo;
                cnc.getPanel().updateIntersections();
                System.out.println("undo effectué");
	}

	public void redo()
	{
                Cnc cncRedo = memoryManager.redo();
                if (cncRedo != null) cnc = cncRedo;
		//cnc = memoryManager.redo();
        }
	public Object getDTOOnHover(Point2D.Double ajustedPoint)
	{
		return cnc.getDTOOnHover(ajustedPoint);
	}

	public DimensionsDTO getDimensionsOfCut(BaseCutDTO cut)
	{
		return cnc.getDimensionsOfCut(cut);
	}

	public void SavePanel(String path) throws IOException
	{
		saveManager.SavePanel(cnc, path);
	}

	public void LoadPanel(String path) throws IOException
	{
		cnc = saveManager.LoadPanel(path);
	}

	public void generateGCode(String path) throws IOException
	{
        if (areCutsValid()) {
            GCodeGenerator.generateGCodeFile(cnc, path);
        } else {
            Message.sendMessage("There are invalid cuts, cannot generate GCODE");
        }

    }

    public boolean areCutsValid() {
        return cnc.areCutsValid();
    }
}

