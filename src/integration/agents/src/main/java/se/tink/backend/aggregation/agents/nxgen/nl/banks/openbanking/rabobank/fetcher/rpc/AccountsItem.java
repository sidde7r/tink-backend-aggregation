package se.tink.backend.aggregation.agents.nxgen.nl.banks.openbanking.rabobank.fetcher.rpc;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.agents.nxgen.nl.banks.openbanking.rabobank.RabobankConstants;
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

    public void setResourceId(final String resourceId) {
        this.resourceId = resourceId;
    }

    public String getResourceId() {
        return resourceId;
    }

    public void setLinks(final Links links) {
        this.links = links;
    }

    public Links getLinks() {
        return links;
    }

    public void setIban(final String iban) {
        this.iban = iban;
    }

    public String getIban() {
        return iban;
    }

    public void setCurrency(final String currency) {
        this.currency = currency;
    }

    public String getCurrency() {
        return currency;
    }

    public void setStatus(final String status) {
        this.status = status;
    }

    public String getStatus() {
        return status;
    }

    @JsonIgnore
    public TransactionalAccount toCheckingAccount(final BalanceResponse balanceResponse) {
        return CheckingAccount.builder()
                .setUniqueIdentifier(getIban())
                .setAccountNumber(getIban())
                .setBalance(balanceResponse.toAmount())
                .addAccountIdentifier(new IbanIdentifier(getIban()))
                .putInTemporaryStorage(RabobankConstants.StorageKey.RESOURCE_ID, getResourceId())
                .build();
    }
}
