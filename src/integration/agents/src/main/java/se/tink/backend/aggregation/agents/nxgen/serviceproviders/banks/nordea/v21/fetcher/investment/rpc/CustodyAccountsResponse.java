package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.nordea.v21.fetcher.investment.rpc;

import com.google.common.collect.Lists;
import java.util.List;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.nordea.v21.fetcher.investment.entities.CustodyAccount;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.nordea.v21.rpc.NordeaResponse;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class CustodyAccountsResponse extends NordeaResponse {
    private List<CustodyAccount> custodyAccounts = Lists.newArrayList();

    public List<CustodyAccount> getCustodyAccounts() {
        return custodyAccounts;
    }
}
