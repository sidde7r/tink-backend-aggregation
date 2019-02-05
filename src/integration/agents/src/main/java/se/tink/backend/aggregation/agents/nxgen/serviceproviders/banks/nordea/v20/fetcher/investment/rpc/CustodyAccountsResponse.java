package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.nordea.v20.fetcher.investment.rpc;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Collections;
import java.util.List;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.nordea.v20.fetcher.investment.entities.CustodyAccount;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.nordea.v20.fetcher.investment.entities.CustodyAccountsEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.nordea.v20.rpc.NordeaResponse;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class CustodyAccountsResponse extends NordeaResponse {
    @JsonProperty("getCustodyAccountsOut")
    private CustodyAccountsEntity custodyAccountsEntity;

    public List<CustodyAccount> getCustodyAccounts() {
        return custodyAccountsEntity != null ? custodyAccountsEntity.getCustodyAccounts() : Collections.emptyList();
    }
}
