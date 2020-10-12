package se.tink.backend.aggregation.agents.nxgen.nl.banks.openbanking.rabobank.fetcher.rpc;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.agents.nxgen.nl.banks.openbanking.rabobank.RabobankConstants.StorageKey;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.account.transactional.CheckingAccount;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;
import se.tink.libraries.account.identifiers.IbanIdentifier;

@JsonObject
public class AccountsItem {

    @JsonProperty("resourceId")
    private String resourceId;

    @JsonProperty("_links")
    private Links links;

    @JsonProperty("iban")
    private String iban;

    @JsonProperty("currency")
    private String currency;

    @JsonProperty("status")
    private String status;

    @JsonAlias("name")
    private String ownerName;

    public String getResourceId() {
        return resourceId;
    }

    public String getIban() {
        return iban;
    }

    @JsonIgnore
    public TransactionalAccount toCheckingAccount(final BalanceResponse balanceResponse) {
        return CheckingAccount.builder()
                .setUniqueIdentifier(getIban())
                .setAccountNumber(getIban())
                .setBalance(balanceResponse.toAmount())
                .setAlias(getIban())
                .addAccountIdentifier(new IbanIdentifier(getIban()))
                .putInTemporaryStorage(StorageKey.RESOURCE_ID, getResourceId())
                .addHolderName(ownerName)
                .build();
    }
}
