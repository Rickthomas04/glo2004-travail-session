package MasterCut.domain.dto;

import MasterCut.domain.Border;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.awt.*;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

public class BorderDTO implements Serializable, LineDTO
{
    @JsonProperty
    public IntersectionDTO start;
    @JsonProperty
    public IntersectionDTO end;
    @JsonProperty
    public Color color;
    @JsonProperty
    public int thickness;
    @JsonProperty
    public boolean isHorizontal;
    @JsonProperty
	public boolean selectionStatus = false;
    @JsonProperty
	public UUID uuid;
	@JsonProperty
	private List<IntersectionDTO> intersections = new ArrayList<>();

    //For Deserialization
    public BorderDTO() {
    }

	public BorderDTO(Border border)
	{
		start = border.getStart().getIntersectionDTO();
		end = border.getEnd().getIntersectionDTO();
		color = border.getColor();
		thickness = border.getThickness();
		uuid = border.getUUID();
		isHorizontal = border.isHorizontal();
		selectionStatus = border.isSelected();
		intersections = border.getIntersectionsDTO();
	}

	@Override
	public boolean equals(Object o)
	{
		if (this == o)
			return true;
		if (o == null || getClass() != o.getClass())
			return false;
		BorderDTO borderDTO = (BorderDTO) o;
		return Objects.equals(start, borderDTO.start) && Objects.equals(end, borderDTO.end);
	}

	@Override
	public int hashCode()
	{
		return Objects.hash(start, end);
	}


	@Override
	public IntersectionDTO getStartDTO()
	{
		return start;
	}

	@Override
	public IntersectionDTO getEndDTO()
	{
		return end;
	}

	@Override
	public List<IntersectionDTO> getIntersectionsDTO()
	{
		return intersections;
	}
}
