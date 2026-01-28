package MasterCut.domain.dto;

import MasterCut.domain.utils.Dimensions;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.io.Serializable;

public class DimensionsDTO implements Serializable {
    @JsonProperty
    public double height;
    @JsonProperty
    public double width;

    public DimensionsDTO(Dimensions dimensions) {
        height = dimensions.getHeight();
        width = dimensions.getWidth();
    }

    public DimensionsDTO(double width, double height)
    {
        this.height = height;
        this.width = width;
    }

    //For Deserialization
    public DimensionsDTO() {
    }

    public double getHeight() {
        return height;
    }

    public double getWidth() {
        return width;
    }
}
