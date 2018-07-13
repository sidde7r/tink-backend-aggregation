package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.revolut.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.revolut.RevolutConstants;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.revolut.fetcher.transactionalaccount.entities.AccountEntity;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.account.TransactionalAccount;
import se.tink.backend.aggregation.rpc.AccountTypes;
import se.tink.backend.core.Amount;

@JsonObject
public class PocketEntity {
    private String id;
    private String type;
    private String state;
    private String currency;
    private int balance;
    private int blockedAmount;
    private boolean closed;
    private int creditLimit;
    private String name;

    @JsonIgnore
    public TransactionalAccount toTinkAccount(AccountEntity accountEntity) {

        TransactionalAccount.Builder builder = TransactionalAccount
                .builder(
                    getTinkAccountType(),
                    Optional.of(accountEntity.getIban()).orElse(accountEntity.getAccountNumber()),
                    new Amount(currency.toUpperCase(), (double) balance));

        if (accountEntity.getRequiredReference() != null) {
            builder.addToTemporaryStorage(
                    RevolutConstants.Accounts.REQUIRED_REFERENCE,
                    accountEntity.getRequiredReference());
        }
        
        return builder.build();
    }

    @JsonIgnore
    public TransactionalAccount toTinkAccount(List<AccountEntity> accountEntities) {

        /* These are the accounts displayed in the Revolut app when requesting details for top up via bank transfer */
        List<AccountEntity> AccountEntitiesForTopUp = accountEntities.stream()
                .filter(account -> account.getRequiredReference() != null)
                .collect(Collectors.toList());

        if (AccountEntitiesForTopUp.size() > 0) {
            return toTinkAccount(AccountEntitiesForTopUp.get(0));
        }

        return toTinkAccount(accountEntities.get(0));
    }

    @JsonIgnore
    private AccountTypes getTinkAccountType() {
        return (type.equalsIgnoreCase(RevolutConstants.Pockets.SAVINGS_ACCOUNT)
                ? AccountTypes.SAVINGS
                : AccountTypes.CHECKING);
    }

    public String getId() {
        return id;
    }

    public String getType() {
        return type;
    }

    public String getState() {
        return state;
    }

    public String getCurrency() {
        return currency;
    }

    public int getBalance() {
        return balance;
    }

    public int getBlockedAmount() {
        return blockedAmount;
    }

    public boolean isClosed() {
        return closed;
    }

    public int getCreditLimit() {
        return creditLimit;
    }

    public String getName() {
        return name;
    }
}
