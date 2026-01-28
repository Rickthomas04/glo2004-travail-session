package MasterCut.domain.utils;

import MasterCut.domain.dto.DimensionsDTO;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.io.Serializable;

public class Dimensions implements Serializable, Cloneable {

    @JsonProperty
    private double width;
    @JsonProperty
	private double height;

    //For Deserialization
    public Dimensions() {
    }
	public Dimensions(double width, double height)
	{
		setSize(width, height);
	}

	public Dimensions(Dimensions dimensions)
	{
		setSize(dimensions.getWidth(), dimensions.getHeight());
	}

	public Dimensions(DimensionsDTO dimensions){
		setSize(dimensions.width, dimensions.height);
	}


	public void setSize(double width, double height)
	{
		this.width = width;
		this.height = height;
	}

	public double getWidth()
	{
		return width;
	}

	public void setWidth(double width)
	{
		this.width = width;
	}

	public double getHeight()
	{
		return height;
	}

	public void setHeight(double height)
	{
		this.height = height;
	}
        @Override
        public Dimensions clone() {
            try {
                return (Dimensions) super.clone(); 
            } catch (CloneNotSupportedException e) {
                throw new AssertionError("Cloning failed for Dimensions", e); 
            }
        }
}
