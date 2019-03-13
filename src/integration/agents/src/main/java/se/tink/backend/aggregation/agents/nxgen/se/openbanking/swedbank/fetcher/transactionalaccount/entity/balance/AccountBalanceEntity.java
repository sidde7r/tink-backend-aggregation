package se.tink.backend.aggregation.agents.nxgen.se.openbanking.swedbank.fetcher.transactionalaccount.entity.balance;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class AccountBalanceEntity {
    private BalanceAuthorisedEntity authorised;
    private BalanceInterimAvailableEntity interimAvailable;

    public BalanceAuthorisedEntity getAuthorised() {
        return authorised;
    }

    public BalanceInterimAvailableEntity getInterimAvailable() {
        return interimAvailable;
    }
}
