package MasterCut.domain.dto.cuts;

import MasterCut.domain.cuts.IrregularCut;
import java.io.Serializable;

public abstract class IrregularCutDTO extends BaseCutDTO implements Serializable {

	public IrregularCutDTO(IrregularCut cut)
	{
		super(cut);
	}
}
