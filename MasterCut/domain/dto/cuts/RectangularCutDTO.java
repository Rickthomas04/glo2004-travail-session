
package MasterCut.domain.dto.cuts;

import MasterCut.domain.Intersection;
import MasterCut.domain.cuts.RectangularCut;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.io.Serializable;
import java.util.List;

public class RectangularCutDTO extends IrregularCutDTO implements Serializable {

    @JsonProperty
    public final String type = "RectangularCutDTO";
    @JsonProperty
    public final RegularCutDTO horizontalCut1;
    @JsonProperty
    public final RegularCutDTO horizontalCut2;
    @JsonProperty
    public final RegularCutDTO verticalCut1;
    @JsonProperty
    public final RegularCutDTO verticalCut2;
    @JsonProperty
    public final Intersection point1;
    @JsonProperty
    public final Intersection point2;
    @JsonProperty 
    public final double width;  
    @JsonProperty
    public final double height;
    @JsonProperty
    public final double distanceFromReferenceY;
    @JsonProperty
    public final double distanceFromReferenceX;
   
   
    
    public RectangularCutDTO(RectangularCut cut)
    {
        super(cut);
        this.horizontalCut1 = cut.getHorizontalCut1().getRegularCutsDTO().get(0);
        this.horizontalCut2 = cut.getHorizontalCut2().getRegularCutsDTO().get(0);
        this.verticalCut1 = cut.getVerticalCut1().getRegularCutsDTO().get(0);
        this.verticalCut2 = cut.getVerticalCut2().getRegularCutsDTO().get(0);
        this.width = cut.getWidth();
        this.height = cut.getHeight();
        this.point1 = cut.getPoint1();
        this.point2 = cut.getPoint2();
        
        this.distanceFromReferenceY = cut.getDistanceFromReference().y;
        this.distanceFromReferenceX = cut.getDistanceFromReference().x;
        
      
        
        this.name = "RectangularCut";
    }

    @Override
    public List<RegularCutDTO> getRegularCuts()
    {
        return List.of(horizontalCut1, horizontalCut2, verticalCut1, verticalCut2);
    }
}
