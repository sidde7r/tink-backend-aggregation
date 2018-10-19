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
    private int balance; // expressed in cents
    private int blockedAmount;  // expressed in cents
    private boolean closed;
    private int creditLimit;    // expressed in cents
    private String name;

    @JsonIgnore
    public TransactionalAccount toTinkAccount(String requiredReference) {
        String accountName = Optional.ofNullable(getName()).orElse("Revolut " + getCurrency());

        TransactionalAccount.Builder builder = TransactionalAccount
                .builder(
                    getTinkAccountType(),
                    getId(),
                    new Amount(currency.toUpperCase(), getBalance()))
                .setName(accountName)
                .setAccountNumber(getId());

        if (requiredReference != null) {
            builder.putInTemporaryStorage(
                    RevolutConstants.Accounts.REQUIRED_REFERENCE,
                    requiredReference);
        }

        builder.putInTemporaryStorage(RevolutConstants.Storage.CURRENCY, currency);
        
        return builder.build();
    }

    /*
    @JsonIgnore
    public TransactionalAccount toTinkAccount(List<AccountEntity> accountEntities) {

        // These are the accounts displayed in the Revolut app when requesting details for top up via bank transfer
        List<AccountEntity> AccountEntitiesForTopUp = accountEntities.stream()
                .filter(account -> account.getRequiredReference() != null)
                .collect(Collectors.toList());

        if (AccountEntitiesForTopUp.size() > 0) {
            return toTinkAccount(AccountEntitiesForTopUp.get(0));
        }

        return toTinkAccount(accountEntities.get(0));
    }
    */

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

    public double getBalance() {
        return balance / 100.0;
    }

    public double getBlockedAmount() {
        return blockedAmount / 100.0;
    }

    public boolean isClosed() {
        return closed;
    }

    public double getCreditLimit() {
        return creditLimit / 100.0;
    }

    public String getName() {
        return name;
    }
}
