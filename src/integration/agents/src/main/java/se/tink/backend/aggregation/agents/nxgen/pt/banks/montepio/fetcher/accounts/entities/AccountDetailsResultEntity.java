package se.tink.backend.aggregation.agents.nxgen.pt.banks.montepio.fetcher.accounts.entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class AccountDetailsResultEntity {

    @JsonProperty("ProductDetails")
    private List<AccountDetailsEntity> accountDetails;

    public List<AccountDetailsEntity> getAccountDetails() {
        return accountDetails;
    }
}
