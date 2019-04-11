package se.tink.backend.aggregation.agents.nxgen.fi.banks.spankki.fetcher.loan.rpc;

import se.tink.backend.aggregation.agents.nxgen.fi.banks.spankki.fetcher.loan.entities.LoanDetailsIdentity;
import se.tink.backend.aggregation.agents.nxgen.fi.banks.spankki.rpc.SpankkiRequest;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class LoanDetailsRequest extends SpankkiRequest {
    private LoanDetailsIdentity loanDetails;

    public LoanDetailsRequest setLoanDetails(LoanDetailsIdentity loanDetails) {
        this.loanDetails = loanDetails;
        return this;
    }
}
