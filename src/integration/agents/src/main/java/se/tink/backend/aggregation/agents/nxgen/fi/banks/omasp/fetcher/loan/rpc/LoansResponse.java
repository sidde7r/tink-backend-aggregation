package se.tink.backend.aggregation.agents.nxgen.fi.banks.omasp.fetcher.loan.rpc;

import java.util.List;
import se.tink.backend.aggregation.agents.nxgen.fi.banks.omasp.fetcher.loan.entities.LoanEntity;
import se.tink.backend.aggregation.agents.nxgen.fi.banks.omasp.rpc.OmaspBaseResponse;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class LoansResponse extends OmaspBaseResponse {
    private List<LoanEntity> loans;

    public List<LoanEntity> getLoans() {
        return loans;
    }
}
