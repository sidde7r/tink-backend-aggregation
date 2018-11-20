package se.tink.backend.aggregation.nxgen.agents.demo;

public class NextGenDemoConstants {

    public static String BASE_PATH = "data/demo";

    public static String ACCOUNT_FILE = "accounts.txt";

    public static String GENERATION_BASE_FILE = "generationbase.json";

    //Change to take amount and return the conversion
    public static double getSekToCurrencyConverter(String currency) {
        switch (currency) {
        case "EUR" :
            return 10.22;
        case "USD" :
            return 9.06;
        case "GBP":
            return 11.77;
        default:
            return 1;
        }
    }

}
