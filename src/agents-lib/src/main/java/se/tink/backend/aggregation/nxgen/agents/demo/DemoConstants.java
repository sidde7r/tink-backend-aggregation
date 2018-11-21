package se.tink.backend.aggregation.nxgen.agents.demo;

import se.tink.backend.aggregation.nxgen.agents.demo.definitions.DemoInvestmentAccount;
import se.tink.backend.aggregation.nxgen.agents.demo.definitions.DemoLoanAccount;
import se.tink.backend.aggregation.nxgen.agents.demo.definitions.DemoSavingsAccount;
import se.tink.backend.aggregation.nxgen.agents.demo.definitions.DemoTransactionAccount;

public abstract class DemoConstants {

    public static String BASE_PATH = "data/demo";
    public static String GENERATION_BASE_FILE = "generationbase.json";

    public abstract DemoInvestmentAccount getInvestmentDefinitions();

    public abstract DemoSavingsAccount getDemoSavingsDefinitions();

    public abstract DemoLoanAccount getDemoLoanDefinitions();

    public abstract DemoTransactionAccount getTransactionalAccountDefinition();

    //Change to take amount and return the conversion
    public static double getSekToCurrencyConverter(String currency, double amountInSek) {
        double conversionAmount;
        switch (currency) {
        case "EUR" :
            conversionAmount = 10.22;
        case "USD" :
            conversionAmount = 9.06;
        case "GBP":
            conversionAmount = 11.77;
        default:
            conversionAmount = 1;
        }

        return amountInSek/conversionAmount;
    }
}
