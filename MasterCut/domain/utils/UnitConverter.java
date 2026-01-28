package MasterCut.domain.utils;

import MasterCut.domain.dto.DimensionsDTO;

public class UnitConverter
{
	private static final double INCH_TO_MM = 25.4f;
	private static final double MM_TO_INCH = 1 / INCH_TO_MM;

	public static double convertToMetric(double value)
	{
		return value * INCH_TO_MM;
	}

	public static Dimensions convertToMetric(Dimensions value)
	{
		value.setHeight(value.getHeight() * INCH_TO_MM);
		value.setWidth(value.getWidth() * INCH_TO_MM);
		return value;
	}

	public static DimensionsDTO convertToMetric(DimensionsDTO value)
	{
		value.height = value.getHeight() * INCH_TO_MM;
		value.width = value.getWidth() * INCH_TO_MM;
		return value;
	}

	public static double convertToImperial(double value)
	{
		return value * MM_TO_INCH;
	}

	public static Dimensions converToImperial(Dimensions value)
	{
		value.setHeight(value.getHeight() * MM_TO_INCH);
		value.setWidth(value.getWidth() * MM_TO_INCH);
		return value;
	}
}
