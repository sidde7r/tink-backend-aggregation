package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.revolut.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.google.common.base.Strings;
import java.util.Optional;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.revolut.RevolutConstants;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.balance.BalanceModule;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.id.IdModule;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccountType;
import se.tink.libraries.account.identifiers.OtherIdentifier;
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
    public Optional<TransactionalAccount> toTransactionalAccount(
            String accountNumber, String holderName) {
        return TransactionalAccount.nxBuilder()
                .withType(TransactionalAccountType.CHECKING)
                .withPaymentAccountFlag()
                .withBalance(
                        BalanceModule.of(
                                ExactCurrencyAmount.of(getBalance(), currency.toUpperCase())))
                .withId(
                        IdModule.builder()
                                .withUniqueIdentifier(id)
                                .withAccountNumber(
                                        Strings.isNullOrEmpty(accountNumber) ? id : accountNumber)
                                .withAccountName(getAccountName())
                                .addIdentifier(new OtherIdentifier(id))
                                .build())
                .setApiIdentifier(id)
                .addHolderName(holderName)
                .putInTemporaryStorage(RevolutConstants.Storage.CURRENCY, currency)
                .build();
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
