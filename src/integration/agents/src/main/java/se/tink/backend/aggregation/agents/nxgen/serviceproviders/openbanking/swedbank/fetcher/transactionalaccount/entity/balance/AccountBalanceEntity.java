package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.swedbank.fetcher.transactionalaccount.entity.balance;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class AccountBalanceEntity {
    private Account account;
    private BalanceAmount balances;
    private BalanceAuthorisedEntity authorised;
    private BalanceInterimAvailableEntity interimAvailable;
    private BalanceAmountEntity balanceAmountEntity;

    public BalanceAuthorisedEntity getAuthorised() {
        return authorised;
    }

    public BalanceInterimAvailableEntity getInterimAvailable() {
        return interimAvailable;
    }

    public BalanceAmountEntity getBalanceAmountEntity() {
        return balanceAmountEntity;
    }
}
