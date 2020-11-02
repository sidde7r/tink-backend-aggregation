package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.danskebank.fetchers.investment.rpc;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Collections;
import java.util.List;
import lombok.Getter;
import se.tink.backend.aggregation.annotations.JsonObject;

@Getter
@JsonObject
public class InvestmentAccountsResponse {
    @JsonProperty("package")
    private long pack;

    private CustodyAccountsEntity custodyAccounts;
    private CustomerProfileEntity customerProfile;
    private boolean disableInvestment;

    public List<GroupEntity> getGroups() {
        return custodyAccounts != null ? custodyAccounts.getGroups() : Collections.emptyList();
    }
}
