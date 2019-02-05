package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.nordea.v20.fetcher.investment.entities;

import java.util.Collections;
import java.util.List;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class CustodyAccountsEntity {
    private List<CustodyAccount> custodyAccounts;

    public List<CustodyAccount> getCustodyAccounts() {
        return custodyAccounts != null ? custodyAccounts : Collections.emptyList();
    }
}
