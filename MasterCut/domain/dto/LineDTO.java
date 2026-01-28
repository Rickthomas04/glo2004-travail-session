package MasterCut.domain.dto;

import java.io.Serializable;
import java.util.List;

public interface LineDTO extends Serializable {
	IntersectionDTO getStartDTO();
	IntersectionDTO getEndDTO();
	List<IntersectionDTO> getIntersectionsDTO();
}
