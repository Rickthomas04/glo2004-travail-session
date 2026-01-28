package MasterCut.domain.cuts;

import MasterCut.domain.Intersection;
import MasterCut.domain.Line;
import MasterCut.domain.Tool;
import MasterCut.domain.utils.Clickable;
import MasterCut.domain.utils.enumPackage.CutType;
import java.util.*;

public abstract class IrregularCut extends BaseCut implements Cloneable, Clickable
{

	public IrregularCut(Tool tool, double depth, Intersection reference, CutType cutType, BaseCut parent)
	{
		super(depth, reference, tool, cutType, parent);
	}

	public IrregularCut clone(Map<Intersection, Intersection> intersectionClones, Map<BaseCut, BaseCut> baseCutClones, Map<Line, Line> lineClones) throws CloneNotSupportedException
	{

		if (baseCutClones.containsKey(this))
		{
			return (IrregularCut) baseCutClones.get(this);
		}


		IrregularCut cloned = (IrregularCut) super.clone(intersectionClones, baseCutClones, lineClones);


		baseCutClones.put(this, cloned);


		cloned.cuts = new ArrayList<>();
		for (RegularCut regularCut : this.cuts)
		{
			RegularCut clonedRegularCut = (RegularCut) lineClones.get(regularCut);
			if (clonedRegularCut == null)
			{
				clonedRegularCut = regularCut.clone(intersectionClones, baseCutClones, lineClones);
				lineClones.put(regularCut, clonedRegularCut);
			}
			cloned.cuts.add(clonedRegularCut);
		}


		if (this.intersections != null)
		{
			cloned.intersections = new ArrayList<>();
			for (Intersection intersection : this.intersections)
			{
				Intersection clonedIntersection = intersectionClones.get(intersection);
				if (clonedIntersection == null)
				{
					clonedIntersection = intersection.clone(intersectionClones, baseCutClones, lineClones);
					intersectionClones.put(intersection, clonedIntersection);
				}
				cloned.intersections.add(clonedIntersection);
			}
		}


		cloned.children.clear();
		for (BaseCut child : this.children)
		{
			BaseCut clonedChild = baseCutClones.get(child);
			if (clonedChild == null)
			{
				clonedChild = child.clone(intersectionClones, baseCutClones, lineClones);
				baseCutClones.put(child, clonedChild);
			}
			cloned.children.add(clonedChild);
		}


		cloned.tool = this.tool != null ? this.tool.clone() : null;


		cloned.reference = intersectionClones.get(this.reference);
		if (cloned.reference == null && this.reference != null)
		{
			cloned.reference = this.reference.clone(intersectionClones, baseCutClones, lineClones);
			intersectionClones.put(this.reference, cloned.reference);
		}


		return cloned;
	}
        
        public void addOffset() {
            
        }
        
        public void removeOffset() {
            
        }
}
