package MasterCut.domain.utils;

import java.awt.geom.Point2D;
import java.util.UUID;

public interface Clickable
{
	boolean isPointOnFeature(Point2D.Double point);
	void handleDrag(Point2D.Double dragPoint, Dimensions dimensions);
	Clickable onClick();
	void setSelected(boolean selectionStatus);

	UUID getUUID();
	Object getDTO();
}
