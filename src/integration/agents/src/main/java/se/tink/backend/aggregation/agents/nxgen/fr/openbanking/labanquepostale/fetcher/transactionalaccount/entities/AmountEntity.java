package se.tink.backend.aggregation.agents.nxgen.fr.openbanking.labanquepostale.fetcher.transactionalaccount.entities;

import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.labanquepostale.LaBanquePostaleConstants.CreditDebitIndicators;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.libraries.amount.ExactCurrencyAmount;

@JsonObject
public class AmountEntity {
    private String currency;
    private String amount;

    public ExactCurrencyAmount toAmount(String creditDebitIndicator) {
        if (creditDebitIndicator.equals(CreditDebitIndicators.DEBIT)) {
            return ExactCurrencyAmount.of(Double.parseDouble(amount), currency).negate();
        }
        return ExactCurrencyAmount.of(Double.parseDouble(amount), currency);
    }

    public String getCurrency() {
        return currency;
    }
}
