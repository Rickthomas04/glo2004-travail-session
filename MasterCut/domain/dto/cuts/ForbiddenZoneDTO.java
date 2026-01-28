
package MasterCut.domain.dto.cuts;

import MasterCut.domain.Intersection;
import MasterCut.domain.cuts.ForbiddenZone;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.io.Serializable;
import java.util.List;


public class ForbiddenZoneDTO extends RectangularCutDTO implements Serializable{
    
    @JsonProperty
    public final String type = "ForbiddenZoneDTO";
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
    
    public ForbiddenZoneDTO(ForbiddenZone forbiddenZone)
    {
        super(forbiddenZone);       
        this.width = forbiddenZone.getWidth();
        this.height = forbiddenZone.getHeight();
        this.point1 = forbiddenZone.getPoint1();
        this.point2 = forbiddenZone.getPoint2();
        
        this.distanceFromReferenceY = forbiddenZone.getDistanceFromReferenceY();
        this.distanceFromReferenceX = forbiddenZone.getDistanceFromReferenceX();

        this.name = "ForbiddenZone";
    }
    
}
