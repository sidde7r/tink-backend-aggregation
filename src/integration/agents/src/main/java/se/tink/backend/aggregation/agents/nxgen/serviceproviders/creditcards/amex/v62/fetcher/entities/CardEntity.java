package se.tink.backend.aggregation.agents.nxgen.serviceproviders.creditcards.amex.v62.fetcher.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.math.BigDecimal;
import java.util.Optional;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.creditcards.amex.v62.AmericanExpressV62Configuration;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.creditcards.amex.v62.utils.AmericanExpressV62Utils;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.account.creditcard.CreditCardAccount;
import se.tink.backend.aggregation.nxgen.core.account.entity.HolderName;
import se.tink.libraries.amount.ExactCurrencyAmount;

@JsonObject
public class CardEntity {
    private String cardProductName;
    private int sortedIndex;
    private String cardNumberDisplay;
    private FinancialTab financialTab;
    private Message message;
    private String canceled;
    private String nameOnCard;

    public String getCanceled() {
        return canceled;
    }

    @JsonIgnore
    public CreditCardAccount toCreditCardAccount(AmericanExpressV62Configuration config) {
        return CreditCardAccount.builder(
                        cardNumberDisplay,
                        ExactCurrencyAmount.of(getTotalBalance(), config.getCurrency()).negate(),
                        ExactCurrencyAmount.of(getAvailableCredit(), config.getCurrency()).negate())
                .setAccountNumber(cardNumberDisplay)
                .setName(
                        cardProductName
                                + " - "
                                + cardNumberDisplay.substring(cardNumberDisplay.length() - 5))
                // card number display in format "xxxxxx - {last 5 digits in number}"
                .setHolderName(new HolderName(nameOnCard))
                .setBankIdentifier(String.valueOf(sortedIndex))
                .build();
    }

    @JsonIgnore
    private BigDecimal getTotalBalance() {
        return Optional.ofNullable(financialTab)
                .map(FinancialTab::getTotalBalance)
                .map(TotalBalance::getValue)
                .filter(AmericanExpressV62Utils::isValidAmount)
                .map(AmericanExpressV62Utils::parseAmountToBigDecimal)
                .orElse(BigDecimal.ZERO);
    }

    @JsonIgnore
    private BigDecimal getAvailableCredit() {
        return Optional.ofNullable(financialTab)
                .map(FinancialTab::getAvailableCredit)
                .map(AvailableCredit::getValue)
                .filter(AmericanExpressV62Utils::isValidAmount)
                .map(AmericanExpressV62Utils::parseAmountToBigDecimal)
                .orElse(BigDecimal.ZERO);
    }

    public String getCardNumberDisplay() {
        return cardNumberDisplay;
    }

    public Message getMessage() {
        return message;
    }
}
