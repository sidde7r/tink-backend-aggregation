package se.tink.backend.aggregation.agents.nxgen.fi.banks.omasp.fetcher.loan.rpc;

import se.tink.backend.aggregation.agents.nxgen.fi.banks.omasp.fetcher.loan.entities.LoanDetailsEntity;
import se.tink.backend.aggregation.agents.nxgen.fi.banks.omasp.rpc.OmaspBaseResponse;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class LoanDetailsResponse extends OmaspBaseResponse {
    private LoanDetailsEntity loan;

    public LoanDetailsEntity getLoan() {
        return loan;
    }
}
