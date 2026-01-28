package MasterCut.domain.dto;

import MasterCut.domain.Tool;
import MasterCut.domain.utils.enumPackage.Unit;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.awt.*;
import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;

public class ToolDTO implements Serializable {
    @JsonProperty
    public String name;
    @JsonProperty
    public double diameter;
    @JsonProperty
    public int rpm;
    @JsonProperty
    public double feedrate;
    @JsonProperty
    public UUID uuid;
    @JsonProperty
    public Unit usedUnit;
    @JsonProperty
	public Color color;

	public ToolDTO()
	{
		this.uuid = UUID.randomUUID();
		this.usedUnit = null;
		this.name = null;
		this.diameter = 0;
		this.rpm = 0;
		this.feedrate = 0;
		this.color = Color.ORANGE;
	}

	public ToolDTO(Tool tool)
	{
		this.name = tool.getName();
		this.diameter = tool.getDiameter();
		this.rpm = tool.getRpm();
		this.feedrate = tool.getFeedrate();
		this.uuid = tool.getUUID();
		this.usedUnit = tool.getUsedUnit();
		this.color = tool.getColor();
	}

	@Override
	public boolean equals(Object o)
	{
		if (this == o)
			return true;
		if (o == null || getClass() != o.getClass())
			return false;
		ToolDTO toolDTO = (ToolDTO) o;
		return Objects.equals(uuid, toolDTO.uuid);
	}

	@Override
	public int hashCode()
	{
		return Objects.hashCode(uuid);
	}

	@Override
	public String toString()
	{
		return name;
	}


}
