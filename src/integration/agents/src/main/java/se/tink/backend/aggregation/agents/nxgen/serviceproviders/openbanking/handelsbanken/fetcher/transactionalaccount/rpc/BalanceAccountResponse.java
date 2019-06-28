package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.handelsbanken.fetcher.transactionalaccount.rpc;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.handelsbanken.fetcher.transactionalaccount.entity.BalancesItem;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.handelsbanken.fetcher.transactionalaccount.entity.Links;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class BalanceAccountResponse {

    @JsonProperty("accountId")
    private String accountId;

    @JsonProperty("balances")
    private List<BalancesItem> balances;

    @JsonProperty("bban")
    private String bban;

    @JsonProperty("ownerName")
    private String ownerName;

    @JsonProperty("_links")
    private Links links;

    @JsonProperty("iban")
    private String iban;

    @JsonProperty("accountType")
    private String accountType;

    @JsonProperty("name")
    private String name;

    @JsonProperty("currency")
    private String currency;

    @JsonProperty("bic")
    private String bic;

    @JsonProperty("clearingNumber")
    private String clearingNumber;

    public String getAccountId() {
        return accountId;
    }

    public List<BalancesItem> getBalances() {
        return balances;
    }

    public String getBban() {
        return bban;
    }

    public String getOwnerName() {
        return ownerName;
    }

    public Links getLinks() {
        return links;
    }

    public String getIban() {
        return iban;
    }

    public String getAccountType() {
        return accountType;
    }

    public String getName() {
        return name;
    }

    public String getCurrency() {
        return currency;
    }

    public String getBic() {
        return bic;
    }

    public String getClearingNumber() {
        return clearingNumber;
    }
}
