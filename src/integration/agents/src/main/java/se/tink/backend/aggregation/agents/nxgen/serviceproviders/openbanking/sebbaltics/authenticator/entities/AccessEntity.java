package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sebbaltics.authenticator.entities;

import java.util.List;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class AccessEntity {

    private List<AccountNumberEntity> accounts;
    private List<AccountNumberEntity> balances;
    private List<AccountNumberEntity> transactions;

    public AccessEntity(List<AccountNumberEntity> accountNumber) {
        this.accounts = accountNumber;
        this.balances = accountNumber;
        this.transactions = accountNumber;
    }
}
