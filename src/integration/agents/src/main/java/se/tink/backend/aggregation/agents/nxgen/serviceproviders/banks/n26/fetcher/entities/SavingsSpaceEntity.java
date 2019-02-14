package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.n26.fetcher.entities;

import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.n26.N26Constants;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.account.transactional.SavingsAccount;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;
import se.tink.libraries.amount.Amount;

@JsonObject
public class SavingsSpaceEntity {

    private String id;
    private String accountId;
    private String name;
    private String imageUrl;
    private String backgroundImageUrl;
    private Balance balance;
    private boolean isPrimary;
    private boolean isHiddenFromBalance;
    private boolean isCardAttached;
    private boolean isLocked;
    private Goal goal;

    public boolean isPrimary() {
        return isPrimary;
    }

    public TransactionalAccount toSavingsAccount() {
        return SavingsAccount
                .builder(getAccountId(), getAmount())
                .setAccountNumber(getAccountId())
                .setName(getName())
                .setBankIdentifier(getAccountId())
                .putInTemporaryStorage(N26Constants.SPACE_ID, id)
                .build();

    }

    public String getAccountId() {
        return accountId;
    }

    public String getName() {
        return name;
    }

    public Amount getAmount() {
        return balance.getAvailableBalance();
    }
}
