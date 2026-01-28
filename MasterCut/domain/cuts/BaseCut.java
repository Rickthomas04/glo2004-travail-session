package MasterCut.domain.cuts;

import MasterCut.domain.Intersection;
import MasterCut.domain.Line;
import MasterCut.domain.Tool;
import MasterCut.domain.dto.DimensionsDTO;
import MasterCut.domain.dto.IntersectionDTO;
import MasterCut.domain.dto.cuts.BaseCutDTO;
import MasterCut.domain.dto.cuts.RegularCutDTO;
import MasterCut.domain.utils.Clickable;
import MasterCut.domain.utils.enumPackage.CutType;
import static MasterCut.domain.utils.enumPackage.CutType.BORDER;
import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonSubTypes.Type;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonTypeInfo.Id;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;

import java.awt.*;
import java.awt.geom.Point2D;
import java.io.Serializable;
import java.util.*;
import java.util.List;

/*@JsonIdentityInfo(
        generator = ObjectIdGenerators.PropertyGenerator.class,
        property = "uuid")*/
@JsonTypeInfo(
        use = Id.CLASS
)
@JsonSubTypes({
    @Type(RegularCut.class),
    @Type(RectangularCut.class),
    @Type(BorderCut.class),
    @Type(LCut.class), //@JsonSubTypes.Type(value = RegularCut.class),
//@JsonSubTypes.Type(value = RectangularCut.class),
//@JsonSubTypes.Type(value = BorderCut.class),
//@JsonSubTypes.Type(value = LCut.class)
})
public abstract class BaseCut implements Cloneable, Clickable, Serializable {
    @JsonProperty
    protected double depth;
    @JsonProperty
    protected Intersection reference;
    @JsonIdentityInfo(
            generator = ObjectIdGenerators.PropertyGenerator.class,
            property = "uuid")
    @JsonProperty
    protected BaseCut parent;
    @JsonProperty
    protected List<RegularCut> cuts = new ArrayList<>();
    @JsonProperty
    protected List<Intersection> intersections = new ArrayList<>();
    @JsonProperty
    @JsonIdentityInfo(
            generator = ObjectIdGenerators.PropertyGenerator.class,
            property = "uuid")
    protected final Set<BaseCut> children = new HashSet<>();
    @JsonProperty
    protected final UUID uuid;
    /*@JsonIdentityInfo(
            generator = ObjectIdGenerators.PropertyGenerator.class,
            property = "uuid",
            scope = Tool.class
    )*/
    @JsonProperty
    protected Tool tool;
    @JsonProperty
    protected Boolean valid = true;
    @JsonProperty
	protected CutType cutType;

    public BaseCut() {
        this.uuid = null;
        this.cutType = null;
    }

	public BaseCut(double depth, Intersection reference, Tool tool, CutType cutType, BaseCut parent)
	{
		this.depth = depth;
		this.reference = reference;
		this.tool = tool;
		this.uuid = UUID.randomUUID();
		this.cutType = cutType;
		this.parent = parent;
	}

	public abstract List<RegularCutDTO> getRegularCutsDTO();

	public abstract BaseCutDTO getBaseCutDTO();

	public abstract List<RegularCut> getRegularCuts();

	public abstract Point2D.Double getDistanceFromReference();

	public abstract void editCutDistance(Point2D.Double point);

	public abstract void setTool(Tool tool);

	public abstract void changeReference(Intersection intersection);

	public abstract DimensionsDTO getDimensionOfCut();

	public void setCutType(CutType cutType)
	{
		this.cutType = cutType;
	}

	public double getDepth()
	{
		return depth;
	}

	public void setDepth(double depth)
	{
		this.depth = depth;
	}

	public Intersection getReference()
	{
		return reference;
	}

	public void setReference(Intersection reference)
	{
		this.reference = reference;
	}

	public IntersectionDTO getReferenceDTO()
    {
        if (cutType.equals(BORDER)) {
            return new IntersectionDTO(new Intersection(new Point2D.Double(0, 0)));
        }
		return reference.getIntersectionDTO();
	}

	public Tool getTool()
	{
		return tool;
	}


	public CutType getCutType()
	{
		return cutType;
	}

	public Boolean isValid()
	{
		return valid;
	}

	public void setValid(Boolean valid)
	{
		this.valid = valid;

		if(valid)
			for(RegularCut cut : getRegularCuts())
			{
				cut.setToolColor(Color.ORANGE);
				cut.setDisplayColor(Color.ORANGE);
			}
		else
			for(RegularCut cut : getRegularCuts())
			{
				cut.setToolColor(Color.MAGENTA);
				cut.setDisplayColor(Color.MAGENTA);
			}
	}

	public UUID getUUID()
	{
		return uuid;
	}

	public Set<BaseCut> getChildren()
	{
		return children;
	}

	public List<RegularCut> getCuts()
	{
		return getRegularCuts();
	}

	public List<IntersectionDTO> getIntersectionsDTO()
	{
		List<IntersectionDTO> intersectionsDTO = new ArrayList<>();
		for (Intersection intersection : intersections)
		{
			intersectionsDTO.add(intersection.getIntersectionDTO());
		}
		return intersectionsDTO;
	}

	public void editCutDepth(double newDepth)
	{
		setDepth(newDepth);
	}

	public BaseCut getParentCut()
	{
		return parent;
	}

	public List<Intersection> getIntersections()
	{
		return intersections;
	}

	public BaseCut clone(Map<Intersection, Intersection> intersectionClones, Map<BaseCut, BaseCut> baseCutClones, Map<Line, Line> lineClones) throws CloneNotSupportedException
	{
		if (baseCutClones.containsKey(this))
		{
			return baseCutClones.get(this);
		}

		BaseCut cloned = (BaseCut) super.clone();

		baseCutClones.put(this, cloned);

		cloned.reference = intersectionClones.get(this.reference);

		cloned.cuts = new ArrayList<>();
		for (RegularCut regularCut : this.cuts)
		{
			RegularCut clonedRegularCut = regularCut.clone(intersectionClones, baseCutClones, lineClones);
			cloned.cuts.add(clonedRegularCut);
		}

		cloned.intersections = new ArrayList<>();
		for (Intersection intersection : this.intersections)
		{
			cloned.intersections.add(intersectionClones.get(intersection));
		}

		cloned.children.clear();
		for (BaseCut child : this.children)
		{
			BaseCut clonedChild = child.clone(intersectionClones, baseCutClones, lineClones);
			cloned.children.add(clonedChild);
		}

		cloned.tool = this.tool != null ? this.tool.clone() : null;

		return cloned;
	}

	public void updateTool(Tool tool)
	{
		this.setTool(tool);
	}

	public void addChild(BaseCut child)
	{
		this.children.add(child);
	}

	public void removeChild(BaseCut child)
	{
		this.children.remove(child);
	}

	protected Set<BaseCut> getAllChilds()
	{
		Set<BaseCut> allChilds = new HashSet<>();

		for (BaseCut child : this.children)
		{
			allChilds.add(child);
			allChilds.addAll(child.getAllChilds());
		}

		return allChilds;
	}

	public BaseCut getParent()
	{
		return parent;
	}

	public void setParent(BaseCut cut)
	{
		parent = cut;
	}


}
