package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.crosskey.fetcher.loan.rpc;

import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.crosskey.fetcher.loan.entities.LoanDetailsEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.crosskey.rpc.CrossKeyResponse;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class LoanDetailsResponse extends CrossKeyResponse {

    private LoanDetailsEntity loanDetailsVO;

    public LoanDetailsEntity getLoanDetails() {
        return loanDetailsVO;
    }
}
