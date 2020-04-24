package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.crosskey.fetcher.entities;

import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.account.creditcard.CreditCardAccount;
import se.tink.libraries.amount.ExactCurrencyAmount;

@JsonObject
public class CrossKeyCard {

    private String id;

    private boolean creditCard;

    private String maskedCardNumber;

    private double usageLimit;

    private double currentTotalDebt;

    public String getId() {
        return id;
    }

    public boolean isCreditCard() {
        return creditCard;
    }

    public CreditCardAccount toCreditCardAccount() {
        return CreditCardAccount.builder(
                        id,
                        ExactCurrencyAmount.inEUR(-currentTotalDebt),
                        ExactCurrencyAmount.inEUR(usageLimit))
                .setAccountNumber(maskedCardNumber)
                .setName(maskedCardNumber)
                .setBankIdentifier(id)
                .build();
    }
}
