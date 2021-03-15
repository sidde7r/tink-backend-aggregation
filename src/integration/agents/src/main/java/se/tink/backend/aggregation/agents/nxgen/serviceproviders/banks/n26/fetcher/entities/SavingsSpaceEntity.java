package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.n26.fetcher.entities;

import java.util.Optional;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.n26.N26Constants;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.balance.BalanceModule;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.id.IdModule;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccountType;
import se.tink.libraries.account.identifiers.OtherIdentifier;
import se.tink.libraries.amount.ExactCurrencyAmount;

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

    public Optional<TransactionalAccount> toSavingsAccount() {
        return TransactionalAccount.nxBuilder()
                .withType(TransactionalAccountType.SAVINGS)
                .withoutFlags()
                .withBalance(BalanceModule.of(getAmount()))
                .withId(
                        IdModule.builder()
                                .withUniqueIdentifier(getAccountId())
                                .withAccountNumber(getAccountId())
                                .withAccountName(getName())
                                .addIdentifier(new OtherIdentifier(getAccountId()))
                                .build())
                .putInTemporaryStorage(N26Constants.SPACE_ID, id)
                .build();
    }

    public String getAccountId() {
        return accountId;
    }

    public String getName() {
        return name;
    }

    public ExactCurrencyAmount getAmount() {
        return balance.getAvailableBalance();
    }
}
