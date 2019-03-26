package se.tink.backend.aggregation.agents.nxgen.es.banks.sabadell.fetcher.loans.rpc;

import se.tink.backend.aggregation.agents.nxgen.es.banks.sabadell.fetcher.entities.PaginatorEntity;
import se.tink.backend.aggregation.agents.nxgen.es.banks.sabadell.fetcher.loans.entities.AccountEntity;
import se.tink.backend.aggregation.annotations.JsonObject;

import java.util.List;

@JsonObject
public class LoansResponse {
    private String aIndamor;
    private List<AccountEntity> accounts;
    private PaginatorEntity paginator;

    public List<AccountEntity> getAccounts() {
        return accounts;
    }
}
