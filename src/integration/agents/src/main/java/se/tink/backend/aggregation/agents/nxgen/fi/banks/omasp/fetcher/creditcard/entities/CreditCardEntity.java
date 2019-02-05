package se.tink.backend.aggregation.agents.nxgen.fi.banks.omasp.fetcher.creditcard.entities;

import java.util.Objects;
import se.tink.backend.aggregation.agents.nxgen.fi.banks.omasp.entities.AmountEntity;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.account.CreditCardAccount;
import se.tink.libraries.amount.Amount;

@JsonObject
public class CreditCardEntity {
    private String id;
    // `iban` is null - cannot define it!
    private String cardNumber;
    private AmountEntity remainingCredit;
    private AmountEntity creditLimit;
    private String name;

    public String getId() {
        return id;
    }

    public boolean isCreditCard() {
        return Objects.nonNull(creditLimit) && creditLimit.getValue() > 0.0;
    }

    private Amount getBalance() {
        return Amount.inEUR(0.0 - creditLimit.getValue() - remainingCredit.getValue());
    }

    public CreditCardAccount toTinkAccount() {
        return CreditCardAccount.builder(id, getBalance(), creditLimit.toTinkAmount())
                .setAccountNumber(cardNumber)
                .setBankIdentifier(id)
                .setName(name)
                .build();
    }
}
