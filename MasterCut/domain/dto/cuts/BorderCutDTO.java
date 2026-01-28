
package MasterCut.domain.dto.cuts;

import MasterCut.domain.cuts.BorderCut;
import MasterCut.domain.dto.IntersectionDTO;
import MasterCut.domain.utils.Dimensions;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;


public class BorderCutDTO extends BaseCutDTO implements Serializable {

    @JsonProperty
    public final String type = "BorderCutDTO";
    @JsonProperty
    private final Dimensions newDimensions;
    @JsonProperty
    public final IntersectionDTO intersection1;
    @JsonProperty
    public final IntersectionDTO intersection2;
    @JsonProperty
    public final IntersectionDTO intersection3;
    @JsonProperty
    public final IntersectionDTO intersection4;
    @JsonProperty
    public final List<RegularCutDTO> cuts;


    //For Deserialization
    public BorderCutDTO() {
        this.newDimensions = null;
        this.intersection1 = null;
        this.intersection2 = null;
        this.intersection3 = null;
        this.intersection4 = null;
        this.cuts = new ArrayList<>();
    }

    public BorderCutDTO(BorderCut cut) {
        super(cut);
        this.newDimensions = cut.getNewDimensions();
        this.intersection1 = cut.getIntersectionsDTO().get(0);
        this.intersection2 = cut.getIntersectionsDTO().get(1);
        this.intersection3 = cut.getIntersectionsDTO().get(2);
        this.intersection4 = cut.getIntersectionsDTO().get(3);
        this.cuts = cut.getRegularCutsDTO();
    }
    
    public Dimensions getNewDimensions()    {
        return newDimensions;
    }

    @Override
    public List<RegularCutDTO> getRegularCuts()
    {
        return cuts;
    }
}
