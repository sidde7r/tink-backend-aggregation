package se.tink.backend.aggregation.agents.nxgen.se.banks.volvofinans.fetcher.accounts.entities;

import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.account.SavingsAccount;

@JsonObject
public class SavingsAccountEntity {

    public SavingsAccount toTinkAccount() {
        return SavingsAccount.builder().build();
    }
}