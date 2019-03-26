package se.tink.backend.aggregation.agents.nxgen.es.banks.sabadell.fetcher.loans.rpc;

import se.tink.backend.aggregation.agents.nxgen.es.banks.sabadell.fetcher.loans.entities.AccountEntity;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class LoanDetailsRequest {
    private AccountEntity account;

    public LoanDetailsRequest(AccountEntity account) {
        this.account = account;
    }
}
