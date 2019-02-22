package se.tink.backend.aggregation.agents.nxgen.es.banks.bbva.fetcher.creditcard.entities;

import se.tink.libraries.amount.Amount;

public class CreditCardEntityTestData {
    public static String UNIQUE_ID = "1111";
    public static String NAME = "Tarj. créd. *1111";
    private static Double balance = 111.11;
    public static Amount AMOUNT = new Amount("EUR", balance);
    public static String CREDIT_CARD_DATA =
            "{\n"
                    + "  \"id\": \"ES0111111100000000000000000111111111XXXXXXXXX\",\n"
                    + "  \"name\": \"" + NAME + "\",\n"
                    + "  \"productDescription\": \"PACK DUO BBVA - DESPUES\",\n"
                    + "  \"iban\": null,\n"
                    + "  \"productFamilyCode\": \"cards\",\n"
                    + "  \"subfamilyCode\": \"cards\",\n"
                    + "  \"subfamilyTypeCode\": \"credit\",\n"
                    + "  \"typeCode\": \"00029\",\n"
                    + "  \"typeDescription\": null,\n"
                    + "  \"familyCode\": \"credit\",\n"
                    + "  \"currency\": \"EUR\",\n"
                    + "  \"availableBalance\": \"" + String.valueOf(balance) + "\",\n"
                    + "  \"availableBalances\": null,\n"
                    + "  \"branch\": \"1111\",\n"
                    + "  \"accountProductId\": null,\n"
                    + "  \"accountProductDescription\": null,\n"
                    + "  \"actualBalanceInOriginalCurrency\": 0.0,\n"
                    + "  \"actualBalance\": 0.0\n"
                    + "}";
}
