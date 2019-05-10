package se.tink.backend.aggregation.agents.nxgen.nl.banks.openbanking.abnamro.fetcher.rpc;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.agents.nxgen.nl.banks.openbanking.abnamro.AbnAmroConstants;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.account.transactional.CheckingAccount;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;
import se.tink.libraries.account.identifiers.IbanIdentifier;

@JsonObject
public class Account {

    @JsonProperty("accountNumber")
    private String accountNumber;

    @JsonProperty("currency")
    private String currency;

    @JsonProperty("accountHolderName")
    private String accountHolderName;

    public String getAccountNumber() {
        return accountNumber;
    }

    public void setAccountNumber(String accountNumber) {
        this.accountNumber = accountNumber;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public String getAccountHolderName() {
        return accountHolderName;
    }

    public void setAccountHolderName(String accountHolderName) {
        this.accountHolderName = accountHolderName;
    }

    @JsonIgnore
    public TransactionalAccount toCheckingAccount(final BalanceResponse balanceResponse) {
        return CheckingAccount.builder()
                .setUniqueIdentifier(getAccountNumber())
                .setAccountNumber(getAccountNumber())
                .setBalance(balanceResponse.toAmount())
                .setAlias(getAccountHolderName())
                .addAccountIdentifier(new IbanIdentifier(getAccountNumber()))
                .putInTemporaryStorage(AbnAmroConstants.StorageKey.RESOURCE_ID, getAccountNumber())
                .build();
    }
}
