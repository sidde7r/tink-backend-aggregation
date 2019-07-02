package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.handelsbanken.fetcher.transactionalaccount.rpc;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.handelsbanken.fetcher.transactionalaccount.entity.BalancesItemEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.handelsbanken.fetcher.transactionalaccount.entity.LinksEntity;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class BalanceAccountResponse {

    private String accountId;

    private List<BalancesItemEntity> balances;

    private String bban;

    private String ownerName;

    @JsonProperty("_links")
    private LinksEntity linksEntity;

    private String iban;

    private String accountType;

    private String name;

    private String currency;

    private String bic;

    private String clearingNumber;

    public String getAccountId() {
        return accountId;
    }

    public List<BalancesItemEntity> getBalances() {
        return balances;
    }

    public String getBban() {
        return bban;
    }

    public String getOwnerName() {
        return ownerName;
    }

    public LinksEntity getLinksEntity() {
        return linksEntity;
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
