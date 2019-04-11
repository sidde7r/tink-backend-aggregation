package se.tink.backend.aggregation.agents.nxgen.fr.banks.bnpparibas.fetcher.transactionalaccounts.entites.accounts;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class RibEntity {
    @JsonProperty("infoAgence")
    private AgencyInfoEntity agencyInfo;

    @JsonProperty("infoClient")
    private CustomerInfoEntity customerInfo;

    @JsonProperty("infoCompte")
    private AccountEntity.AccountInfoEntity accountInfo;

    public AgencyInfoEntity getAgencyInfo() {
        return agencyInfo;
    }

    public CustomerInfoEntity getCustomerInfo() {
        return customerInfo;
    }

    public AccountEntity.AccountInfoEntity getAccountInfo() {
        return accountInfo;
    }
}
