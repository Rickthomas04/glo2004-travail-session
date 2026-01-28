package MasterCut.domain.dto;

import MasterCut.domain.Panel;
import MasterCut.domain.dto.cuts.BaseCutDTO;
import MasterCut.domain.utils.Dimensions;
import MasterCut.domain.utils.enumPackage.Unit;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.io.Serializable;
import java.util.*;

public class PanelDTO implements Serializable {
    @JsonProperty
    public DimensionsDTO dimensions;
    @JsonProperty
    public List<BorderDTO> borders;
    @JsonProperty
    public Map<UUID, IntersectionDTO> intersections;
    @JsonProperty
    public Map<UUID, BaseCutDTO> cutsMap;
    @JsonProperty
    public double thickness;
    @JsonProperty
	public Unit usedUnit;

    //For Deserialization
    public PanelDTO() {
    }
	public PanelDTO(Panel panel)
	{
		dimensions = panel.getDimensionsDTO();
		borders = panel.getBordersDTO();
		intersections = panel.getIntersectionsDTO();
		cutsMap = panel.getBaseCutDTO();
		thickness = panel.getThickness();
		usedUnit = panel.getUsedUnit();
	}

	@Override
	public boolean equals(Object o)
	{
		if (this == o)
			return true;
		if (o == null || getClass() != o.getClass())
			return false;
		PanelDTO panelDTO = (PanelDTO) o;
		return Double.compare(thickness, panelDTO.thickness) == 0 && Objects.equals(dimensions, panelDTO.dimensions) && Objects.equals(borders, panelDTO.borders) && Objects.equals(intersections, panelDTO.intersections) && usedUnit == panelDTO.usedUnit;
	}

	@Override
	public int hashCode()
	{
		return Objects.hash(dimensions, borders, intersections, thickness, usedUnit);
	}
}
