package se.tink.backend.aggregation.nxgen.agents.demo;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class DemoConstants {

    public static String BASE_PATH = "data/demo";
    public static String GENERATION_BASE_FILE = "generationbase.json";

    // Change to take amount and return the conversion
    public static double getSekToCurrencyConverter(String currency, double amountInSek) {
        double conversionAmount;
        switch (currency) {
            case "EUR":
                conversionAmount = 10.22;
            case "USD":
                conversionAmount = 9.06;
            case "GBP":
                conversionAmount = 11.77;
            default:
                conversionAmount = 1;
        }

        return roundDecimal(amountInSek / conversionAmount);
    }

    // always return 2 decimals
    private static double roundDecimal(double number) {
        BigDecimal bd = new BigDecimal(number);
        bd = bd.setScale(2, RoundingMode.HALF_UP);
        return bd.doubleValue();
    }
}
