package se.tink.backend.aggregation.nxgen.agents.demo;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Arrays;
import java.util.List;

public class DemoConstants {

    public static final String BASE_PATH = "data/demo";
    public static final String GENERATION_BASE_FILE = "generationbase.json";

    // Change to take amount and return the conversion
    public static double getSekToCurrencyConverter(String currency, double amountInSek) {
        double conversionAmount;
        switch (currency) {
            case "EUR":
                conversionAmount = 10.22d;
                break;
            case "USD":
                conversionAmount = 9.06d;
                break;
            case "GBP":
                conversionAmount = 11.77d;
                break;
            default:
                conversionAmount = 1d;
        }

        return roundDecimal(amountInSek / conversionAmount);
    }

    // always return 2 decimals
    private static double roundDecimal(double number) {
        BigDecimal bd = BigDecimal.valueOf(number);
        bd = bd.setScale(2, RoundingMode.HALF_UP);
        return bd.doubleValue();
    }

    static class MarketCodes {
        static final List<Integer> UK_SORT_CODES = Arrays.asList(218353);
    }

    public static class MarketRegex {
        public static final String UK_PROVIDERS_REGEX = "^(?:uk|gb)-.*";
        static final String IT_PROVIDERS_REGEX = "^it-.*";
        static final String FR_PROVIDERS_REGEX = "^fr-.*";
        static final String NO_PROVIDERS_REGEX = "^no-.*";
    }
}
