package MasterCut.domain;

import MasterCut.domain.dto.ToolDTO;
import MasterCut.domain.utils.enumPackage.Unit;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.awt.*;
import java.io.Serializable;
import java.util.UUID;
public class Tool implements Cloneable, Serializable {

    @JsonProperty
    private String name;
    @JsonProperty
    private double diameter;
    @JsonProperty
    private int rpm;
    @JsonProperty
    private double feedrate;
    @JsonProperty
    private final UUID uuid;
    @JsonProperty
    private Unit usedUnit;
    @JsonProperty
    private Color color;

    //For Deserialization
    public Tool() {
        this.uuid = UUID.randomUUID();
    }
    public Tool(String name, double diameter, int rpm, double feedrate, Unit usedUnit)
    {
        this.name = name;
        this.diameter = diameter;
        this.rpm = rpm;
        this.feedrate = feedrate;
        this.usedUnit = usedUnit;
        this.uuid = UUID.randomUUID();
        this.color = Color.ORANGE;
    }

    public Tool(ToolDTO toolDTO)
    {
        this.name = toolDTO.name;
        this.diameter = toolDTO.diameter;
        this.rpm = toolDTO.rpm;
        this.feedrate = toolDTO.feedrate;
        this.usedUnit = toolDTO.usedUnit;
        this.uuid = toolDTO.uuid;
    }

    public UUID getUuid()
    {
        return uuid;
    }

    public double getDiameter()
    {
        return diameter;
    }

    public void setDiameter(double diameter)
    {
        this.diameter = diameter;
    }

    public double getFeedrate()
    {
        return feedrate;
    }

    public void setFeedrate(double feedrate)
    {
        this.feedrate = feedrate;
    }

    public UUID getUUID()
    {
        return uuid;
    }

    public String getName()
    {
        return name;
    }

    public void setName(String name)
    {
        this.name = name;
    }

    public int getRpm()
    {
        return rpm;
    }

    public void setRpm(int rpm)
    {
        this.rpm = rpm;
    }

    public Unit getUsedUnit()
    {
        return usedUnit;
    }

    public void setUsedUnit(Unit usedUnit)
    {
        this.usedUnit = usedUnit;
    }

    public ToolDTO getToolDTO()
    {
        return new ToolDTO(this);
    }


    public Color getColor()
    {
        return color;
    }

    public void setColor(Color color)
    {
        this.color = color;
    }

    public void updateTool(ToolDTO toolDTO)
    {
        this.name = toolDTO.name;
        this.diameter = toolDTO.diameter;
        this.rpm = toolDTO.rpm;
        this.feedrate = toolDTO.feedrate;
        this.usedUnit = toolDTO.usedUnit;
    }

    @Override
            public Tool clone() {
                try {
                    Tool clonedTool = (Tool) super.clone();

                   
                    clonedTool.name = this.name;
                    clonedTool.diameter = this.diameter;
                    clonedTool.rpm = this.rpm;
                    clonedTool.feedrate = this.feedrate;
                    clonedTool.usedUnit = this.usedUnit;
                    clonedTool.color = this.color;

                    return clonedTool;

                } catch (CloneNotSupportedException e) {
                    throw new AssertionError("Clone not supported", e);
                }
            }
            @Override
            public String toString() {
                return "Tool{" +
                       "name='" + name + '\'' +
                       ", diameter=" + diameter +
                       ", rpm=" + rpm +
                       ", feedrate=" + feedrate +
                       ", uuid=" + uuid +
                       ", usedUnit=" + usedUnit +
                       ", color=" + color +
                       '}';
            }

}
