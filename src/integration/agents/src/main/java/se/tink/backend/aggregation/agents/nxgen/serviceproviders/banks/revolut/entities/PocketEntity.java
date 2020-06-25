package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.revolut.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.google.common.base.Strings;
import se.tink.backend.agents.rpc.AccountTypes;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.revolut.RevolutConstants;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.account.entity.HolderName;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;
import se.tink.libraries.account.enums.AccountFlag;
import se.tink.libraries.amount.ExactCurrencyAmount;

@JsonObject
public class PocketEntity {
    private String id;
    private String name;
    private String type;
    private String state;
    private String currency;
    private int balance; // expressed in cents
    private int blockedAmount; // expressed in cents
    private boolean closed;
    private int creditLimit; // expressed in cents

    @JsonIgnore
    public TransactionalAccount toTinkCheckingAccount(String accountNumber, String holderName) {
        return toTinkAccount(accountNumber, holderName, AccountTypes.CHECKING);
    }

    private TransactionalAccount toTinkAccount(
            String accountNumber, String holderName, AccountTypes accountType) {
        TransactionalAccount.Builder builder =
                TransactionalAccount.builder(
                                accountType,
                                id,
                                ExactCurrencyAmount.of(getBalance(), currency.toUpperCase()))
                        .addAccountFlag(AccountFlag.PSD2_PAYMENT_ACCOUNT)
                        .setName(getAccountName())
                        .setHolderName(new HolderName(holderName))
                        .setBankIdentifier(id);

        if (!Strings.isNullOrEmpty(accountNumber)) {
            builder.setAccountNumber(accountNumber);
        } else {
            builder.setAccountNumber(id);
        }

        builder.putInTemporaryStorage(RevolutConstants.Storage.CURRENCY, currency);
        return builder.build();
    }

    @JsonIgnore
    private String getAccountName() {
        return Strings.isNullOrEmpty(name) ? "Revolut " + getCurrency() : name;
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

    public boolean isActive() {
        return RevolutConstants.Accounts.ACTIVE_STATE.equalsIgnoreCase(getState());
    }

    public boolean isOpen() {
        return !closed;
    }

    public double getCreditLimit() {
        return creditLimit / 100.0;
    }

    public String getName() {
        return name;
    }

    @JsonIgnore
    public boolean isCryptoCurrency() {
        return currency != null
                && RevolutConstants.REVOLUT_SUPPORTED_CRYPTO_CURRENCIES.contains(
                        currency.toUpperCase());
    }
}
