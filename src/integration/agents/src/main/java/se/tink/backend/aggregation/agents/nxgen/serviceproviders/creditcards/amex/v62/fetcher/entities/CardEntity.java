package se.tink.backend.aggregation.agents.nxgen.serviceproviders.creditcards.amex.v62.fetcher.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.util.Optional;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.creditcards.amex.v62.AmericanExpressV62Configuration;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.account.CreditCardAccount;
import se.tink.libraries.strings.StringUtils;

@JsonObject
public class CardEntity {
    public static final String NOT_APPLICABLE = "n/a";
    public static final String NUMBER_REGEX = "[^0-9,.]";
    public static final double ZERO = 0d;
    private String cardProductName;
    private int sortedIndex;
    private String cardNumberDisplay;
    private FinancialTab financialTab;
    private Message message;
    private String canceled;

    public String getCanceled() {
        return canceled;
    }

    @JsonIgnore
    public CreditCardAccount toCreditCardAccount(AmericanExpressV62Configuration config) {

        // TODO confirm there is no credit for this account or find credit for this account
        // currently the credit is set to 0
        CreditCardAccount.Builder<?, ?> builder =
                CreditCardAccount.builder(
                                cardNumberDisplay,
                                config.toAmount(getBalanceValue()),
                                config.toAmount(ZERO))
                        .setAccountNumber(cardNumberDisplay)
                        .setName(
                                cardProductName
                                        + " - "
                                        + cardNumberDisplay.substring(
                                                cardNumberDisplay.length() - 5))
                        // card number display in format "xxxxxx - {last 5 digits in number}"
                        .setBankIdentifier(String.valueOf(sortedIndex));
        return builder.build();
    }

    @JsonIgnore
    public double getBalanceValue() {
        return Optional.ofNullable(financialTab)
                .map(ft -> ft.getTotalBalance())
                .map(b -> b.getValue())
                .filter(value -> !value.equalsIgnoreCase(NOT_APPLICABLE))
                .map(value -> StringUtils.parseAmount(value.replaceAll(NUMBER_REGEX, "")))
                .orElse(ZERO);
    }

    public String getCardProductName() {
        return cardProductName;
    }

    public int getSortedIndex() {
        return sortedIndex;
    }

    public String getCardNumberDisplay() {
        return cardNumberDisplay;
    }

    public Message getMessage() {
        return message;
    }
}
