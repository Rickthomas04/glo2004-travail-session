package MasterCut.domain.dto.cuts;
import MasterCut.domain.cuts.RegularCut;
import MasterCut.domain.dto.IntersectionDTO;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.awt.*;
import java.io.Serializable;
import java.util.List;

public class RegularCutDTO extends BaseCutDTO implements Serializable {

    @JsonProperty
    public final String type = "RegularCutDTO";
    @JsonProperty
    public int thickness;
    public boolean selectionStatus;
    @JsonProperty
    public Color color;
    @JsonProperty
    public final boolean isHorizontal;
    @JsonProperty
    public IntersectionDTO start;
    @JsonProperty
    public IntersectionDTO end;
    @JsonProperty
    public IrregularCutDTO parentCut = null;
    @JsonProperty
    public IntersectionDTO reference;

	//For Deserialization
    public RegularCutDTO() {
        this.isHorizontal = false;
    }

	public RegularCutDTO(RegularCut cut)
	{
		super(cut);
		this.start = cut.getStart().getIntersectionDTO();
		this.end = cut.getEnd().getIntersectionDTO();
		this.thickness = cut.getThickness();
		this.selectionStatus = cut.isSelected();
		this.color = cut.getDisplayColor();
		this.isHorizontal = cut.isHorizontal();
            this.name = "RegularCut";
            this.reference = cut.getReferenceDTO();
	}

	@Override
	public List<RegularCutDTO> getRegularCuts()
	{
		return List.of(this);
	}
}
