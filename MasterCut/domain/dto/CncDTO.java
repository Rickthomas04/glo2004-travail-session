package MasterCut.domain.dto;

import MasterCut.domain.Cnc;
import MasterCut.domain.utils.Dimensions;
import MasterCut.domain.utils.enumPackage.Unit;
import java.io.Serializable;
import java.util.List;

public class CncDTO implements Serializable {

    public Dimensions dimensions;
    public List<ToolDTO> tools;
    //private List<NoGoZone> NoGoZones;
    public PanelDTO panel;

    public CncDTO(Cnc cnc)
    {
        dimensions = new Dimensions(cnc.getDimensions());
        tools = cnc.getToolsDTO();
        panel = cnc.getPanelDTO();
    }
}
