
package MasterCut.domain.cuts;
import MasterCut.domain.Intersection;
import MasterCut.domain.dto.cuts.BaseCutDTO;
import MasterCut.domain.dto.cuts.ForbiddenZoneDTO;
import static MasterCut.domain.utils.Message.sendMessage;
import MasterCut.domain.utils.enumPackage.CutType;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.awt.geom.Point2D;
import java.io.Serializable;

public class ForbiddenZone extends RectangularCut implements Serializable{
    
    @JsonProperty
    public final String type = "ForbiddenZone"; 

    //For deserialization
    public ForbiddenZone() {
    }

    public ForbiddenZone(Intersection point1, Intersection point2, Intersection oppositePoint1, Intersection oppositePoint2)
    {
        super(null, 0, new Intersection (new Point2D.Double(0,0)), point1,  point2,  oppositePoint1,  oppositePoint2,  null);

        for(RegularCut regularCut : getRegularCuts())
            regularCut.setCutType(CutType.FORBIDDEN_ZONE);

        sendMessage("Forbidden Zone Created");
    }

    @Override
    public Object getDTO()
    {
        return getBaseCutDTO();
    }

    @Override
    public BaseCutDTO getBaseCutDTO()
    {
        return new ForbiddenZoneDTO(this);
    }
}
