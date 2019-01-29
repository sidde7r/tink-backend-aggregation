package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.n26.fetcher.entities;

import com.google.common.base.Strings;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;
import se.tink.backend.aggregation.nxgen.core.account.transactional.SavingsAccount;
import se.tink.libraries.amount.Amount;

@JsonObject
public class SavingsAccountEntity {
    private String id;
    private String name;
    private double balance;

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public double getBalance() {
        return balance;
    }

    public Amount getAmount() {
        return Amount.inEUR(balance);
    }

    public boolean isValid() {
        return !Strings.isNullOrEmpty(id) &&
                !Strings.isNullOrEmpty(name) &&
                balance > 0;
    }

    public TransactionalAccount toSavingsAccount() {
        return SavingsAccount
                .builder(getId(), getAmount())
                .setAccountNumber(getId())
                .setName(getName())
                .setBankIdentifier(getId())
                .build();
    }
}
