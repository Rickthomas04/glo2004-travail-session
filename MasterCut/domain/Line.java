package MasterCut.domain;

import MasterCut.domain.cuts.BaseCut;
import MasterCut.domain.cuts.RegularCut;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonSubTypes.Type;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonTypeInfo.Id;
import java.awt.*;
import java.awt.geom.Point2D;
import java.io.Serializable;
import java.util.List;
import java.util.Map;

@JsonTypeInfo(use = Id.CLASS)
@JsonSubTypes({
    @Type(RegularCut.class),
    @Type(Border.class)})
public interface Line extends Cloneable, Serializable
{
	Tool getTool();

	double getLenght();

	void setDisplayColor(Color color);

	void addIntersection(Intersection intersection);

	void removeIntersection(Intersection intersection);

	Intersection getStart();

	void setStart(Intersection start);

	Intersection getEnd();

	void setEnd(Intersection end);

	List<Intersection> getIntersections();

	void setIntersections(List<Intersection> intersections);

	boolean isHorizontal();

	void addChild(BaseCut baseCut);

	void removeChild(BaseCut baseCut);

	BaseCut getParentCut();

	Line clone(Map<Intersection, Intersection> intersectionClones, Map<BaseCut, BaseCut> baseCutClones, Map<Line, Line> lineClones) throws CloneNotSupportedException;

	void removeOffset();

	void addOffset();

	boolean containsPoint(Point2D.Double point, double tolerance);

}

