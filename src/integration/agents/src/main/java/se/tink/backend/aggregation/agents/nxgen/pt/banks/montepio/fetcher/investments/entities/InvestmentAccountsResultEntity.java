package se.tink.backend.aggregation.agents.nxgen.pt.banks.montepio.fetcher.investments.entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class InvestmentAccountsResultEntity {

    @JsonProperty("CustomerProducts")
    private List<InvestmentAccountEntity> accounts;

    public List<InvestmentAccountEntity> getAccounts() {
        return accounts;
    }
}
