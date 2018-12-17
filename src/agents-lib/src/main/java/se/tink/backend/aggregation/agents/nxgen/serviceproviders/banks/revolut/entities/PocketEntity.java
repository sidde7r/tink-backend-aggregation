package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.revolut.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.util.Optional;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.revolut.RevolutConstants;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.revolut.fetcher.transactionalaccount.entities.AccountEntity;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.account.TransactionalAccount;
import se.tink.backend.aggregation.nxgen.core.account.entity.HolderName;
import se.tink.backend.aggregation.rpc.AccountTypes;
import se.tink.backend.core.Amount;

@JsonObject
public class PocketEntity {
    private String id;
    private String name;
    private String type;
    private String state;
    private String currency;
    private int balance; // expressed in cents
    private int blockedAmount;  // expressed in cents
    private boolean closed;
    private int creditLimit;    // expressed in cents

    @JsonIgnore
    public TransactionalAccount toTinkAccount(AccountEntity topUpAccount) {
        String accountNumber = topUpAccount.getIdentifier();
        String accountName = Optional.ofNullable(getName()).orElse("Revolut " + getCurrency());

        TransactionalAccount.Builder builder = TransactionalAccount
                .builder(
                        getTinkAccountType(),
                        getId(),
                        new Amount(currency.toUpperCase(), getBalance()))
                .setName(accountName)
                .setHolderName(new HolderName(topUpAccount.getBeneficiaryName()))
                .setAccountNumber(accountNumber);

        builder.putInTemporaryStorage(RevolutConstants.Storage.CURRENCY, currency);

        return builder.build();
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
