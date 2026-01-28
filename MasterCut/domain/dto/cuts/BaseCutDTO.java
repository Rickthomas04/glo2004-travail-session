package MasterCut.domain.dto.cuts;

import MasterCut.domain.cuts.BaseCut;
import MasterCut.domain.dto.ToolDTO;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

import java.util.List;
import java.util.UUID;


@JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,
        include = JsonTypeInfo.As.PROPERTY,
        property = "type",
        visible = true
)
@JsonSubTypes({
    @JsonSubTypes.Type(value = RegularCutDTO.class, name = "RegularCutDTO"),
    @JsonSubTypes.Type(value = RectangularCutDTO.class, name = "RectangularCutDTO"),
    @JsonSubTypes.Type(value = BorderCutDTO.class, name = "BorderCutDTO"),
    @JsonSubTypes.Type(value = LCutDTO.class, name = "LCutDTO"),
    @JsonSubTypes.Type(value = ForbiddenZoneDTO.class, name = "ForbiddenZoneDTO")
})
public abstract class BaseCutDTO
{

	@JsonProperty
	public double depth;
	@JsonProperty
	public final UUID uuid;
	@JsonProperty
	public ToolDTO tool;
	@JsonProperty
	public String name;
	@JsonProperty
	public Boolean valid;


	public BaseCutDTO(BaseCut baseCutDTO)
	{
		this.name = "BaseCut";
                this.depth = baseCutDTO.getDepth();
                
		if (baseCutDTO.getTool()!= null)   
                    this.tool = baseCutDTO.getTool().getToolDTO();
                else
                    this.tool = null;
                
                
                
                
		this.uuid = baseCutDTO.getUUID();
                this.valid = baseCutDTO.isValid();
	}

	abstract public List<RegularCutDTO> getRegularCuts();


	//For Deserialization
	public BaseCutDTO()
	{
		this.uuid = null;
	}

}

