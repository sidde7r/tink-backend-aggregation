package se.tink.backend.aggregation.agents.nxgen.fi.banks.alandsbanken.fetcher.entities;

import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.account.CreditCardAccount;
import se.tink.backend.core.Amount;

@JsonObject
public class AlandsBankenCard {

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
        return CreditCardAccount.builder(maskedCardNumber, Amount.inEUR(-currentTotalDebt), Amount.inEUR(usageLimit))
                .setName(maskedCardNumber)
                .setUniqueIdentifier(id)
                .setBankIdentifier(id)
                .build();
    }
}
