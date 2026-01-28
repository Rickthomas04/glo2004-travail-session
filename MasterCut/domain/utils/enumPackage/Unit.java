package MasterCut.domain.utils.enumPackage;

public enum Unit {
	METRIC, IMPERIAL;

	@Override
	public String toString()
	{
		return switch (this)
		{
			case METRIC -> "mm";
			case IMPERIAL -> "in";
			default -> super.toString();
		};
	}
}
