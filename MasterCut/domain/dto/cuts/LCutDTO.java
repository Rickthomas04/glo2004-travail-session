package MasterCut.domain.dto.cuts;

import MasterCut.domain.cuts.LCut;

import java.util.List;

public class LCutDTO extends IrregularCutDTO
{
	public final RegularCutDTO horizontalCut;
	public final RegularCutDTO verticalCut;



	public LCutDTO(LCut dto)
	{
		super(dto);
		this.horizontalCut = dto.getHorizontalCutDTO();
		this.verticalCut = dto.getVerticalCutDTO();
		this.name = "LCut";
	}


	@Override
	public List<RegularCutDTO> getRegularCuts()
	{
		return List.of(horizontalCut, verticalCut);
	}
}
