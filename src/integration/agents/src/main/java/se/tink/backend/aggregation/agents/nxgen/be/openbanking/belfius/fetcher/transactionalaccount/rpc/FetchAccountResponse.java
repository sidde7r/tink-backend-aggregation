package se.tink.backend.aggregation.agents.nxgen.be.openbanking.belfius.fetcher.transactionalaccount.rpc;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import se.tink.backend.aggregation.agents.nxgen.be.openbanking.belfius.fetcher.transactionalaccount.entity.account.Links;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.balance.BalanceModule;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.id.IdModule;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccountType;
import se.tink.libraries.account.identifiers.IbanIdentifier;
import se.tink.libraries.amount.Amount;

@JsonObject
public class FetchAccountResponse {

    @JsonProperty("_links")
    private Links links;

    private Double balance;

    private String currency;

    private String iban;

    private Boolean multicurrency;

    @JsonProperty("other_compartments")
    private List<Object> otherCompartments;

    private String type;

    public Links getLinks() {
        return links;
    }

    public void setLinks(Links links) {
        this.links = links;
    }

    public TransactionalAccount toTinkAccount(String logicalId) {
        return TransactionalAccount.nxBuilder()
                // TODO: API returns a hard codded string for type, we cannot test it
                .withType(TransactionalAccountType.CHECKING)
                .withBalance(BalanceModule.of(new Amount(currency, balance)))
                .withId(
                        IdModule.builder()
                                .withUniqueIdentifier(iban)
                                .withAccountNumber(iban)
                                // TODO: API doesn't return account name
                                .withAccountName("")
                                .addIdentifier(new IbanIdentifier(iban))
                                .build())
                .setApiIdentifier(logicalId)
                // TODO: API doesn't return account name
                .addHolderName("")
                .build();
    }

    public Double getBalance() {
        return balance;
    }

    public void setBalance(Double balance) {
        this.balance = balance;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public String getIban() {
        return iban;
    }

    public void setIban(String iban) {
        this.iban = iban;
    }

    public Boolean getMulticurrency() {
        return multicurrency;
    }

    public void setMulticurrency(Boolean multicurrency) {
        this.multicurrency = multicurrency;
    }

    public List<Object> getOtherCompartments() {
        return otherCompartments;
    }

    public void setOtherCompartments(List<Object> otherCompartments) {
        this.otherCompartments = otherCompartments;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
}
