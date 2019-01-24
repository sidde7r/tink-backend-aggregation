package se.tink.backend.aggregation.agents.nxgen.serviceproviders.creditcards.amex.v45.authenticator.entities;

import se.tink.backend.aggregation.agents.nxgen.serviceproviders.creditcards.amex.v45.AmericanExpressConfiguration;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.account.CreditCardAccount;
import se.tink.libraries.strings.StringUtils;

@JsonObject
public class CardEntity {
    private String embossedName;
    private String homeCountryLocale;
    private String cardProductName;
    private int sortedIndex;
    private String cardKey;
    private String cardNumberDisplay;
    private SummaryEntity summary;
    private String accountToken;

    public CreditCardAccount toCreditCardAccount(AmericanExpressConfiguration config) {

        // TODO confirm there is no credit for this account or find credit for this account
        // currently the credit is set to 0

        CreditCardAccount.Builder<?, ?> builder = CreditCardAccount.builder(
                cardNumberDisplay,
                config.toAmount(getBalanceValue()),
                config.toAmount(0d))
                .setAccountNumber(cardNumberDisplay)
                .setName(
                        cardProductName
                                + " - "
                                + cardNumberDisplay.substring(cardNumberDisplay.length() - 5))
                // card number display in format "xxxxxx - {last 5 digits in number}"
                .setBankIdentifier(String.valueOf(sortedIndex));
        return builder.build();
    }

    public double getBalanceValue(){

        String value;
        try{
            value = summary.getTotalBalance().getValue();
        } catch(NullPointerException e){
            return 0d;
        }

        if (value.equalsIgnoreCase("n/a")) {
            return 0d;
        }
        return -StringUtils.parseAmount(value.replaceAll("[^0-9,.]", ""));

    }

    public double parseValueFromStringToDouble(String value) {
        if (value.equalsIgnoreCase("n/a")) {
            return 0d;
        }
        return StringUtils.parseAmount(value.replaceAll("[^0-9,.]", ""));
    }

    public String getEmbossedName() {
        return embossedName;
    }

    public String getHomeCountryLocale() {
        return homeCountryLocale;
    }

    public String getCardProductName() {
        return cardProductName;
    }

    public int getSortedIndex() {
        return sortedIndex;
    }

    public String getCardKey() {
        return cardKey;
    }

    public String getCardNumberDisplay() {
        return cardNumberDisplay;
    }

    public SummaryEntity getSummary() {
        return summary;
    }

    public String getAccountToken() {
        return accountToken;
    }
}
