package se.tink.backend.aggregation.agents.nxgen.dk.banks.nordea.fetcher.investment.rpc;

import java.util.List;
import se.tink.backend.aggregation.agents.nxgen.dk.banks.nordea.fetcher.investment.entities.CustodyAccountEntity;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class CustodyAccountsResponse {

    private List<CustodyAccountEntity> accounts;

    public List<CustodyAccountEntity> getAccounts() {
        return accounts;
    }
}
