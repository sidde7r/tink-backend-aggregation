package se.tink.backend.aggregation.agents.nxgen.fi.openbanking.akita.fetcher.transactionalaccount.entities;

import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.libraries.amount.Amount;

@JsonObject
public class BalanceAmountEntity {

    private String amount;
    private String currency;

    public Amount toAmount() {
        return new Amount(currency, amountToDouble());
    }

    private Double amountToDouble() {
        return Double.parseDouble(amount.replace(",", "."));
    }
}
