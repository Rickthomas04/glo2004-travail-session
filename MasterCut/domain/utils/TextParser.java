package MasterCut.domain.utils;

import MasterCut.domain.utils.enumPackage.Unit;

public class TextParser {

    public static class Result {
        private final double valueInMm;
        private final Unit unit;

        public Result(double valueInMm, Unit unit) {
            this.valueInMm = valueInMm;
            this.unit = unit;
        }

        public double getValueInMm() {
            return valueInMm;
        }

        public Unit getUnit() {
            return unit;
        }
    }
    public static int extractCutIndex(String nodeText) {
        if (nodeText.matches("^\\d+\\.\\s.*")) {
            return Integer.parseInt(nodeText.split("\\.")[0]) - 1;
        }
        return -1;
    }
    
    public static Result parseInputWithUnits(String input, Unit defaultUnit) throws NumberFormatException {
        input = input.trim().toLowerCase();
        double valueInMm;
        Unit detectedUnit = defaultUnit;

        // Supprimer les espaces entre le nombre et l'unité
        input = input.replaceAll("\\s+(in|inch|ft|foot|m|metre|meter|mm)\\b", "$1");

        if (input.endsWith("in") || input.endsWith("inch")) {
            input = input.replaceAll("(in|inch)$", "").trim();
            double inches = parseFractionalInput(input);
            valueInMm = UnitConverter.convertToMetric(inches);
            detectedUnit = Unit.IMPERIAL;
        } else if (input.endsWith("ft") || input.endsWith("foot")) {
            input = input.replaceAll("(ft|foot)$", "").trim();
            double feet = parseFractionalInput(input);
            valueInMm = UnitConverter.convertToMetric(feet * 12);
            detectedUnit = Unit.IMPERIAL;
        } else if (input.endsWith("m") || input.endsWith("metre") || input.endsWith("meter")) {
            input = input.replaceAll("(m|metre|meter)$", "").trim();
            double meters = parseFractionalInput(input);
            valueInMm = meters * 1000;
            detectedUnit = Unit.METRIC;
        } else if (input.endsWith("mm")) {
            input = input.replaceAll("mm$", "").trim();
            valueInMm = parseFractionalInput(input);
            detectedUnit = Unit.METRIC;
        } else {
            double value = parseFractionalInput(input);
            if (defaultUnit == Unit.IMPERIAL) {
                valueInMm = UnitConverter.convertToMetric(value);
                detectedUnit = Unit.IMPERIAL;
            } else {
                valueInMm = value;
                detectedUnit = Unit.METRIC;
            }
        }

        if (valueInMm == 0) {
            throw new NumberFormatException("La valeur ne peut pas être zéro.");
        }

        return new Result(valueInMm, detectedUnit);
    }

    private static double parseFractionalInput(String input) throws NumberFormatException {
        String[] parts = input.split(" ");
        double result = 0.0;

        if (parts.length == 2) {
            result = Double.parseDouble(parts[0]) + parseFraction(parts[1]);
        } else if (parts.length == 1) {
            if (parts[0].contains("/")) {
                result = parseFraction(parts[0]);
            } else {
                result = Double.parseDouble(parts[0]);
            }
        } else {
            throw new NumberFormatException("Invalid input format. Ensure correct spacing and format.");
        }
        return result;
    }

    private static double parseFraction(String fraction) throws NumberFormatException {
        try {
            String[] parts = fraction.split("/");
            if (parts.length != 2) {
                throw new NumberFormatException("Invalid fraction format. Use format: numerator/denominator");
            }
            
            double numerator = Double.parseDouble(parts[0]);
            double denominator = Double.parseDouble(parts[1]);
            
            if (denominator == 0) {
                throw new NumberFormatException("Denominator cannot be zero");
            }
            
            return numerator / denominator;
        } catch (NumberFormatException e) {
            throw new NumberFormatException("Invalid fraction format. Use format: numerator/denominator");
        }
    }
}